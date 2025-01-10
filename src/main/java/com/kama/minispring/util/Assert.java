package com.kama.minispring.util;

/**
 * 断言工具类，用于参数校验
 *
 * @author kama
 * @version 1.0.0
 */
public abstract class Assert {
    
    /**
     * 断言对象不为null
     *
     * @param object 要检查的对象
     * @param message 异常消息
     * @throws IllegalArgumentException 如果对象为null
     */
    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }
    
    /**
     * 断言字符串不为空
     *
     * @param text 要检查的字符串
     * @param message 异常消息
     * @throws IllegalArgumentException 如果字符串为null或空
     */
    public static void hasText(String text, String message) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }
    
    /**
     * 断言表达式为true
     *
     * @param expression 要检查的表达式
     * @param message 异常消息
     * @throws IllegalArgumentException 如果表达式为false
     */
    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new IllegalArgumentException(message);
        }
    }
} 