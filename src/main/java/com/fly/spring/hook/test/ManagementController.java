package com.fly.spring.hook.test;

import com.fly.spring.hook.util.SpringHookContext;
import javassist.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;

/**
 * @author guoxiang
 * @version 1.0.0
 * @since 2021/2/3
 */
@RestController
@RequestMapping("management")
public class ManagementController {

    @Autowired
    private GenericApplicationContext applicationContext;

    @Autowired
    private SpringHookContext springHookContext;


    @PostMapping("replaceBean")
    public String replace(@RequestParam String beanName,
                          @RequestPart MultipartFile file) throws IOException {
        springHookContext.replaceBeanByClass(beanName, file.getInputStream());
        return "success";
    }


    @PostMapping("replaceMethod")
    public String replaceMethod(@RequestParam String beanName,
                                @RequestParam String methodName,
                                @RequestParam String methodCode) {
        springHookContext.replaceBeanByMethod(beanName, methodName, methodCode);
        return "success";
    }


    @GetMapping("event")
    public String event() {
        applicationContext.publishEvent(new TestEvent("hello event"));
        return "event";
    }

}
