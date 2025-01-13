package com.kama.minispring.aop;

/**
 * 基于表达式的切点接口
 * 用于获取和设置切点表达式
 * 
 * @author kama
 * @version 1.0.0
 */
public interface ExpressionPointcut extends Pointcut {
    
    /**
     * 获取切点表达式
     * 
     * @return 切点表达式
     */
    String getExpression();
    
    /**
     * 设置切点表达式
     * 
     * @param expression 切点表达式
     */
    void setExpression(String expression);
} 