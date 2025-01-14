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

## 事务管理实现

### 1. 核心接口设计
- `PlatformTransactionManager`: 事务管理器接口，定义了获取事务、提交事务、回滚事务等基本操作
- `TransactionDefinition`: 事务定义接口，包含事务的传播行为、隔离级别、超时时间等属性
- `TransactionStatus`: 事务状态接口，用于跟踪事务的执行状态

### 2. 事务同步机制
- `TransactionSynchronization`: 定义了事务执行各个阶段的回调方法
- `TransactionSynchronizationManager`: 使用ThreadLocal管理事务同步状态
- 同步回调的执行顺序：
  1. beforeBegin: 事务开始前
  2. beforeCommit/beforeRollback: 事务提交/回滚前
  3. afterCommit/afterRollback: 事务提交/回滚后
  4. afterCompletion: 事务完成后

### 3. 抽象事务管理器
- `AbstractPlatformTransactionManager`: 实现了事务处理的基本流程
- 模板方法模式：定义了doGetTransaction、doBegin、doCommit、doRollback等抽象方法
- 处理事务同步：在事务执行的不同阶段触发同步回调

### 4. JDBC事务实现
- `DataSourceTransactionManager`: 基于数据源的事务管理器实现
- 管理数据库连接的事务状态
- 处理事务的提交和回滚操作

### 面试要点
1. Spring事务的实现原理
   - 基于AOP实现声明式事务
   - 使用ThreadLocal保证事务的隔离性
   - 通过同步回调机制扩展事务处理流程

2. 事务同步的作用
   - 在事务执行的不同阶段执行自定义逻辑
   - 实现缓存同步、消息发送等功能
   - 保证数据的一致性

3. 事务管理器的设计模式
   - 模板方法模式：定义事务处理的骨架
   - 策略模式：支持不同的事务实现
   - 适配器模式：适配不同的事务API

4. 注意事项
   - 正确处理事务的传播行为
   - 合理设置事务的隔离级别
   - 注意事务超时设置
   - 处理好事务回滚的异常 

### 事务传播行为实现

事务传播行为定义了事务方法和事务方法发生嵌套调用时事务如何传播。Spring 支持 7 种事务传播行为:

1. PROPAGATION_REQUIRED: 如果当前没有事务,就新建一个事务,如果已经存在一个事务中,加入到这个事务中。这是最常见的选择。

2. PROPAGATION_SUPPORTS: 支持当前事务,如果当前没有事务,就以非事务方式执行。

3. PROPAGATION_MANDATORY: 使用当前的事务,如果当前没有事务,就抛出异常。

4. PROPAGATION_REQUIRES_NEW: 新建事务,如果当前存在事务,把当前事务挂起。

5. PROPAGATION_NOT_SUPPORTED: 以非事务方式执行操作,如果当前存在事务,就把当前事务挂起。

6. PROPAGATION_NEVER: 以非事务方式执行,如果当前存在事务,则抛出异常。

7. PROPAGATION_NESTED: 如果当前存在事务,则在嵌套事务内执行。如果当前没有事务,则执行与PROPAGATION_REQUIRED类似的操作。

实现要点:

1. 在 TransactionDefinition 接口中定义事务传播行为的常量。

2. 在 AbstractPlatformTransactionManager 的 getTransaction 方法中实现事务传播行为的判断逻辑:
   - 检查是否存在当前事务
   - 根据传播行为执行相应的处理:
     - 对于 REQUIRED,如果没有事务则创建新事务
     - 对于 REQUIRES_NEW,总是创建新事务并挂起当前事务
     - 对于 NESTED,在当前事务中创建保存点
     - 对于 MANDATORY,在没有事务时抛出异常
     - 等等

3. 在 DataSourceTransactionManager 中实现:
   - suspend 方法用于挂起当前事务
   - resume 方法用于恢复挂起的事务
   - 管理数据库连接的自动提交状态

4. 编写测试用例验证各种传播行为:
   - 测试 REQUIRED 传播行为
   - 测试 REQUIRES_NEW 传播行为
   - 测试事务挂起和恢复

面试要点:

1. Spring 事务传播行为的应用场景
   - REQUIRED 适用于增删改操作
   - REQUIRES_NEW 适用于不受外部事务影响的操作
   - NESTED 适用于可以回滚到保存点的操作

2. 事务传播行为的实现原理
   - 基于 ThreadLocal 存储和传播事务信息
   - 通过 事务同步管理器 管理事务资源
   - 通过 AOP 实现事务增强

3. 常见问题
   - 事务失效的原因
   - 事务传播行为的选择依据
   - 分布式事务的处理 