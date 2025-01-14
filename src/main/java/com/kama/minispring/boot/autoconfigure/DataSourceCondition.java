package com.kama.minispring.boot.autoconfigure;

import com.kama.minispring.context.annotation.Condition;
import com.kama.minispring.context.annotation.ConditionContext;

/**
 * 数据源条件类，用于判断是否需要创建数据源
 * 
 * @author kama
 * @version 1.0.0
 */
public class DataSourceCondition implements Condition {
    
    @Override
    public boolean matches(ConditionContext context) {
        String driverClassName = context.getEnvironment()
            .getProperty("spring.datasource.driver-class-name");
        return driverClassName != null && !driverClassName.isEmpty();
    }
} 