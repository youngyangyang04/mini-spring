package com.kama.minispring.tx;

import com.kama.minispring.beans.BeansException;

/**
 * 事务异常类
 * 封装事务操作中的异常
 *
 * @author kama
 * @version 1.0.0
 */
public class TransactionException extends BeansException {
    
    /**
     * 创建一个新的事务异常
     *
     * @param message 异常信息
     */
    public TransactionException(String message) {
        super(message);
    }
    
    /**
     * 创建一个新的事务异常
     *
     * @param message 异常信息
     * @param cause 异常原因
     */
    public TransactionException(String message, Throwable cause) {
        super(message, cause);
    }
} 