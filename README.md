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
    ![架构图](https://github.com/StonyShi/message-channel/blob/master/flows/cluster-flow.png)
    
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
    
    
    