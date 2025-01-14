package com.kama.minispring.core.type;

import java.util.ArrayList;
import java.util.List;

/**
 * ClassMetadata的简单实现类
 *
 * @author kama
 * @version 1.0.0
 */
public class SimpleClassMetadata implements ClassMetadata {
    
    private final Class<?> introspectedClass;
    
    public SimpleClassMetadata(Class<?> introspectedClass) {
        this.introspectedClass = introspectedClass;
    }
    
    @Override
    public String getClassName() {
        return this.introspectedClass.getName();
    }
    
    @Override
    public boolean isInterface() {
        return this.introspectedClass.isInterface();
    }
    
    @Override
    public boolean isAbstract() {
        return java.lang.reflect.Modifier.isAbstract(this.introspectedClass.getModifiers());
    }
    
    @Override
    public boolean isConcrete() {
        return !(isInterface() || isAbstract());
    }
    
    @Override
    public String getSuperClassName() {
        Class<?> superClass = this.introspectedClass.getSuperclass();
        return superClass != null ? superClass.getName() : null;
    }
    
    @Override
    public String[] getInterfaceNames() {
        Class<?>[] interfaces = this.introspectedClass.getInterfaces();
        List<String> interfaceNames = new ArrayList<>(interfaces.length);
        for (Class<?> ifc : interfaces) {
            interfaceNames.add(ifc.getName());
        }
        return interfaceNames.toArray(new String[0]);
    }
} 