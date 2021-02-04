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


    @PostMapping("replace")
    public String replace(@RequestParam String beanName,
                          @RequestPart MultipartFile file) throws IOException {
        springHookContext.replace(beanName, file.getInputStream());
        return "success";
    }


    @GetMapping
    public String changeBean() throws NotFoundException, CannotCompileException {

        // 类库池, jvm中所加载的class
        ClassPool pool = ClassPool.getDefault();

        // 获取指定的类
        String name = TestService.class.getName();
        CtClass father = pool.get(name);
        CtClass ctClass = pool.getAndRename(name, name + "$$sub");
        ctClass.setSuperclass(father);

        // 获取sayHello方法
        CtMethod ctMethod = ctClass.getDeclaredMethod("test");
        // 修改方法
        ctClass.removeMethod(ctMethod);
        CtMethod testMethod = CtNewMethod.make("public String test(String param) {\n" +
                "return \"sub test service: \" + param;\n" +
                "}", ctClass);

        ctClass.addMethod(testMethod);

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
