package com.kama.minispring.beans.converter;

/**
 * 类型转换器接口，定义将一个类型转换为另一个类型的基本功能
 *
 * @author kama
 * @version 1.0.0
 */
public interface TypeConverter {
    
    /**
     * 将给定的值转换为指定的类型
     *
     * @param value 要转换的值
     * @param requiredType 目标类型
     * @param <T> 目标类型的泛型参数
     * @return 转换后的值
     * @throws TypeMismatchException 如果无法进行转换
     */
    <T> T convertIfNecessary(Object value, Class<T> requiredType) throws TypeMismatchException;
} 