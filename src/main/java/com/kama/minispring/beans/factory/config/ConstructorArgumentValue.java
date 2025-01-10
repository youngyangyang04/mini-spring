package com.kama.minispring.beans.factory.config;

/**
 * 构造器参数值的封装类
 *
 * @author kama
 * @version 1.0.0
 */
public class ConstructorArgumentValue {
    private final Object value;
    private final Class<?> type;
    private final String name;

    /**
     * 创建一个构造器参数值
     *
     * @param value 参数值
     * @param type 参数类型
     * @param name 参数名称
     */
    public ConstructorArgumentValue(Object value, Class<?> type, String name) {
        this.value = value;
        this.type = type;
        this.name = name;
    }

    /**
     * 获取参数值
     *
     * @return 参数值
     */
    public Object getValue() {
        return value;
    }

    /**
     * 获取参数类型
     *
     * @return 参数类型
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * 获取参数名称
     *
     * @return 参数名称
     */
    public String getName() {
        return name;
    }
} 