package com.fly.spring.hook.entity;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author guoxiang
 * @version 1.0.0
 * @since 2021/3/1
 */
public class BeanDto {
    private String beanName;
    private String className;
    private List<MethodDto> methodList;

    public BeanDto(String beanName, String className) {
        this.beanName = beanName;
        this.className = className;
    }

    public BeanDto(BeanInfo beanInfo) {
        beanName = beanInfo.getBeanName();
        className = beanInfo.getTargetClass().getName();

        Method[] methods = beanInfo.getTargetClass().getDeclaredMethods();
        this.methodList = Stream.of(methods)
                .map(MethodDto::new)
                .collect(Collectors.toList());
    }

    public String getBeanName() {
        return beanName;
    }

    public BeanDto setBeanName(String beanName) {
        this.beanName = beanName;
        return this;
    }

    public String getClassName() {
        return className;
    }

    public BeanDto setClassName(String className) {
        this.className = className;
        return this;
    }

    public List<MethodDto> getMethodList() {
        return methodList;
    }

    public BeanDto setMethodList(List<MethodDto> methodList) {
        this.methodList = methodList;
        return this;
    }
}
