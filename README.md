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
 
 
 ==saga流程设计==
 
 
```
package com.ywl.study.axon.order;

import com.ywl.study.axon.customer.command.OrderPayCommand;
import com.ywl.study.axon.customer.event.OrderPaidEvent;
import com.ywl.study.axon.customer.event.OrderPayFailEvent;
import com.ywl.study.axon.order.command.OrderFailCommand;
import com.ywl.study.axon.order.command.OrderFinishCommand;
import com.ywl.study.axon.order.event.OrderCreatedEvent;
import com.ywl.study.axon.order.event.OrderFailedEvent;
import com.ywl.study.axon.order.event.OrderFinishedEvent;
import com.ywl.study.axon.ticket.command.OrderTicketMoveCommand;
import com.ywl.study.axon.ticket.command.OrderTicketPreserveCommand;
import com.ywl.study.axon.ticket.command.OrderTicketUnlockCommand;
import com.ywl.study.axon.ticket.event.OrderTicketMovedEvent;
import com.ywl.study.axon.ticket.event.OrderTicketPreserveFailEvent;
import com.ywl.study.axon.ticket.event.OrderTicketPreservedEvent;
import com.ywl.study.axon.ticket.event.OrderTicketUnlockedEvent;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.callbacks.LoggingCallback;
import org.axonframework.eventhandling.saga.EndSaga;
import org.axonframework.eventhandling.saga.SagaEventHandler;
import org.axonframework.eventhandling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import static org.axonframework.commandhandling.GenericCommandMessage.asCommandMessage;

/**
 * 对于每一个流程会有一个saga实例
 */
@Saga
public class OrderManagementSaga {
    private static final Logger LOG= LoggerFactory.getLogger(OrderManagementSaga.class);

    /*saga对象会被序列化到数据库中，如果其中属性不想序列化到数据库中，则增加transient*/
    @Autowired
    private transient CommandBus commandBus;

    /*属性值要想序列化到saga实例中，需要有get set方法*/
    private String orderId;
    private String ticketId;
    private String customerId;
    private Double amount;

    /**
     * 当执行OrderCreatedEvent 的时候，下一步要触发执行OrderTicketPreserveCommand
     * @param event
     */
    @StartSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void on(OrderCreatedEvent event){
        this.orderId=event.getOrderId();
        this.ticketId=event.getTicketId();
        this.customerId=event.getCustomerId();
        this.amount=event.getAmount();
        //生成下一步要执行的command
        OrderTicketPreserveCommand command=new OrderTicketPreserveCommand(event.getTicketId(),event.getOrderId(),event.getCustomerId());
        commandBus.dispatch(asCommandMessage(command), LoggingCallback.INSTANCE);//LoggingCallback.INSTANCE 日志回调方法
    }


    @SagaEventHandler(associationProperty = "orderId")
    public void on(OrderTicketPreservedEvent event){
        OrderPayCommand command=new OrderPayCommand(customerId,orderId,amount);
        commandBus.dispatch(asCommandMessage(command), LoggingCallback.INSTANCE);//LoggingCallback.INSTANCE 日志回调方法
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void on(OrderTicketPreserveFailEvent event){
        OrderFailCommand command=new OrderFailCommand(orderId,"lock ticket fail");
        commandBus.dispatch(asCommandMessage(command), LoggingCallback.INSTANCE);//LoggingCallback.INSTANCE 日志回调方法
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void on(OrderPaidEvent event){

        OrderTicketMoveCommand command=new OrderTicketMoveCommand(ticketId,orderId,customerId);
        commandBus.dispatch(asCommandMessage(command), LoggingCallback.INSTANCE);//LoggingCallback.INSTANCE 日志回调方法
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void on(OrderTicketMovedEvent event){

        OrderFinishCommand command=new OrderFinishCommand(orderId);
        commandBus.dispatch(asCommandMessage(command), LoggingCallback.INSTANCE);//LoggingCallback.INSTANCE 日志回调方法
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void on(OrderPayFailEvent event){
        OrderTicketUnlockCommand unlockCommand=new OrderTicketUnlockCommand(ticketId,customerId);
        commandBus.dispatch(asCommandMessage(unlockCommand), LoggingCallback.INSTANCE);//LoggingCallback.INSTANCE 日志回调方法

        OrderFailCommand failCommand=new OrderFailCommand(orderId,"Paid fail");//saga 对中文的序列化和返序列化会有问题
        commandBus.dispatch(asCommandMessage(failCommand), LoggingCallback.INSTANCE);//LoggingCallback.INSTANCE 日志回调方法
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void on(OrderFailedEvent event){
      //只是标志saga结束，不需要做任何处理
        LOG.info("Order:{} failed.",orderId);
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void on(OrderFinishedEvent event){
        //只是标志saga结束，不需要做任何处理
        LOG.info("Order:{} finish.",orderId);
    }

    /**
     * @return the orderId
     */
    public String getOrderId() {
        return orderId;
    }

    /**
     * @param orderId to set
     */
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    /**
     * @return the ticketId
     */
    public String getTicketId() {
        return ticketId;
    }

    /**
     * @param ticketId to set
     */
    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    /**
     * @return the customerId
     */
    public String getCustomerId() {
        return customerId;
    }

    /**
     * @param customerId to set
     */
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    /**
     * @return the amount
     */
    public Double getAmount() {
        return amount;
    }

    /**
     * @param amount to set
     */
    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
```


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

---
有的时候，因为程序设计问题，会导致物化视图和聚合对象中的数据不一致，如何解决呢？

## 解决办法：

**在开发阶段，我们如何查看聚合对象中的数据，验证聚合对象和物化视图的一致性问题---生产环境不建议直接查看聚合对象**

**1.新增OrderId**

```
package com.ywl.study.axon.order.query;

import org.axonframework.common.Assert;

public class OrderId {
    private final String identifier;
    private final int hashCode;

    public OrderId(String identifier){
        Assert.notNull(identifier,()->"Identifier may not be null");
        this.identifier=identifier;
        this.hashCode=identifier.hashCode();
    }

    @Override
    public boolean equals(Object o){
        if(this==o) return true;
        if(o==null || getClass()!=o.getClass()) return false;
        OrderId customerId=(OrderId)o;
        return identifier.equals(customerId.identifier);
    }

    @Override
    public int hashCode(){
        return hashCode;
    }

    @Override
    public String toString(){
        return identifier;
    }

}

```
**2.新增OrderQueryHandler**


```
package com.ywl.study.axon.order.query;

import com.ywl.study.axon.order.Order;
import org.axonframework.commandhandling.model.Repository;
import org.axonframework.queryhandling.QueryHandler;
import org.axonframework.spring.config.AxonConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 为了查看聚合对象中的数据，验证和物化视图中的数据是否一致
 * 正式环境下不建议使用，只限定在开发环境
 */
@Component
public class OrderQueryHandler {
    private static final Logger LOG= LoggerFactory.getLogger(OrderQueryHandler.class);

    @Autowired
    AxonConfiguration axonConfiguration;

    @Autowired
    private OrderEntityRepository orderEntityRepository;

    /**
     * 其实本来是要这样设计的
     * @QueryHandler
     * public Order query(String orderId){
     *     ....
     * }
     * 我们预期是给orderId字符串绑定queryHandler的，但是由于orderId类型是String，
     * 这样会遇到，假设我们传了String customerId,或者String ticketId也会绑定QueryHandler，
     * 这样肯定不是我们需要的；
     *
     * 所以这里将String orderId转换成了OrderId,而，这个类中的属性identifier其实就是String orderId，
     * 而其中的hashCode属性只是附加来实现equals方法的
     *
     */

    /*evenSource不建议这么做*/
    @QueryHandler
    public Order query(OrderId orderId){
        LOG.info("Query order info with:{}",orderId.toString());
        final Order[] orders=new Order[1];
        Repository<Order> repository=axonConfiguration.repository(Order.class);//根据聚合对象类型来获取
        repository.load(orderId.toString()).execute(order -> {
            orders[0]=order;
        });
      return orders[0];
    }

    /*一般是通过物化视图来查询的；
    * 并且，同一个类型绑定的handler只能有一个，所以这里暂时注释掉其中一个*/

//    @QueryHandler
//    public OrderEntity queryByView(OrderId orderId){
//        return orderEntityRepository.getOne(orderId.toString());
//    }
}

```

3.新增restful接口

```
@RestController
@RequestMapping("/orders")
public class OrderController {
    
    @Autowired
    private QueryGateway queryGateway;
    
    @GetMapping("/query/{orderId}")
    public CompletableFuture<Order> getRromRepo(@PathVariable String orderId){
        LOG.info("Reqeust Order with:{}",orderId );
        return queryGateway.query(new OrderId(orderId),Order.class);//由OrderId关联的QueryHandler处理
    }
}
```


 
axon默认都是用string序列化和反序列化的

但是对于web的接口，一般都是用json进行序列化和反序列化的

**4.更改axon序列化方式**

```

#axon默认都是用string序列化和反序列化的
axon:
  serializer:
    general: jackson
    messages: jackson
    events: jackson

```
**5.引入依赖**

```
 <!--axon设置json序列化需要增加此依赖-->
        <dependency>
            <groupId>com.fasterxml.jackson.datatype</groupId>
            <artifactId>jackson-datatype-jsr310</artifactId>
        </dependency>
        <!--axon设置json序列化需要增加此依赖-->
```
**6.测试**

```
curl http://localhost:8080/orders/query/4cd98ab5-66e7-4688-a3f4-e7aca0005f0d
{"orderId":"4cd98ab5-66e7-4688-a3f4-e7aca0005f0d","titile":"杞︾エ棰勮","ticketId":"036994ee-3a8a-4f51-85d8-bed05378b9a9","customerId":"2d076433-e98e-4ff0-92e3-a78169d4016b","amount":100.0,"reason":null,"
createDate":"2020-07-02T06:21:06.742Z","status":"FINISHED"}

```


