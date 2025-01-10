package com.kama.minispring.beans.converter;

import com.kama.minispring.beans.BeansException;

/**
 * 类型不匹配异常，当无法进行类型转换时抛出
 *
 * @author kama
 * @version 1.0.0
 */
public class TypeMismatchException extends BeansException {
    
    private final Class<?> requiredType;
    private final Object value;
    
    /**
     * 创建一个类型不匹配异常
     *
     * @param value 要转换的值
     * @param requiredType 目标类型
     */
    public TypeMismatchException(Object value, Class<?> requiredType) {
        super("Failed to convert value '" + value + "' to type '" + requiredType.getName() + "'");
        this.value = value;
        this.requiredType = requiredType;
    }
    
    /**
     * 创建一个类型不匹配异常
     *
     * @param value 要转换的值
     * @param requiredType 目标类型
     * @param cause 导致转换失败的原因
     */
    public TypeMismatchException(Object value, Class<?> requiredType, Throwable cause) {
        super("Failed to convert value '" + value + "' to type '" + requiredType.getName() + "'", cause);
        this.value = value;
        this.requiredType = requiredType;
    }
    
    /**
     * 获取目标类型
     *
     * @return 目标类型
     */
    public Class<?> getRequiredType() {
        return requiredType;
    }
    
    /**
     * 获取要转换的值
     *
     * @return 要转换的值
     */
    public Object getValue() {
        return value;
    }
} 