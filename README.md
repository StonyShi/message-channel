# 消息中心 message-channel

## 主要功能
    1. 消息处理，生产消费
    2. 聊天会话
    3. 应用广播、通知推送

## 设计目标
    1. worker无状态线性扩容
    2. 消息不落盘中转
    3. 单机长连接用户200w+
    4. 单机并发用户30w+

## 架构图
![架构图](https://github.com/StonyShi/message-channel/blob/master/flows/cluster-flow_jg.png)
 
    
## 主要模块

#### Master
    1. worker注册、发现，平衡worker负载
    2. 监控worker存活，性能指标
    3. 聊天会话管理分配
    
#### Worker
    1. 注册、性能指标上报Master 
    2. topic消息转发
    3. 通知提醒app
    4. 聊天会话转发
    
    
#### APP
    1. 生产topic消息
    2. 聊天会话


#### Consumer
    1. 消防topic消息 
    2. 广播消息到APP
    
## 聊天会话
    1. app-A发起聊天，在worker注册管理会话信息（id，app-A通道，A-worker地址）
    2. worker向master注册会话(Id,A-worker地址)
    3. app-B连接聊天Id，在worker注册
    4. 先在master查找id，匹配B-worker与发起者是否一致，不一致返回307重定向到A-worker地址
    
    
## 会话流程图
![会话流程图](https://github.com/StonyShi/message-channel/blob/master/flows/chat-flow.png)
    
 
    
    
    