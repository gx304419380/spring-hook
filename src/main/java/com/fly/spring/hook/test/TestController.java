package com.fly.spring.hook.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

/**
 * @author guoxiang
 * @version 1.0.0
 * @since 2021/2/3
 */
@RestController
@RequestMapping("test")
public class TestController {

    private final TestService testService;

    public TestController(TestService testService) {
        this.testService = testService;
    }

    @PostMapping
    public String test(String param) {
        return testService.test(param);
    }

    @GetMapping("hello")
    public String hi() {
        return "hello sub CCC";
    }

    @PostConstruct
    public void init() {
        System.out.println("CCCCC");
    }

}
