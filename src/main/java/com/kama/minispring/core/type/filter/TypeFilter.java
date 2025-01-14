package com.kama.minispring.core.type.filter;

import com.kama.minispring.core.type.ClassMetadata;

/**
 * 类型过滤器接口
 * 用于在类路径扫描时过滤特定的类
 *
 * @author kama
 * @version 1.0.0
 */
public interface TypeFilter {
    
    /**
     * 判断给定的类是否匹配过滤条件
     *
     * @param metadata 类的元数据
     * @return 如果匹配返回true，否则返回false
     */
    boolean match(ClassMetadata metadata);
} 