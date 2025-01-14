package com.kama.minispring.context.annotation;

/**
 * 条件接口，用于判断是否满足特定条件
 * 
 * @author kama
 * @version 1.0.0
 */
public interface Condition {
    
    /**
     * 判断条件是否满足
     *
     * @param context 条件上下文
     * @return 如果条件满足返回true，否则返回false
     */
    boolean matches(ConditionContext context);
} 