package com.fly.spring.hook.test;

/**
 * @author guoxiang
 * @version 1.0.0
 * @since 2021/2/3
 */
public class TestEvent {
    private String message;

    public TestEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public TestEvent setMessage(String message) {
        this.message = message;
        return this;
    }
}
