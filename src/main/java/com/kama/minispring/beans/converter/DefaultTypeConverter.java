package com.kama.minispring.beans.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 默认的类型转换器实现，支持基本类型和常用类型的转换
 *
 * @author kama
 * @version 1.0.0
 */
public class DefaultTypeConverter implements TypeConverter {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultTypeConverter.class);
    
    /** 日期格式 */
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    /** 类型转换函数映射 */
    private final Map<Class<?>, Function<String, ?>> converters = new HashMap<>();
    
    public DefaultTypeConverter() {
        // 注册基本类型转换器
        converters.put(Integer.class, Integer::valueOf);
        converters.put(int.class, Integer::parseInt);
        converters.put(Long.class, Long::valueOf);
        converters.put(long.class, Long::parseLong);
        converters.put(Double.class, Double::valueOf);
        converters.put(double.class, Double::parseDouble);
        converters.put(Float.class, Float::valueOf);
        converters.put(float.class, Float::parseFloat);
        converters.put(Boolean.class, Boolean::valueOf);
        converters.put(boolean.class, Boolean::parseBoolean);
        converters.put(Short.class, Short::valueOf);
        converters.put(short.class, Short::parseShort);
        converters.put(Byte.class, Byte::valueOf);
        converters.put(byte.class, Byte::parseByte);
        converters.put(Character.class, s -> s.charAt(0));
        converters.put(char.class, s -> s.charAt(0));
        
        // 注册其他常用类型转换器
        converters.put(BigDecimal.class, BigDecimal::new);
        converters.put(BigInteger.class, BigInteger::new);
        converters.put(String.class, String::valueOf);
        converters.put(Date.class, this::parseDate);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T convertIfNecessary(Object value, Class<T> requiredType) throws TypeMismatchException {
        if (value == null) {
            return null;
        }
        
        // 如果值已经是目标类型，直接返回
        if (requiredType.isInstance(value)) {
            return (T) value;
        }
        
        // 如果值是字符串，尝试转换
        if (value instanceof String) {
            String stringValue = (String) value;
            try {
                // 获取转换函数
                Function<String, ?> converter = converters.get(requiredType);
                if (converter != null) {
                    Object result = converter.apply(stringValue);
                    logger.debug("Converted string value '{}' to type '{}'", stringValue, requiredType.getName());
                    return (T) result;
                }
            } catch (Exception e) {
                throw new TypeMismatchException(value, requiredType, e);
            }
        }
        
        // 如果没有找到合适的转换器，抛出异常
        throw new TypeMismatchException(value, requiredType);
    }
    
    /**
     * 解析日期字符串
     *
     * @param dateStr 日期字符串
     * @return 日期对象
     */
    private Date parseDate(String dateStr) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
            return dateFormat.parse(dateStr);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Failed to parse date: " + dateStr, e);
        }
    }
    
    /**
     * 注册自定义类型转换器
     *
     * @param type 目标类型
     * @param converter 转换函数
     * @param <T> 目标类型的泛型参数
     */
    public <T> void registerConverter(Class<T> type, Function<String, T> converter) {
        converters.put(type, converter);
        logger.debug("Registered converter for type '{}'", type.getName());
    }
} 