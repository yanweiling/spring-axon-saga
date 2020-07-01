package com.ywl.study.axon.customer.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomerCreatedEvent {
    /*关联的聚合对象ID*/
    private String customerId;

    private String username;
    private String password;

}
