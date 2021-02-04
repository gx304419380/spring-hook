package com.fly.spring.hook.exception;

/**
 * @author guoxiang
 * @version 1.0.0
 * @since 2021/2/1
 */
public class LoadSubClassException extends RuntimeException {
    public LoadSubClassException() {
    }

    public LoadSubClassException(String message) {
        super(message);
    }

    public LoadSubClassException(Throwable cause) {
        super(cause);
    }
}
