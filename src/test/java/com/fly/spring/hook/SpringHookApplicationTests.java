package com.fly.spring.hook;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Method;

class SpringHookApplicationTests {

    @Test
    void contextLoads() throws NoSuchMethodException {

        System.out.println("hello");

        Method testA = A.class.getMethod("test");
        Method testB = B.class.getMethod("test");

        System.out.println(testA.equals(testB));
    }


    class A {
        int a;
        public void test() {
            System.out.println("AAA");
        }
    }

    class B extends A {
        int b;

        public void test() {
            System.out.println("BBB");
        }
    }
}
