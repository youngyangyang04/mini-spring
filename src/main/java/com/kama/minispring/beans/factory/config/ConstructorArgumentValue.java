package com.kama.minispring.beans.factory.config;

/**
 * 封装构造函数参数
 */
public class ConstructorArgumentValue {
    private final Object value;
    private final Class<?> type;
    private final String name;

    public ConstructorArgumentValue(Object value) {
        this(value, null, null);
    }

    public ConstructorArgumentValue(Object value, Class<?> type) {
        this(value, type, null);
    }

    public ConstructorArgumentValue(Object value, Class<?> type, String name) {
        this.value = value;
        this.type = type;
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public Class<?> getType() {
        return type;
    }

    public String getName() {
        return name;
    }
} 