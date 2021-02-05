package com.fly.spring.hook.controller;

import com.fly.spring.hook.entity.BeanInfo;
import com.fly.spring.hook.entity.HookMethodDto;
import com.fly.spring.hook.test.TestEvent;
import com.fly.spring.hook.util.SpringHookContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static com.fly.spring.hook.util.ObjectUtils.isEmpty;
import static com.fly.spring.hook.util.ObjectUtils.notEmpty;
import static java.util.stream.Collectors.toList;

/**
 * @author guoxiang
 * @version 1.0.0
 * @since 2021/2/3
 */
@RestController
@RequestMapping("/spring/hook")
public class SpringHookController {

    private static final Logger log = LoggerFactory.getLogger(SpringHookController.class);

    @Autowired
    private GenericApplicationContext applicationContext;

    @Autowired
    private SpringHookContext springHookContext;


    /**
     * 上传class文件，替换bean
     *
     * @param beanName  bean名称
     * @param file      class文件
     * @return          信息
     * @throws IOException  异常
     */
    @PostMapping("replaceBean")
    public String replaceBean(@RequestParam String beanName,
                              @RequestPart MultipartFile file) throws IOException {
        log.info("- replace bean: {}, class file: {}", beanName, file.getOriginalFilename());

        springHookContext.replaceBeanByClass(beanName, file.getInputStream());

        log.info("- replace bean: {} finished...", beanName);
        return "success";
    }


    /**
     * 直接修改某个bean的指定方法
     * @param dto   dto
     * @return      执行信息
     */
    @PostMapping("replaceMethod")
    public String replaceMethod(@RequestBody HookMethodDto dto) {

        log.info("- replace method: {}", dto);

        springHookContext.replaceBeanByMethod(dto);

        log.info("- replace method: {}.{} finished...", dto.getBeanName(), dto.getMethodName());
        return "success";
    }


    /**
     * 获取当前bean列表
     * @param name          根据名称查询
     * @param packageName   根据包名查询
     * @return              bean list
     */
    @GetMapping("bean")
    public List<String> getBeanNameList(@RequestParam(required = false) String name,
                                        @RequestParam(required = false) String packageName) {

        String[] names = applicationContext.getBeanDefinitionNames();

        Stream<String> stream = Stream.of(names).sorted();

        if (notEmpty(name)) {
            stream = stream.filter(n -> n.contains(name));
        }

        if (notEmpty(packageName)) {
            stream = stream.filter(bean -> inPackage(bean, packageName));
        }

        return stream.collect(toList());
    }


    private boolean inPackage(String bean, String packageName) {

        BeanInfo beanInfo = springHookContext.getBeanInfo(bean);
        return beanInfo.getTargetClass().getName().startsWith(packageName);
    }

}
