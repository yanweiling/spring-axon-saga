package com.ywl.study.axon.customer.query;

import com.ywl.study.axon.customer.event.CustomerChargedEvent;
import com.ywl.study.axon.customer.event.CustomerCreatedEvent;
import com.ywl.study.axon.customer.event.CustomerDepositedEvent;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomerProjector {
    @Autowired
    private CustomerEntityRepository repository;

    @EventHandler
    public void on(CustomerCreatedEvent event){
        CustomerEntity customer=new CustomerEntity(event.getCustomerId(),event.getUsername(),event.getPassword(),0d);
        repository.save(customer);
    }
    @EventHandler
    public void on(CustomerDepositedEvent event){
        String customerId=event.getCustomerId();
        CustomerEntity accountView=repository.getOne(customerId);
        Double newDeposit=accountView.getDeposit()+event.getAmount();
        accountView.setDeposit(newDeposit);
        repository.save(accountView);
    }

    @EventHandler
    public void on(CustomerChargedEvent event){
        String customerId=event.getCustomerId();
        CustomerEntity accountView=repository.getOne(customerId);
        Double newDeposit=accountView.getDeposit()-event.getAmount();
        accountView.setDeposit(newDeposit);
        repository.save(accountView);
    }
}
