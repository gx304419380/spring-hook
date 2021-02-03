package com.fly.spring.hook.util;

import com.fly.spring.hook.exception.LoadClassException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.ClassUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author guoxiang
 * @version 1.0.0
 * @since 2021/2/1
 * @apiNote 上下文，用于获取spring context中的bean并解析依赖
 */
public class SpringHookContext implements ApplicationContextAware, ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(SpringHookContext.class);

    private static GenericApplicationContext applicationContext;

    private static void setStaticApplicationContext(ApplicationContext applicationContext) {
        SpringHookContext.applicationContext = (GenericApplicationContext) applicationContext;
    }

    public static Class<?> getByClassName(String className) {
        try {
            return ClassUtils.forName(className, null);
        } catch (ClassNotFoundException e) {
            log.error("- class not find error", e);
            throw new LoadClassException(className);
        }
    }



    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        setStaticApplicationContext(applicationContext);
    }

    /**
     * 启动监听器，便利spring的bean，将依赖关系保存起来
     *
     * @param applicationReadyEvent     项目启动事件
     */
    @Override
    @Async
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {

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
