package com.kama.minispring.util;

/**
 * 字符串工具类
 *
 * @author kama
 * @version 1.0.0
 */
public abstract class StringUtils {
    
    /**
     * 清理路径字符串
     * 将路径中的反斜杠替换为正斜杠，移除重复的分隔符
     *
     * @param path 要清理的路径
     * @return 清理后的路径
     */
    public static String cleanPath(String path) {
        if (path == null) {
            return null;
        }
        
        // 将反斜杠替换为正斜杠
        String pathToUse = path.replace('\\', '/');
        
        // 移除重复的分隔符
        while (pathToUse.contains("//")) {
            pathToUse = pathToUse.replace("//", "/");
        }
        
        return pathToUse;
    }
    
    /**
     * 获取路径中的文件名
     * 返回路径中最后一个分隔符后的部分
     *
     * @param path 路径
     * @return 文件名，如果路径为null或没有文件名则返回null
     */
    public static String getFilename(String path) {
        if (path == null) {
            return null;
        }
        
        int separatorIndex = path.lastIndexOf('/');
        return (separatorIndex != -1 ? path.substring(separatorIndex + 1) : path);
    }
    
    /**
     * 判断字符串是否为空
     *
     * @param str 要检查的字符串
     * @return 如果字符串为null或空字符串返回true，否则返回false
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }
    
    /**
     * 判断字符串是否有内容（不为null且不为空字符串）
     *
     * @param str 要检查的字符串
     * @return 如果字符串有内容返回true，否则返回false
     */
    public static boolean hasLength(String str) {
        return str != null && !str.isEmpty();
    }
    
    /**
     * 判断字符串是否有文本内容（不为null且包含非空白字符）
     *
     * @param str 要检查的字符串
     * @return 如果字符串有文本内容返回true，否则返回false
     */
    public static boolean hasText(String str) {
        return str != null && !str.trim().isEmpty();
    }
} 