package com.fly.spring.hook.constant;

/**
 * 方法hook点
 * @author guoxiang
 */
public enum HookMethodType {
    /**
     * 方法前插入
     */
    BEFORE,
    /**
     * 方法后插入
     */
    AFTER,
    /**
     * 方法替换
     */
    REPLACE,
    /**
     * 在finally中插入
     */
    FINALLY
}
