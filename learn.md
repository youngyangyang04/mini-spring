# Spring框架核心原理学习指南

## 循环依赖处理机制

### 1. 什么是循环依赖?
循环依赖是指两个或多个bean之间相互依赖,形成一个闭环。例如:
- A依赖B,B依赖A
- A依赖B,B依赖C,C依赖A

### 2. Spring如何解决循环依赖?

#### 2.1 三级缓存机制
Spring使用三级缓存来解决setter注入的循环依赖:

1. 一级缓存(singletonObjects): 
   - 存储完全初始化好的bean
   - Map<String, Object>结构
   - bean完成所有初始化后放入

2. 二级缓存(earlySingletonObjects):
   - 存储原始的bean对象
   - Map<String, Object>结构
   - 提前曝光的bean,还未完成属性注入

3. 三级缓存(singletonFactories):
   - 存储bean工厂对象
   - Map<String, ObjectFactory<?>>结构
   - 创建bean的工厂,用于延迟创建代理对象

#### 2.2 循环依赖解决流程

##### Setter注入循环依赖解决流程:
1. 创建A的实例,放入三级缓存
2. 填充A的属性,发现需要B
3. 创建B的实例,放入三级缓存
4. 填充B的属性,发现需要A
5. 从三级缓存中获取A的早期引用
6. 完成B的初始化,并将B放入一级缓存
7. 继续完成A的初始化,并将A放入一级缓存

##### 构造器注入循环依赖:
- Spring无法解决构造器注入的循环依赖
- 因为在创建实例时就需要依赖对象
- 解决方案是在设计时避免构造器循环依赖

### 3. 实现要点

#### 3.1 关键类和方法
1. DefaultListableBeanFactory:
   - createBean(): 创建bean实例
   - doGetBean(): 获取bean,处理依赖
   - addSingleton(): 将bean加入一级缓存
   - addSingletonFactory(): 将工厂加入三级缓存

2. AbstractAutowireCapableBeanFactory:
   - createBeanInstance(): 实例化bean
   - populateBean(): 填充bean属性
   - initializeBean(): 初始化bean

#### 3.2 核心实现步骤
1. 创建bean前检查循环依赖:
```java
if (isSingletonCurrentlyInCreation(beanName)) {
    // 检查是否构造器循环依赖
    if (beanDefinition.getConstructorArgumentValues().size() > 0) {
        throw new BeansException("Circular dependency detected");
    }
}
```

2. 添加bean到三级缓存:
```java
addSingletonFactory(beanName, () -> getEarlyBeanReference(beanName, beanDefinition, bean));
```

3. 处理属性填充时的循环依赖:
```java
// 尝试从缓存中获取早期引用
Object bean = getSingleton(beanName);
if (bean == null) {
    bean = createBean(beanName, beanDefinition);
}
```

### 4. 面试考点

#### 4.1 基础问题
1. 什么是循环依赖?如何检测循环依赖?
2. Spring如何解决循环依赖?为什么要使用三级缓存?
3. 构造器循环依赖为什么无法解决?

#### 4.2 进阶问题
1. 三级缓存的作用分别是什么?为什么需要三级而不是两级?
2. 如何在日常开发中避免循环依赖?
3. Spring是如何保证单例bean的线程安全的?

#### 4.3 实战问题
1. 如何实现一个简单的循环依赖检测器?
2. 如何优化三级缓存机制?
3. 在实际项目中如何定位循环依赖问题?

### 5. 最佳实践

1. 设计原则:
   - 避免构造器循环依赖
   - 使用setter注入处理必要的循环依赖
   - 考虑重构存在循环依赖的代码

2. 开发建议:
   - 合理规划类的职责,避免过度耦合
   - 使用@Lazy注解处理特殊场景
   - 编写单元测试验证循环依赖处理

3. 调试技巧:
   - 使用断点跟踪bean的创建过程
   - 观察三级缓存的变化
   - 分析循环依赖的调用链路

### 6. 参考资料

1. Spring官方文档:
   - [Spring Framework Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/)
   - [Spring Core Technologies](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html)

2. 推荐阅读:
   - 《Spring源码深度解析》
   - 《Spring技术内幕》

3. 在线资源:
   - Spring Framework GitHub仓库
   - Spring Framework问答社区 