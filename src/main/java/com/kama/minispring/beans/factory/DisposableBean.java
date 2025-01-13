package com.kama.minispring.beans.factory;

/**
 * 定义bean销毁时的行为
 */
public interface DisposableBean {
    /**
     * 销毁时调用
     */
    void destroy() throws Exception;
} 