package com.fly.spring.hook.util;

import com.fly.spring.hook.entity.BeanInfo;
import com.fly.spring.hook.exception.LoadClassException;
import javassist.*;
import javassist.expr.ConstructorCall;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;

/**
 * @author guoxiang
 * @version 1.0.0
 * @since 2021/2/1
 * @apiNote 上下文，用于获取spring context中的bean并解析依赖
 */
public class SpringHookContext extends RequestMappingHandlerMapping {

    private static final Logger log = LoggerFactory.getLogger(SpringHookContext.class);

    @Autowired
    private GenericApplicationContext applicationContext;

    public void replace(String beanName, InputStream classInputStream) {
        BeanInfo beanInfo = getBeanInfo(beanName);

        Class<?> subClass = generateSubClass(beanInfo, classInputStream);

        DefaultListableBeanFactory beanFactory = applicationContext.getDefaultListableBeanFactory();
        beanFactory.removeBeanDefinition(beanName);

        AbstractBeanDefinition bd = BeanDefinitionBuilder.genericBeanDefinition(subClass).getBeanDefinition();
        bd.setBeanClassName(subClass.getName());

        beanFactory.registerBeanDefinition(beanName, bd);

        Object bean = beanFactory.getBean(beanName);
        log.info("refresh bean finish: {}", bean);

        handleRequestMapping(beanName);
    }


    /**
     * 将新的类继承要替换的类
     *
     * @param beanInfo          bean信息
     * @param classInputStream  类文件输入流
     * @return                  新类
     */
    private Class<?> generateSubClass(BeanInfo beanInfo, InputStream classInputStream) {
        String name = beanInfo.getTargetClass().getName();
        // 类库池, jvm中所加载的class
        ClassPool pool = ClassPool.getDefault();

        try {
            CtClass father = pool.get(name);
            CtClass ctClass = pool.makeClass(classInputStream);
            ctClass.setName(name + "__JAVASSIST");
            ctClass.setSuperclass(father);

            //这里需要将构造函数整理一下，如果父类没有空构造会报错！！！
            CtConstructor[] constructors = ctClass.getDeclaredConstructors();
            for (CtConstructor constructor : constructors) {
                constructor.instrument(new ExprEditor() {
                    @Override
                    public void edit(ConstructorCall c) throws CannotCompileException {
                        if (c.isSuper()) {
                            c.replace("{}");
                        }
                    }
                });
                constructor.insertBeforeBody("super($$);");
            }


            ctClass.writeFile("D:/");
            return ctClass.toClass(beanInfo.getBeanClassLoader(), null);
        } catch (Exception e) {
            throw new LoadClassException(e);
        }

    }

    @Override
    public void afterPropertiesSet() {
    }

    /**
     * 处理该类中RequestMapping相关方法
     *
     * @param beanName  bean名称
     */
    private void handleRequestMapping(String beanName) {
        processCandidateBean(beanName);
        handlerMethodsInitialized(getHandlerMethods());
    }


    /**
     * 获取bean信息
     *
     * @param beanName  bean名称
     * @return          bean信息
     */
    public BeanInfo getBeanInfo(String beanName) {
        Object bean = applicationContext.getBean(beanName);

        boolean isProxy = AopUtils.isAopProxy(bean);

        Class<?> targetClass = isProxy ? AopProxyUtils.ultimateTargetClass(bean) : bean.getClass();

        Object target = isProxy ? AopProxyUtils.getSingletonTarget(bean) : null;

        return new BeanInfo(beanName, bean, isProxy, target, targetClass);
    }

    /**
     * 启动监听器，便利spring的bean，将依赖关系保存起来
     *
     */
    @Async
    @EventListener(classes = ApplicationReadyEvent.class)
    public void onApplicationEvent() {

        // 获取当前启动类包路径
        String defaultPackage = resolveDefaultPackage();

        if (defaultPackage == null) {
            log.info("cannot find default package");
            return;
        }

        //扫描bean，将包路径下所有的bean找出来
        Map<String, Class<?>> classMap = resolveBeanMap(defaultPackage);

        //解析bean的依赖
        resolveDependencies(classMap);

    }


    /**
     * 解析bean的依赖
     *
     * @param classMap  所有bean信息
     */
    private void resolveDependencies(Map<String, Class<?>> classMap) {

        for (String name : classMap.keySet()) {
            BeanDefinition definition = applicationContext.getBeanDefinition(name);
            Object bean = applicationContext.getBean(name);
            Class<?> targetClass = classMap.get(name);

        }
    }

    /**
     * 解析包下所有的bean
     *
     * @param defaultPackage    包
     * @return                  bean信息
     */
    private Map<String, Class<?>> resolveBeanMap(String defaultPackage) {
        //扫描bean，将包路径下所有的bean找出来
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        Map<String, Class<?>> classMap = new HashMap<>(16);

        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);

            Class<?> beanClass = AopUtils.isAopProxy(bean) ?
                    AopProxyUtils.ultimateTargetClass(bean) : bean.getClass();

            if (beanClass.getName().startsWith(defaultPackage)) {
                classMap.put(beanName, beanClass);
            }
        }

        return classMap;
    }

    /**
     * @return  启动类所在的包
     */
    private String resolveDefaultPackage() {
        return applicationContext.getBeansWithAnnotation(SpringBootApplication.class)
                .values()
                .stream()
                .map(bean -> bean.getClass().getPackage())
                .map(Package::getName)
                .findFirst()
                .orElse(null);

    }
}
