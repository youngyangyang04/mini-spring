package com.kama.minispring.util;

/**
 * 类操作工具类
 *
 * @author kama
 * @version 1.0.0
 */
public class ClassUtils {
    
    /** 数组类名后缀 */
    public static final String ARRAY_SUFFIX = "[]";
    /** 内部类分隔符 */
    private static final String INNER_CLASS_SEPARATOR = "$";
    /** 包分隔符 */
    private static final String PACKAGE_SEPARATOR = ".";
    
    /**
     * 获取默认的类加载器
     *
     * @return 默认的类加载器
     */
    public static ClassLoader getDefaultClassLoader() {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        }
        catch (Throwable ex) {
            // Cannot access thread context ClassLoader - falling back...
        }
        if (cl == null) {
            // No thread context class loader -> use class loader of this class.
            cl = ClassUtils.class.getClassLoader();
            if (cl == null) {
                // getClassLoader() returning null indicates the bootstrap ClassLoader
                try {
                    cl = ClassLoader.getSystemClassLoader();
                }
                catch (Throwable ex) {
                    // Cannot access system ClassLoader - oh well, maybe the caller can live with null...
                }
            }
        }
        return cl;
    }
    
    /**
     * 获取类的包名
     *
     * @param className 类名
     * @return 包名
     */
    public static String getPackageName(String className) {
        int lastDotIndex = className.lastIndexOf(PACKAGE_SEPARATOR);
        return (lastDotIndex != -1 ? className.substring(0, lastDotIndex) : "");
    }
    
    /**
     * 获取类的短名称
     *
     * @param className 类名
     * @return 短名称
     */
    public static String getShortName(String className) {
        int lastDotIndex = className.lastIndexOf(PACKAGE_SEPARATOR);
        int nameEndIndex = className.indexOf(ARRAY_SUFFIX);
        if (nameEndIndex == -1) {
            nameEndIndex = className.length();
        }
        String shortName = className.substring(lastDotIndex + 1, nameEndIndex);
        shortName = shortName.replace(INNER_CLASS_SEPARATOR, PACKAGE_SEPARATOR);
        return shortName;
    }
    
    /**
     * 判断是否是内部类
     *
     * @param className 类名
     * @return 如果是内部类返回true，否则返回false
     */
    public static boolean isInnerClass(String className) {
        return className.contains(INNER_CLASS_SEPARATOR);
    }
} 