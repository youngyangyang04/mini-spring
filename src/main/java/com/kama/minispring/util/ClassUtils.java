package com.kama.minispring.util;

/**
 * 类工具类
 *
 * @author kama
 * @version 1.0.0
 */
public abstract class ClassUtils {
    
    /** 默认类加载器数组 */
    private static final ClassLoader[] EMPTY_CLASS_LOADER_ARRAY = new ClassLoader[0];
    
    /**
     * 获取默认的类加载器
     * 优先使用当前线程的上下文类加载器，其次使用加载当前类的类加载器，最后使用系统类加载器
     *
     * @return 类加载器
     */
    public static ClassLoader getDefaultClassLoader() {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ex) {
            // 无法访问线程上下文类加载器，忽略异常
        }
        if (cl == null) {
            // 使用加载当前类的类加载器
            cl = ClassUtils.class.getClassLoader();
            if (cl == null) {
                // 使用系统类加载器
                try {
                    cl = ClassLoader.getSystemClassLoader();
                } catch (Throwable ex) {
                    // 无法访问系统类加载器，忽略异常
                }
            }
        }
        return cl;
    }
    
    /**
     * 获取类的包名
     *
     * @param clazz 类
     * @return 包名
     */
    public static String getPackageName(Class<?> clazz) {
        Assert.notNull(clazz, "Class must not be null");
        return getPackageName(clazz.getName());
    }
    
    /**
     * 获取类名的包名部分
     *
     * @param fqClassName 完全限定的类名
     * @return 包名
     */
    public static String getPackageName(String fqClassName) {
        Assert.notNull(fqClassName, "Class name must not be null");
        int lastDotIndex = fqClassName.lastIndexOf('.');
        return (lastDotIndex != -1 ? fqClassName.substring(0, lastDotIndex) : "");
    }
    
    /**
     * 判断类是否是内部类
     *
     * @param clazz 类
     * @return 如果是内部类返回true，否则返回false
     */
    public static boolean isInnerClass(Class<?> clazz) {
        return (clazz != null && clazz.isMemberClass() && !isStaticClass(clazz));
    }
    
    /**
     * 判断类是否是静态类
     *
     * @param clazz 类
     * @return 如果是静态类返回true，否则返回false
     */
    public static boolean isStaticClass(Class<?> clazz) {
        return (clazz != null && clazz.getModifiers() == java.lang.reflect.Modifier.STATIC);
    }
} 