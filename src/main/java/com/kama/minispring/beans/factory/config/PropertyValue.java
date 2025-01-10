package com.kama.minispring.beans.factory.config;

/**
 * 属性值的封装类，用于setter注入
 *
 * @author kama
 * @version 1.0.0
 */
public class PropertyValue {
    private final String name;
    private final Object value;
    private final Class<?> type;

    /**
     * 创建一个属性值
     *
     * @param name 属性名称
     * @param value 属性值
     * @param type 属性类型
     */
    public PropertyValue(String name, Object value, Class<?> type) {
        this.name = name;
        this.value = value;
        this.type = type;
    }

    /**
     * 获取属性名称
     *
     * @return 属性名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取属性值
     *
     * @return 属性值
     */
    public Object getValue() {
        return value;
    }

    /**
     * 获取属性类型
     *
     * @return 属性类型
     */
    public Class<?> getType() {
        return type;
    }
} 