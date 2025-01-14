package com.kama.minispring.core.type;

/**
 * 用于获取类的基本信息的接口
 * 提供访问类的名称、修饰符等基本信息的能力
 *
 * @author kama
 * @version 1.0.0
 */
public interface ClassMetadata {
    
    /**
     * 获取类名
     *
     * @return 类的全限定名
     */
    String getClassName();
    
    /**
     * 判断是否是接口
     *
     * @return 如果是接口返回true，否则返回false
     */
    boolean isInterface();
    
    /**
     * 判断是否是抽象类
     *
     * @return 如果是抽象类返回true，否则返回false
     */
    boolean isAbstract();
    
    /**
     * 判断是否是具体类
     *
     * @return 如果是具体类返回true，否则返回false
     */
    boolean isConcrete();
    
    /**
     * 获取父类名
     *
     * @return 父类的全限定名，如果没有父类则返回null
     */
    String getSuperClassName();
    
    /**
     * 获取实现的接口名称
     *
     * @return 实现的接口全限定名数组
     */
    String[] getInterfaceNames();
} 