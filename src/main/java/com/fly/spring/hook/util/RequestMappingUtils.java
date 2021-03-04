package com.fly.spring.hook.util;

import com.fly.spring.hook.entity.BeanInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.handler.AbstractHandlerMethodMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author guoxiang
 * @version 1.0.0
 * @since 2021/2/5
 */
public class RequestMappingUtils {

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    /**
     * 去掉Controller的Mapping
     * @param beanInfo beanInfo
     */
    public void unregisterController(BeanInfo beanInfo) {

        final Class<?> targetClass = beanInfo.getTargetClass();

        Method createMappingMethod = null;
        try {
            createMappingMethod = RequestMappingHandlerMapping.class.
                    getDeclaredMethod("getMappingForMethod", Method.class, Class.class);
            createMappingMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        Method finalCreateMappingMethod = createMappingMethod;
        ReflectionUtils.doWithMethods(targetClass, method -> {
            Method specificMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
            try {
                RequestMappingInfo requestMappingInfo =(RequestMappingInfo)
                        finalCreateMappingMethod.invoke(requestMappingHandlerMapping, specificMethod, targetClass);

                if(requestMappingInfo != null) {
                    requestMappingHandlerMapping.unregisterMapping(requestMappingInfo);
                }
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }, ReflectionUtils.USER_DECLARED_METHODS);

    }

    public void registerController(BeanInfo beanInfo) {

        Class<?> targetClass = beanInfo.getTargetClass();

        boolean isHandler = AnnotatedElementUtils.hasAnnotation(targetClass, Controller.class) ||
                AnnotatedElementUtils.hasAnnotation(targetClass, RequestMapping.class);

        if (!isHandler) {
            return;
        }

        unregisterController(beanInfo);
        //注册Controller
        Method method= null;
        try {
            method = AbstractHandlerMethodMapping.class.getDeclaredMethod("detectHandlerMethods", Object.class);

            method.setAccessible(true);
            method.invoke(requestMappingHandlerMapping, beanInfo.getBeanName());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}
