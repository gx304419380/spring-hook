package com.fly.spring.hook.test;

import javassist.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping
    public String changeBean() throws NotFoundException, CannotCompileException {

        // 类库池, jvm中所加载的class
        ClassPool pool = ClassPool.getDefault();

        // 获取指定的Student类
        String name = TestService.class.getName();
        CtClass father = pool.get(name);
        CtClass ctClass = pool.getAndRename(name, name + "$$sub");
        ctClass.setSuperclass(father);

        // 获取sayHello方法
        CtMethod ctMethod = ctClass.getDeclaredMethod("test");
        // 在方法的代码后追加 一段代码
//        ctMethod.insertAfter("System.out.println(\"I'm 100 years old.\");");
        ctMethod.setBody("{$1 = $1 + \"测试\";" +
                "\nreturn \"javassist sub test service\" + $1;}");

        // 使用当前的ClassLoader加载被修改后的类
        Class<?> subClass = ctClass.toClass(TestService.class.getClassLoader(), null);

        DefaultListableBeanFactory beanFactory = applicationContext.getDefaultListableBeanFactory();
        beanFactory.removeBeanDefinition("testService");
        RootBeanDefinition bd = new RootBeanDefinition(subClass);
        bd.setScope(SCOPE_SINGLETON);
        beanFactory.registerBeanDefinition("testService", bd);
        return "success";
    }

    @GetMapping("event")
    public String event() {
        applicationContext.publishEvent(new TestEvent("hello event"));
        return "event";
    }

}
