package com.kama.minispring.beans.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 默认类型转换器的测试类
 *
 * @author kama
 * @version 1.0.0
 */
public class DefaultTypeConverterTest {
    
    private DefaultTypeConverter converter;
    
    @BeforeEach
    void setUp() {
        converter = new DefaultTypeConverter();
    }
    
    @Test
    void testConvertPrimitiveTypes() {
        // 测试整数类型转换
        assertEquals(123, converter.convertIfNecessary("123", int.class));
        assertEquals(123, converter.convertIfNecessary("123", Integer.class));
        
        // 测试长整数类型转换
        assertEquals(123L, converter.convertIfNecessary("123", long.class));
        assertEquals(123L, converter.convertIfNecessary("123", Long.class));
        
        // 测试浮点数类型转换
        assertEquals(123.45, converter.convertIfNecessary("123.45", double.class));
        assertEquals(123.45, converter.convertIfNecessary("123.45", Double.class));
        
        // 测试布尔类型转换
        assertTrue(converter.convertIfNecessary("true", boolean.class));
        assertTrue(converter.convertIfNecessary("true", Boolean.class));
        
        // 测试字符类型转换
        assertEquals('A', converter.convertIfNecessary("A", char.class));
        assertEquals('A', converter.convertIfNecessary("A", Character.class));
    }
    
    @Test
    void testConvertBigNumbers() {
        // 测试BigDecimal转换
        BigDecimal decimal = converter.convertIfNecessary("123.45", BigDecimal.class);
        assertEquals(new BigDecimal("123.45"), decimal);
        
        // 测试BigInteger转换
        BigInteger integer = converter.convertIfNecessary("123", BigInteger.class);
        assertEquals(new BigInteger("123"), integer);
    }
    
    @Test
    void testConvertDate() {
        // 测试日期转换
        Date date = converter.convertIfNecessary("2023-12-25 12:34:56", Date.class);
        assertNotNull(date);
    }
    
    @Test
    void testConvertWithNullValue() {
        // 测试null值转换
        assertNull(converter.convertIfNecessary(null, String.class));
        assertNull(converter.convertIfNecessary(null, Integer.class));
    }
    
    @Test
    void testConvertWithInvalidValue() {
        // 测试无效值转换
        assertThrows(TypeMismatchException.class, () -> {
            converter.convertIfNecessary("abc", Integer.class);
        });
        
        assertThrows(TypeMismatchException.class, () -> {
            converter.convertIfNecessary("2023-13-45", Date.class);
        });
    }
    
    @Test
    void testRegisterCustomConverter() {
        // 注册自定义转换器
        converter.registerConverter(CustomType.class, CustomType::new);
        
        // 测试自定义转换器
        CustomType result = converter.convertIfNecessary("test", CustomType.class);
        assertEquals("test", result.getValue());
    }
    
    /**
     * 用于测试的自定义类型
     */
    static class CustomType {
        private final String value;
        
        public CustomType(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
} 