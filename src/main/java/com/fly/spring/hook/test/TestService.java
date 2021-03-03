package com.fly.spring.hook.test;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @author guoxiang
 * @version 1.0.0
 * @since 2021/2/2
 */
@Service
public class TestService {

    @PostConstruct
    public void init() {
        System.out.println("hello this is test service PostConstruct");
    }

    @PreDestroy
    public void destroy() {
        System.out.println("PreDestroy");
    }

    @Async
    public void testService() {
        System.out.println("father");
    }

    public String test(Object param, String param2) {
        return "original test service: " + param + param2;
    }

    public String test(String param) {
        return "original test service: " + param;
    }


    @EventListener
    public void onEvent(TestEvent event) {
        System.out.println("father " + event.getMessage());
    }

}
