package com.fly.spring.hook.entity;

/**
 * @author guoxiang
 * @version 1.0.0
 * @since 2021/2/4
 */
public class BeanInfo {
    private String beanName;

    private Object bean;

    private Boolean isProxy;

    private Object target;

    private Class<?> targetClass;

    public BeanInfo(String beanName, Object bean, Boolean isProxy, Object target, Class<?> targetClass) {
        this.beanName = beanName;
        this.bean = bean;
        this.isProxy = isProxy;
        this.target = target;
        this.targetClass = targetClass;
    }

    public ClassLoader getBeanClassLoader() {
        return targetClass.getClassLoader();
    }

    public String getBeanName() {
        return beanName;
    }

    public BeanInfo setBeanName(String beanName) {
        this.beanName = beanName;
        return this;
    }

    public Object getBean() {
        return bean;
    }

    public BeanInfo setBean(Object bean) {
        this.bean = bean;
        return this;
    }

    public Boolean getProxied() {
        return isProxy;
    }

    public BeanInfo setProxied(Boolean proxied) {
        isProxy = proxied;
        return this;
    }

    public Object getTarget() {
        return target;
    }

    public BeanInfo setTarget(Object target) {
        this.target = target;
        return this;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public BeanInfo setTargetClass(Class<?> targetClass) {
        this.targetClass = targetClass;
        return this;
    }
}
