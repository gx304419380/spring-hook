package com.fly.spring.hook.config;

import com.fly.spring.hook.controller.SpringHookController;
import com.fly.spring.hook.util.RequestMappingUtils;
import com.fly.spring.hook.util.SpringHookContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author guoxiang
 * @version 1.0.0
 * @since 2021/2/1
 */
@Configuration
@ConditionalOnProperty(value = "spring.hook.enable", matchIfMissing = true)
@EnableAsync(proxyTargetClass = true)
public class SpringHookAutoConfig {

    @Bean
    @ConditionalOnMissingBean
    public SpringHookContext springHookContext() {
        return new SpringHookContext();
    }


    @Bean
    @ConditionalOnMissingBean
    public SpringHookController springHookController() {
        return new SpringHookController();
    }


    @Bean
    @ConditionalOnMissingBean
    public RequestMappingUtils requestMappingUtils() {
        return new RequestMappingUtils();
    }

}
