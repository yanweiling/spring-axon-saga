package com.ywl.study.axon.customer;

import com.ywl.study.axon.customer.command.CustomerChargeCommand;
import com.ywl.study.axon.customer.command.CustomerCreateCommand;
import com.ywl.study.axon.customer.command.CustomerDepositCommand;
import com.ywl.study.axon.customer.command.OrderPayCommand;
import com.ywl.study.axon.customer.event.CustomerChargedEvent;
import com.ywl.study.axon.customer.event.CustomerCreatedEvent;
import com.ywl.study.axon.customer.event.CustomerDepositedEvent;
import com.ywl.study.axon.customer.event.OrderPaidEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

@Aggregate
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Customer {
    private static final Logger LOG= LoggerFactory.getLogger(Customer.class);

    @AggregateIdentifier
    private String customerId;

    private String username;

    private String password;

    private Double deposit;


    @CommandHandler
    public Customer(CustomerCreateCommand command){
        apply(new CustomerCreatedEvent(command.getCustomerId(),command.getUsername(),command.getPassword()));
    }

    @CommandHandler
    public void handle(CustomerDepositCommand command){
        apply(new CustomerDepositedEvent(command.getCustomerId(),command.getAmount()));
    }

    @CommandHandler
    public void handle(CustomerChargeCommand command){
        if(this.deposit>=command.getAmount()){
            apply(new CustomerChargedEvent(command.getCustomerId(),command.getAmount()));
        }else{
            throw new IllegalArgumentException("余额不足");
        }

    }


    @CommandHandler
    public void handle(OrderPayCommand command){
        if(this.deposit>=command.getAmount()){
            apply(new OrderPaidEvent(command.getCustomerId(),command.getOrderId(),command.getAmount()));
        }else{
            throw new IllegalArgumentException("余额不足");
        }

    }

    @EventSourcingHandler
    public void on(CustomerCreatedEvent event){
        this.customerId=event.getCustomerId();
        this.username=event.getUsername();
        this.password=event.getPassword();
        this.deposit=0d;
        LOG.info("Executed event:{}",event);
    }

    @EventSourcingHandler
    public void on(CustomerDepositedEvent event){
        this.deposit=deposit+event.getAmount();
        LOG.info("Executed event:{}",event);
    }

    @EventSourcingHandler
    public void on(CustomerChargedEvent event){
        this.deposit=deposit-event.getAmount();
        LOG.info("Executed event:{}",event);
    }

    @EventSourcingHandler
    public void on(OrderPaidEvent event){
        this.deposit=deposit-event.getAmount();
        LOG.info("Executed event:{}",event);
    }


}
