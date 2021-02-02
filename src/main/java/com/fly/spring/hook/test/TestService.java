package com.fly.spring.hook.test;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @author guoxiang
 * @version 1.0.0
 * @since 2021/2/2
 */
@Service
public class TestService {

    @Async
    public void testService() {
        System.out.println("test service");
    }

}
