package com.business.business.util;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.stereotype.Component;

/**
 * Description: business
 * <p>
 * Created by w_kiven on 2020/12/2 17:42
 */
@Component
public class SpringContextUtil implements BeanFactoryAware {
    private static BeanFactory beanFactory;
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        SpringContextUtil.beanFactory =beanFactory;
    }
    public static <T> T getBean(String beanName) {
        if (null != beanFactory) {
            return (T) beanFactory.getBean(beanName);
        }
        return null;
    }
}
