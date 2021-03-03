package com.fly.spring.hook.config;

import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author guoxiang
 * @version 1.0.0
 * @since 2021/3/3
 */
public class SpringHookWebMvcConfig  implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/spring-hook/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/spring-hook/")
                .resourceChain(false);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/spring-hook/")
                .setViewName("forward:/spring-hook/index.html");

        registry.addViewController("/spring-hook")
                .setViewName("redirect:/spring-hook/index.html");
    }
}
