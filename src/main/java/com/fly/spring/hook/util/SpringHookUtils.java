package com.fly.spring.hook.util;


import javassist.*;

import java.lang.reflect.Constructor;

/**
 * @author guoxiang
 * @version 1.0.0
 * @since 2021/2/1
 * @apiNote 一个用于将逻辑织入指定方法的工具类
 */
public class SpringHookUtils {






    /**
     * @param mother    母体类
     * @param parasite  寄生类
     */
    public static void operate(Class<?> mother, Class<?> parasite) {

        // TODO: 2021/2/1 实例化parasite，然后将spring上下文中的bean赋值给他内部
        Constructor<?>[] constructors = parasite.getConstructors();



        // TODO: 2021/2/1 找到要寄生的方法list



    }


    public static void doOperate(Class<?> mother,
                                 Class<?> parasite,
                                 Object parasiteInstance,
                                 String method) throws NotFoundException, CannotCompileException {
        // 类库池, jvm中所加载的class
        ClassPool pool = ClassPool.getDefault();
        // 获取指定的Student类
        CtClass ctClass = pool.get(mother.getName());
        // 获取sayHello方法
        CtMethod ctMethod = ctClass.getDeclaredMethod(method);
        // 在方法的代码后追加 一段代码
        ctMethod.insertAfter("System.out.println(\"I'm \" + this.age + \" years old.\");");
        // 使用当前的ClassLoader加载被修改后的类
        ctClass.toClass(mother.getClassLoader(), null);
    }

}
