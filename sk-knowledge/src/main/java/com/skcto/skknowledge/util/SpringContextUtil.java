package com.skcto.skknowledge.util;

import com.skcto.skknowledge.extract.FileExtract;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * spring上下文工具类
 */
@Component
public class SpringContextUtil implements ApplicationContextAware {

    //spring上下文
    private static ApplicationContext applicationContext;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextUtil.applicationContext = applicationContext;
    }

    /**
     * 获取spring容器中的bean对象
     */
    public static <T> T getBean(Class<T> clazz) {
        isInitialized();
        return applicationContext.getBean(clazz);
    }

    /**
     * 检查spring上下文是否初始化完成
     */
    public static void isInitialized() {
        if (applicationContext == null) {
            throw new IllegalStateException("spring容器未初始化完成");
        }
    }
}
