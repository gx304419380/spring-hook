package com.fly.spring.hook.entity;

import com.fly.spring.hook.constant.HookMethodType;

import java.util.List;

public class HookMethodDto {

    // beanName      bean名称
    private String beanName;

    // methodName    方法名
    private String methodCode;

    // methodCode    新方法代码
    private String methodName;

    // 参数类型列表
    private List<String> argClassList;

    // hook方法类型 包括前插，后插，整体替换 以及 finally块插入
    private HookMethodType hookMethodType;

    public String getBeanName() {
        return beanName;
    }

    public HookMethodDto setBeanName(String beanName) {
        this.beanName = beanName;
        return this;
    }

    public String getMethodCode() {
        return methodCode;
    }

    public HookMethodDto setMethodCode(String methodCode) {
        this.methodCode = methodCode;
        return this;
    }

    public String getMethodName() {
        return methodName;
    }

    public HookMethodDto setMethodName(String methodName) {
        this.methodName = methodName;
        return this;
    }

    public HookMethodType getHookMethodType() {
        return hookMethodType;
    }

    public HookMethodDto setHookMethodType(HookMethodType hookMethodType) {
        this.hookMethodType = hookMethodType;
        return this;
    }

    public List<String> getArgClassList() {
        return argClassList;
    }

    public HookMethodDto setArgClassList(List<String> argClassList) {
        this.argClassList = argClassList;
        return this;
    }
}
