package com.kama.minispring.tx;

/**
 * 非法事务状态异常
 * 当事务状态与期望的状态不符时抛出此异常
 *
 * @author kama
 * @version 1.0.0
 */
public class IllegalTransactionStateException extends TransactionException {
    
    /**
     * 使用指定的错误消息构造新的异常
     *
     * @param message 错误消息
     */
    public IllegalTransactionStateException(String message) {
        super(message);
    }
    
    /**
     * 使用指定的错误消息和根异常构造新的异常
     *
     * @param message 错误消息
     * @param cause 根异常
     */
    public IllegalTransactionStateException(String message, Throwable cause) {
        super(message, cause);
    }
} 