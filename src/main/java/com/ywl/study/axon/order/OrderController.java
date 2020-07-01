package com.ywl.study.axon.order;

import com.ywl.study.axon.customer.command.CustomerChargeCommand;
import com.ywl.study.axon.customer.command.CustomerCreateCommand;
import com.ywl.study.axon.customer.command.CustomerDepositCommand;
import com.ywl.study.axon.customer.query.CustomerEntity;
import com.ywl.study.axon.customer.query.CustomerEntityRepository;
import com.ywl.study.axon.order.command.OrderCreateCommand;
import com.ywl.study.axon.order.query.OrderEntity;
import com.ywl.study.axon.order.query.OrderEntityRepository;
import org.axonframework.commandhandling.callbacks.LoggingCallback;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.queryhandling.QueryGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private static final Logger LOG= LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private CommandGateway commandGateway;
    @Autowired
    private QueryGateway queryGateway;
    @Autowired
    private OrderEntityRepository repository;

    @PostMapping("")
    public void create(@RequestBody Order order){
        LOG.info("Reqeust to create order for:{}",order);
        UUID orderId= UUID.randomUUID();
        OrderCreateCommand createCommand=new OrderCreateCommand(orderId.toString(),order.getTitile(),order.getTicketId(),order.getCustomerId(),order.getAmount());
         commandGateway.send(createCommand, LoggingCallback.INSTANCE);//callback 用来记录日志

    }

    @GetMapping("/{orderId}")
    public OrderEntity getView(@PathVariable String orderId){
        LOG.info("Reqeust OrderEntity with:{}",orderId );
        return repository.getOne(orderId);
    }



}
