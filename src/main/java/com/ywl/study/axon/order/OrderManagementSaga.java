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
