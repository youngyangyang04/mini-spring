package com.kama.minispring.aop.aspectj;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * AspectJ表达式切点测试类
 *
 * @author kama
 * @version 1.0.0
 */
public class AspectJExpressionPointcutTest {

    @Test
    public void testExecutionExpression() throws Exception {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("execution(* com.kama.minispring.aop.aspectj.AspectJExpressionPointcutTest.*(..))");
        
        assertTrue(pointcut.matches(AspectJExpressionPointcutTest.class));
        assertTrue(pointcut.matches(
            AspectJExpressionPointcutTest.class.getDeclaredMethod("testExecutionExpression"),
            AspectJExpressionPointcutTest.class
        ));
    }

    @Test
    public void testMethodMatchWithArgs() throws Exception {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("execution(* com.kama.minispring.aop.aspectj.AspectJExpressionPointcutTest.testMethodMatch*(..))");
        
        assertTrue(pointcut.matches(
            AspectJExpressionPointcutTest.class.getDeclaredMethod("testMethodMatchWithArgs"),
            AspectJExpressionPointcutTest.class
        ));
        
        assertFalse(pointcut.matches(
            AspectJExpressionPointcutTest.class.getDeclaredMethod("testExecutionExpression"),
            AspectJExpressionPointcutTest.class
        ));
    }
} 