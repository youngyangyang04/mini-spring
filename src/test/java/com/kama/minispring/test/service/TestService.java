package com.kama.minispring.test.service;

import com.kama.minispring.stereotype.Service;

/**
 * 测试服务类
 *
 * @author kama
 * @version 1.0.0
 */
@Service
public class TestService {
    
    public String sayHello() {
        return "Hello from TestService!";
    }
} 