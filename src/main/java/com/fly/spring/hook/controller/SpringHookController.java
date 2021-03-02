package com.fly.spring.hook.controller;

import com.fly.spring.hook.entity.BeanDto;
import com.fly.spring.hook.entity.BeanInfo;
import com.fly.spring.hook.entity.HookMethodDto;
import com.fly.spring.hook.util.SpringHookContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static com.fly.spring.hook.util.ObjectUtils.isEmpty;
import static com.fly.spring.hook.util.ObjectUtils.notEmpty;
import static java.util.stream.Collectors.toList;

/**
 * @author guoxiang
 * @version 1.0.0
 * @since 2021/2/3
 */
@ResponseBody
@RequestMapping("/spring/hook")
public class SpringHookController {

    private static final Logger log = LoggerFactory.getLogger(SpringHookController.class);

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

        String result = springHookContext.replaceBeanByClass(beanName, file.getInputStream());

        log.info("- replace bean: {} finished...", beanName);
        return result;
    }


    /**
     * 直接修改某个bean的指定方法
     * @param dto   dto
     * @return      执行信息
     */
    @PostMapping("replaceMethod")
    public String replaceMethod(@RequestBody HookMethodDto dto) {

        log.info("- replace method: {}", dto);

        String result = springHookContext.replaceBeanByMethod(dto);

        log.info("- replace method: {}.{} finished...", dto.getBeanName(), dto.getMethodName());
        return result;
    }

    @GetMapping("bean/detail/{beanName}")
    public BeanDto getBeanDetail(@PathVariable String beanName) {
        BeanInfo beanInfo = springHookContext.getBeanInfo(beanName);

        return new BeanDto(beanInfo);
    }

    /**
     * 获取当前bean列表
     * @param beanName       根据名称查询
     * @param className     根据包名查询
     * @return              bean list
     */
    @GetMapping("bean")
    public List<BeanDto> getBeanNameList(@RequestParam(required = false) String beanName,
                                         @RequestParam(required = false) String className) {

        Stream<String> stream = springHookContext.getBeanNameStream();

        if (notEmpty(beanName)) {
            stream = stream.filter(n -> n.toLowerCase().contains(beanName.toLowerCase()));
        }

        Stream<BeanDto> infoStream = stream.map(this::getBeanDto);

        if (notEmpty(className)) {
            infoStream = infoStream.filter(bean -> classNameLike(bean, className));
        }

        return infoStream.collect(toList());
    }

    private BeanDto getBeanDto(String name) {
        BeanInfo beanInfo = springHookContext.getBeanInfo(name);
        return new BeanDto(name, beanInfo.getTargetClass().getName());
    }


    private boolean classNameLike(BeanDto bean, String className) {
        return bean.getClassName().toLowerCase().contains(className.toLowerCase());
    }

}
