package com.ywl.study.axon.config;

import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.scheduling.EventScheduler;
import org.axonframework.eventhandling.scheduling.java.SimpleEventScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.util.concurrent.Executors;

@Configuration
public class AxonConfig {


   @Bean
    public EventScheduler eventScheduler(EventBus eventBus,TransactionManager transactionManager){
       return new SimpleEventScheduler(Executors.newScheduledThreadPool(1),eventBus,transactionManager);
   }

}
