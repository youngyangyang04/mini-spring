package com.kama.minispring.context.support;

import com.kama.minispring.context.MessageSource;
import com.kama.minispring.context.MessageSourceResolvable;
import com.kama.minispring.context.NoSuchMessageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.Locale;

/**
 * MessageSource接口的抽象实现
 * 提供了消息格式化和缓存的基础功能
 * 
 * @author kama
 * @version 1.0.0
 */
public abstract class AbstractMessageSource implements MessageSource {
    
    private static final Logger logger = LoggerFactory.getLogger(AbstractMessageSource.class);
    
    private MessageSource parentMessageSource;
    private boolean useCodeAsDefaultMessage = false;

    @Override
    public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        String msg = getMessageInternal(code, args, locale);
        if (msg != null) {
            return msg;
        }
        
        if (defaultMessage == null && useCodeAsDefaultMessage) {
            defaultMessage = code;
        }
        return defaultMessage;
    }

    @Override
    public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
        String msg = getMessageInternal(code, args, locale);
        if (msg != null) {
            return msg;
        }
        
        if (useCodeAsDefaultMessage) {
            return code;
        }
        throw new NoSuchMessageException(code, locale);
    }

    @Override
    public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
        String[] codes = resolvable.getCodes();
        if (codes == null) {
            codes = new String[0];
        }
        
        for (String code : codes) {
            String msg = getMessageInternal(code, resolvable.getArguments(), locale);
            if (msg != null) {
                return msg;
            }
        }
        
        if (useCodeAsDefaultMessage && codes.length > 0) {
            return codes[0];
        }
        
        if (resolvable.getDefaultMessage() != null) {
            return resolvable.getDefaultMessage();
        }
        
        if (codes.length > 0) {
            throw new NoSuchMessageException(codes[0], locale);
        }
        
        throw new NoSuchMessageException("No message", locale);
    }
    
    /**
     * 获取消息的内部方法
     * 由子类实现具体的消息解析逻辑
     */
    protected abstract String resolveMessage(String code, Locale locale);
    
    /**
     * 获取消息并进行参数格式化
     */
    protected String getMessageInternal(String code, Object[] args, Locale locale) {
        String message = resolveMessage(code, locale);
        if (message == null && parentMessageSource != null) {
            message = parentMessageSource.getMessage(code, args, null, locale);
        }
        if (message != null && args != null) {
            return formatMessage(message, args, locale);
        }
        return message;
    }
    
    /**
     * 使用MessageFormat格式化消息
     */
    protected String formatMessage(String msg, Object[] args, Locale locale) {
        if (msg == null || args == null || args.length == 0) {
            return msg;
        }
        msg = msg.trim();
        MessageFormat messageFormat = new MessageFormat(msg, locale);
        return messageFormat.format(args);
    }
    
    /**
     * 设置父消息源
     */
    public void setParentMessageSource(MessageSource parent) {
        this.parentMessageSource = parent;
    }
    
    /**
     * 获取父消息源
     */
    public MessageSource getParentMessageSource() {
        return this.parentMessageSource;
    }
    
    /**
     * 设置是否使用消息代码作为默认消息
     */
    public void setUseCodeAsDefaultMessage(boolean useCodeAsDefaultMessage) {
        this.useCodeAsDefaultMessage = useCodeAsDefaultMessage;
    }
    
    /**
     * 是否使用消息代码作为默认消息
     */
    public boolean isUseCodeAsDefaultMessage() {
        return this.useCodeAsDefaultMessage;
    }
} 