package com.kama.minispring.context;

import java.util.Locale;

/**
 * 国际化消息源接口
 * 定义了获取国际化消息的核心方法
 * 
 * @author kama
 * @version 1.0.0
 */
public interface MessageSource {
    
    /**
     * 获取消息,如果没找到返回默认消息
     *
     * @param code 消息代码
     * @param args 参数数组
     * @param defaultMessage 默认消息
     * @param locale 区域信息
     * @return 解析后的消息
     */
    String getMessage(String code, Object[] args, String defaultMessage, Locale locale);
    
    /**
     * 获取消息,如果没找到抛出NoSuchMessageException
     *
     * @param code 消息代码
     * @param args 参数数组
     * @param locale 区域信息
     * @return 解析后的消息
     * @throws NoSuchMessageException 如果没找到消息
     */
    String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException;
    
    /**
     * 获取消息,使用MessageSourceResolvable
     *
     * @param resolvable 包含消息解析信息的对象
     * @param locale 区域信息
     * @return 解析后的消息
     * @throws NoSuchMessageException 如果没找到消息
     */
    String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException;
} 