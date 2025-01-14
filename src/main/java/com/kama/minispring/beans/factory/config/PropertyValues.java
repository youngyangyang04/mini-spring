package com.kama.minispring.beans.factory.config;

import java.util.ArrayList;
import java.util.List;

/**
 * bean属性值的集合封装类
 *
 * @author kama
 * @version 1.0.0
 */
public class PropertyValues {
    
    private final List<PropertyValue> propertyValueList = new ArrayList<>();
    
    public void addPropertyValue(PropertyValue propertyValue) {
        // 检查是否已存在同名属性
        for (int i = 0; i < propertyValueList.size(); i++) {
            PropertyValue currentValue = propertyValueList.get(i);
            if (currentValue.getName().equals(propertyValue.getName())) {
                // 如果存在，替换它
                propertyValueList.set(i, propertyValue);
                return;
            }
        }
        // 如果不存在，添加到列表
        propertyValueList.add(propertyValue);
    }
    
    public List<PropertyValue> getPropertyValues() {
        return propertyValueList;
    }
    
    public PropertyValue getPropertyValue(String propertyName) {
        for (PropertyValue pv : propertyValueList) {
            if (pv.getName().equals(propertyName)) {
                return pv;
            }
        }
        return null;
    }
} 