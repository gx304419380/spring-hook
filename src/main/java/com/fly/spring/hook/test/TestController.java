package com.fly.spring.hook.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;

/**
 * @author guoxiang
 * @version 1.0.0
 * @since 2021/2/3
 */
@RestController
@RequestMapping("test")
public class TestController {

    @Autowired
    private TestService testService;

    @PostMapping
    public String reload(String param) {
        return testService.test(param);
    }

}
