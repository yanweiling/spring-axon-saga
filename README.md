**Axon实例**

    购票系统
    功能：创建用户，创建票，实现用户购票流程
    单系统服务
    使用Axon Saga
    
**Saga模式**
    
    事务驱动的业务流程管理模式
    通过开始事件、结束事件、过程中的事件完成整个业务流程
    保证在多个事件处理方法执行期间实现事务性
    
Axon Saga模式

    StartSaga-SagaEventHandler-EndSaga
    使用associate将不同的事件关联到同一个Saga流程中
    正常的结束业务都通过EndSaga标签触发，超时使用EventScheduler，触发一个EndSaga
    一次业务流程的执行对应一个saga实例
    Saga实例状态和关联的事件会保存在数据库中
---

**以前的方案**
![image](https://note.youdao.com/yws/public/resource/354d8215ba83a7c5c8c20782b614e896/xmlnote/B75E3A6F93C14A168DAA1F5C2B44E5DF/30259)

**现在的方案**
![image](https://note.youdao.com/yws/public/resource/354d8215ba83a7c5c8c20782b614e896/xmlnote/1DBE0E9078DB47EAAA115168DD97F5AD/30545)

    saga中的流程，每一步都是由event驱动的
    当某一个event开始的时候，注册到saga中和该event关联的eventHandler就会被触发，就会开始一个saga流程
    
    1.订单创建command-->订单创建event--->将由order聚合对象去处理，同时saga会监听这个订单创建的event，监听到以后，开始start saga；
    当有order_create 的event以后，会创建一个TICKET_PRESERVE（锁票）的command，那下一步就开始进行锁票操作，然后生成一个TICKET_PRESERVE 的event，然后再saga中监听这个event，当存在这个event以后，会告诉saga系统，下一步操作是ORDER_PAY,以此类推，当监听到TICKET_FINISH，就知道结束了
    
![image](https://note.youdao.com/yws/public/resource/354d8215ba83a7c5c8c20782b614e896/xmlnote/B1C57953143F403985B2EF4544107797/30583)

-----
# 项目演示

项目地址：https://github.com/yanweiling/spring-axon-saga.git
    
根据上面的下单Axon流程图，我们得知

    --- order模块
         ORDER_CREATE  COMMAND 
         ORDER_FAIL    COMMAND
         ORDER_FINISH  COMMAND
     
     
         ORDER_FAILED     EVENT
         ORDER_CREATED    EVENT
         ORDER_FINISHED   EVENT
     
    
    ---customer模块
         ORDER_PAY     COMMAND
         
         ORDER_PAY_FAILED EVENT
         ORDER_PAID       EVENT
    
    ----ticket模块
            TICKET_PRESERVE COMMAND
            TICKET_UNLOCK   COMMAND
            TICKET_MOVE     COMMAND
    
    
            TICKET_PRESERVE_FAILED EVENT
            TICKET_PRESERVED       EVENT
            TICKET_UNLOCKED        EVENT
            TICKET_MOVED           EVENT
    

项目启动后，会在数据库ywl_axon_saga中自动新建数据表

**1.新建ticket**

    post http://localhost:8080/tickets?name=京张铁路
    
    返回ticketid：1308155a-d70b-4f00-aa6a-7f928b59b3e6

**2.新建customer**
    
    post http://localhost:8080/customers?name=zhangsan&password=123
    
    返回customerid:4001f353-aefd-48e7-b0e0-a2767fe8cba3
**3.充值**

    put  http://localhost:8080/customers/4001f353-aefd-48e7-b0e0-a2767fe8cba3/deposit/100
**4.customer购买ticket**

    post http://localhost:8080/orders
    
    {
	"ticketId":"1308155a-d70b-4f00-aa6a-7f928b59b3e6",
	"titile":"车票预订",
	"customerId":"4001f353-aefd-48e7-b0e0-a2767fe8cba3",
	"amount":100
    }
    
在domain_event_entry中记录操作流程：

    com.ywl.study.axon.customer.event.CustomerCreatedEvent
    com.ywl.study.axon.customer.event.CustomerDepositedEvent
    com.ywl.study.axon.ticket.event.TicketCreatedEvent
    com.ywl.study.axon.order.event.OrderCreatedEvent
    com.ywl.study.axon.ticket.event.OrderTicketPreservedEvent
    com.ywl.study.axon.customer.event.OrderPaidEvent
    com.ywl.study.axon.ticket.event.OrderTicketMovedEvent
    com.ywl.study.axon.order.event.OrderFinishedEvent

每一次流程启动就会新建一个saga实例到表saga_entry中，当实例结束后，将将实例从saga_entry中删除