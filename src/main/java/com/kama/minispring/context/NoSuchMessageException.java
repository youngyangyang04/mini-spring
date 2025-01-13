package com.kama.minispring.context;

import com.kama.minispring.beans.BeansException;
import java.util.Locale;

/**
 * 当消息不存在时抛出的异常
 * 
 * @author kama
 * @version 1.0.0
 */
public class NoSuchMessageException extends BeansException {
    
    private static final long serialVersionUID = 1L;

    /**
     * 构造函数
     *
     * @param code 消息代码
     */
    public NoSuchMessageException(String code) {
        super("No message found under code '" + code + "'");
    }

    /**
     * 构造函数
     *
     * @param code 消息代码
     * @param locale 区域信息
     */
    public NoSuchMessageException(String code, Locale locale) {
        super("No message found under code '" + code + "' for locale '" + locale + "'");
    }
} 