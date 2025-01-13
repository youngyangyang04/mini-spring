package com.kama.minispring.aop;

/**
 * 目标对象源
 * 封装目标对象的类型和实例
 * 
 * @author kama
 * @version 1.0.0
 */
public class TargetSource {
    
    private final Object target;
    
    /**
     * 构造函数
     * 
     * @param target 目标对象
     */
    public TargetSource(Object target) {
        this.target = target;
    }
    
    /**
     * 获取目标对象的类型
     * 
     * @return 目标对象的类型
     */
    public Class<?> getTargetClass() {
        return target.getClass();
    }
    
    /**
     * 获取目标对象
     * 
     * @return 目标对象
     */
    public Object getTarget() {
        return target;
    }
} 