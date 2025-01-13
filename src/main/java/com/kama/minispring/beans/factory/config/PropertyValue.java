package com.kama.minispring.beans.factory.config;

/**
 * bean属性值的封装类
 *
 * @author kama
 * @version 1.0.0
 */
public class PropertyValue {

    private final String name;
    private final Object value;
    private final Class<?> type;

    /**
     * 创建一个新的PropertyValue实例
     *
     * @param name 属性名称
     * @param value 属性值
     * @param type 属性类型
     */
    public PropertyValue(String name, Object value, Class<?> type) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Property name must not be null or empty");
        }
        this.name = name;
        this.value = value;
        this.type = type;
    }

    /**
     * 创建一个新的PropertyValue实例（不指定类型）
     *
     * @param name 属性名称
     * @param value 属性值
     */
    public PropertyValue(String name, Object value) {
        this(name, value, value != null ? value.getClass() : Object.class);
    }

    /**
     * 获取属性名称
     *
     * @return 属性名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * 获取属性值
     *
     * @return 属性值
     */
    public Object getValue() {
        return this.value;
    }

    /**
     * 获取属性类型
     *
     * @return 属性类型
     */
    public Class<?> getType() {
        return this.type;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PropertyValue)) {
            return false;
        }
        PropertyValue otherPv = (PropertyValue) other;
        return this.name.equals(otherPv.name);
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public String toString() {
        return "PropertyValue: name='" + this.name + "', value=[" + this.value + "]";
    }
} 