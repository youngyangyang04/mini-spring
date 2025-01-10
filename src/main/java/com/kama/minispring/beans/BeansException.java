package com.kama.minispring.beans;

/**
 * Spring Bean异常的基类
 *
 * @author kama
 * @version 1.0.0
 */
public class BeansException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 创建一个空的BeansException
     */
    public BeansException() {
        super();
    }
    
    /**
     * 创建一个带有错误信息的BeansException
     *
     * @param message 错误信息
     */
    public BeansException(String message) {
        super(message);
    }
    
    /**
     * 创建一个带有错误信息和原因的BeansException
     *
     * @param message 错误信息
     * @param cause 原因
     */
    public BeansException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * 创建一个带有原因的BeansException
     *
     * @param cause 原因
     */
    public BeansException(Throwable cause) {
        super(cause);
    }
} 