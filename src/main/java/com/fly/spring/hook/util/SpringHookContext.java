package com.fly.spring.hook.util;

import com.fly.spring.hook.entity.BeanInfo;
import com.fly.spring.hook.entity.HookMethodDto;
import javassist.*;
import javassist.expr.ConstructorCall;
import javassist.expr.ExprEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static com.fly.spring.hook.util.ObjectUtils.notEmpty;

/**
 * @author guoxiang
 * @version 1.0.0
 * @since 2021/2/1
 * @apiNote 上下文，用于获取spring context中的bean并解析依赖
 */
public class SpringHookContext {

    private static final Logger log = LoggerFactory.getLogger(SpringHookContext.class);

    private static final String CLASS_POSTFIX = "__JAVASSIST__";
    private static final String SUCCESS = "SUCCESS";
    private static final AtomicInteger COUNTER = new AtomicInteger(1);

    @Autowired
    private GenericApplicationContext applicationContext;

    @Autowired
    private RequestMappingUtils requestMappingUtils;

    private volatile String defaultPackage;

    private static final ConcurrentHashMap<String, BeanInfo> CACHE = new ConcurrentHashMap<>(512);


    /**
     * 替换指定bean中的某个方法，加锁防并发
     * @return  result
     */
    public synchronized String replaceBeanByMethod(HookMethodDto dto) {

        BeanInfo beanInfo = getBeanInfo(dto.getBeanName());

        //根据传入的方法代码生成一个老class的子类
        Class<?> subClass;
        try {
            subClass = generateClassByMethod(beanInfo, dto);
        } catch (Exception e) {
            log.error("generate class error", e);
            return e.getMessage();
        }

        replaceBean(dto.getBeanName(), subClass);
        return SUCCESS;
    }


    /**
     * 替换bean，加锁防并发
     * @param beanName          bean名称
     * @param classInputStream  class文件
     * @return  result
     */
    public synchronized String replaceBeanByClass(String beanName, InputStream classInputStream) {

        BeanInfo beanInfo = getBeanInfo(beanName);

        //根据传入的新class生成一个老class的子类
        Class<?> subClass;
        try {
            subClass = generateClassByFile(beanInfo, classInputStream);
        } catch (Exception e) {
            log.error("generate class error", e);
            return e.getMessage();
        }

        replaceBean(beanName, subClass);
        return SUCCESS;
    }


    /**
     * 调用spring框架替换bean
     *
     * @param beanName  beanName
     * @param subClass  新的bean类型
     */
    private void replaceBean(String beanName, Class<?> subClass) {
        //移除旧的bean
        DefaultListableBeanFactory beanFactory = applicationContext.getDefaultListableBeanFactory();
        beanFactory.removeBeanDefinition(beanName);

        //定义新的bean definition并注册到spring
        AbstractBeanDefinition bd = BeanDefinitionBuilder.genericBeanDefinition(subClass).getBeanDefinition();
        beanFactory.registerBeanDefinition(beanName, bd);

        //生成bean
        Object bean = beanFactory.getBean(beanName);
        log.info("refresh bean finish: {}", bean);

        //对于controller需要特殊处理，生成request mapping
        handleRequestMapping(beanName);

        //刷新缓存
        CACHE.remove(beanName);
    }


    /**
     * 根据传入的方法字符串，生成一个新的bean class
     *
     * @param beanInfo      bean信息
     * @param dto           hook信息
     * @return              新class
     */
    private Class<?> generateClassByMethod(BeanInfo beanInfo, HookMethodDto dto) throws NotFoundException, CannotCompileException {

        String name = beanInfo.getTargetClass().getName();
        if (name.contains(CLASS_POSTFIX)) {
            name = name.substring(0, name.indexOf(CLASS_POSTFIX));
        }
        // 类库池, jvm中所加载的class
        ClassPool pool = ClassPool.getDefault();

        CtClass father = pool.get(name);
        CtClass sub = pool.getAndRename(name, name + CLASS_POSTFIX + COUNTER.getAndIncrement());
        sub.setSuperclass(father);

        handleConstructors(sub);

        //todo 这里会涉及到重载的问题，等有时间在完善
        CtMethod ctMethod = findMethod(sub, dto.getMethodName(), dto.getArgClassList());

        switch (dto.getHookMethodType()) {
            case REPLACE:
                sub.removeMethod(ctMethod);
                //生成新的方法
                CtMethod method = CtNewMethod.make(dto.getMethodCode(), sub);
                sub.addMethod(method);
                break;

            case BEFORE:
                ctMethod.insertBefore(dto.getMethodCode());
                break;

            case AFTER:
                ctMethod.insertAfter(dto.getMethodCode());
                break;

            case FINALLY:
                ctMethod.insertAfter(dto.getMethodCode(), true);
                break;
            default:
                break;
        }

        return sub.toClass(beanInfo.getBeanClassLoader(), null);
    }

    /**
     * 寻找指定的method
     * @param ctClass       ctClass
     * @param methodName    方法名
     * @param argClassList  参数列表
     * @return              返回ctMethod
     */
    private CtMethod findMethod(CtClass ctClass, String methodName, List<String> argClassList) throws NotFoundException {
        CtMethod[] declaredMethods = ctClass.getDeclaredMethods(methodName);
        if (declaredMethods.length == 1) {
            return declaredMethods[0];
        }

        for (CtMethod method : declaredMethods) {
            CtClass[] parameterTypes = method.getParameterTypes();
            boolean matchParam = Stream.of(parameterTypes).map(CtClass::getName).allMatch(argClassList::contains);
            if (matchParam) {
                return method;
            }
        }

        throw new NotFoundException(methodName);
    }


    /**
     * 将新的类继承要替换的类
     *
     * @param beanInfo          bean信息
     * @param classInputStream  类文件输入流
     * @return                  新类
     */
    private Class<?> generateClassByFile(BeanInfo beanInfo, InputStream classInputStream)
            throws NotFoundException, IOException, CannotCompileException {

        String name = beanInfo.getTargetClass().getName();
        if (name.contains(CLASS_POSTFIX)) {
            name = name.substring(0, name.indexOf(CLASS_POSTFIX));
        }
        // 类库池, jvm中所加载的class
        ClassPool pool = ClassPool.getDefault();

        CtClass father = pool.get(name);
        CtClass sub = pool.makeClass(classInputStream);
        validateClass(sub, father);

        sub.setName(name + CLASS_POSTFIX + COUNTER.getAndIncrement());
        sub.setSuperclass(father);

        handleConstructors(sub);

        return sub.toClass(beanInfo.getBeanClassLoader(), null);
    }


    /**
     * 校验用户上传的class
     * @param sub       子类
     * @param father    父类
     */
    private void validateClass(CtClass sub, CtClass father) {

        boolean same = sub.getName().equals(father.getName());

        if (same) {
            return;
        }

        boolean isSub = sub.subclassOf(father);
        Assert.isTrue(isSub, "类型错误：" + father.getName());
    }


    /**
     * 这里需要将构造函数整理一下，如果父类没有空构造会报错！！！
     * @param ctClass       子类
     */
    private void handleConstructors(CtClass ctClass) throws NotFoundException, CannotCompileException {
        //这里需要将构造函数整理一下，如果父类没有空构造会报错！！！
        for (CtConstructor constructor : ctClass.getDeclaredConstructors()) {
            int argLength = constructor.getParameterTypes().length;

            if (argLength == 0) {
                continue;
            }

            constructor.instrument(ConstructorEditor.INSTANCE);
            constructor.insertBeforeBody("super($$);");
        }

    }

    public Stream<String> getBeanNameStream() {
        String[] names = applicationContext.getBeanDefinitionNames();
        return Stream.of(names).sorted();
    }


    /**
     * 将构造函数中自带的隐式super()去掉
     */
    private static class ConstructorEditor extends ExprEditor {

        static final ConstructorEditor INSTANCE = new ConstructorEditor();

        @Override
        public void edit(ConstructorCall c) throws CannotCompileException {
            if (c.isSuper()) {
                c.replace("{}");
            }
        }
    }


    /**
     * 处理该类中RequestMapping相关方法
     *
     * @param beanName  bean名称
     */
    private void handleRequestMapping(String beanName) {
        // TODO: 2021/2/5 针对controller中的RequestMapping要进行相应的处理
        BeanInfo beanInfo = getBeanInfo(beanName);
        requestMappingUtils.registerController(beanInfo);
    }

    public BeanInfo getBeanInfo(String beanName) {
        return CACHE.computeIfAbsent(beanName, this::doGetBeanInfo);
    }

    /**
     * 获取bean信息
     *
     * @param beanName  bean名称
     * @return          bean信息
     */
    public BeanInfo doGetBeanInfo(String beanName) {

        Object bean = applicationContext.getBean(beanName);

        boolean isProxy = AopUtils.isAopProxy(bean);

        Class<?> targetClass = isProxy ? AopProxyUtils.ultimateTargetClass(bean) : bean.getClass();

        Object target = isProxy ? AopProxyUtils.getSingletonTarget(bean) : null;

        return new BeanInfo(beanName, bean, isProxy, target, targetClass);
    }


    /**
     * @return  启动类所在的包
     */
    private String resolveDefaultPackage() {

        if (notEmpty(defaultPackage)) {
            return defaultPackage;
        }

        defaultPackage = applicationContext.getBeansWithAnnotation(SpringBootApplication.class)
                .values()
                .stream()
                .map(bean -> bean.getClass().getPackage())
                .map(Package::getName)
                .findFirst()
                .orElse(null);

        return defaultPackage;

    }
}
