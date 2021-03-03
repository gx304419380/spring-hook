package com.fly.spring.hook.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author guoxiang
 * @version 1.0.0
 * @since 2021/3/3
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(SpringHookAutoConfig.class)
public @interface EnableSpringHook {
}
