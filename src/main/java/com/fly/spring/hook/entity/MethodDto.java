package com.fly.spring.hook.entity;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * @author guoxiang
 * @version 1.0.0
 * @since 2021/3/1
 */
public class MethodDto {
    private String methodName;
    private List<String> argClassList;
    private String argClassListString;
    private String returnClass;

    public MethodDto(Method method) {
        methodName = method.getName();
        returnClass = method.getReturnType().getSimpleName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        argClassList = Stream.of(parameterTypes)
                .map(Class::getName)
                .collect(Collectors.toList());

        argClassListString = Stream.of(parameterTypes)
                .map(Class::getSimpleName)
                .collect(joining(", "));
    }

    public String getMethodName() {
        return methodName;
    }

    public MethodDto setMethodName(String methodName) {
        this.methodName = methodName;
        return this;
    }

    public List<String> getArgClassList() {
        return argClassList;
    }

    public MethodDto setArgClassList(List<String> argClassList) {
        this.argClassList = argClassList;
        return this;
    }

    public String getReturnClass() {
        return returnClass;
    }

    public MethodDto setReturnClass(String returnClass) {
        this.returnClass = returnClass;
        return this;
    }

    public String getArgClassListString() {
        return argClassListString;
    }

    public MethodDto setArgClassListString(String argClassListString) {
        this.argClassListString = argClassListString;
        return this;
    }
}
