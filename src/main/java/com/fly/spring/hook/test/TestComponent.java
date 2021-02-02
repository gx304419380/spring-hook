package com.fly.spring.hook.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @author guoxiang
 * @version 1.0.0
 * @since 2021/2/2
 */
@Component
public class TestComponent {

    private final TestService testService;

    public TestComponent(TestService testService) {
        this.testService = testService;
    }

    public void test() {
        testService.testService();
    }

}
