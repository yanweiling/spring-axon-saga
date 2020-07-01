package com.ywl.study.axon.customer.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomerDepositedEvent {
    private String customerId;
    /*存款金额*/
    private Double amount;
}
