package com.ywl.study.axon.order.query;

import com.sun.org.apache.regexp.internal.RE;
import com.ywl.study.axon.order.Order;
import com.ywl.study.axon.order.event.OrderCreatedEvent;
import com.ywl.study.axon.order.event.OrderFailedEvent;
import com.ywl.study.axon.order.event.OrderFinishedEvent;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderProjector {
    private static final Logger LOG= LoggerFactory.getLogger(OrderProjector.class);
    @Autowired
    private OrderEntityRepository repository;
    @EventHandler
    public void on(OrderCreatedEvent event){
        OrderEntity order=new OrderEntity();
        order.setOrderId(event.getOrderId());
        order.setAmount(event.getAmount());
        order.setCustomerId(event.getCustomerId());
        order.setTicketId(event.getTicketId());
        order.setTitile(event.getTitile());
        order.setCreateDate(event.getCreateDate());
        order.setStatus("NEW");
        repository.save(order);
        LOG.info("Executed event:{}",event);
    }

    @EventHandler
    public void on(OrderFinishedEvent event){
        OrderEntity order=repository.getOne(event.getOrderId());
        order.setStatus("FINISHED");
        repository.save(order);
        LOG.info("Executed event:{}",event);
    }

    @EventHandler
    public void on(OrderFailedEvent event){
        OrderEntity order=repository.getOne(event.getOrderId());
        order.setStatus("FAILED");
        repository.save(order);
        LOG.info("Executed event:{}",event);
    }
}
