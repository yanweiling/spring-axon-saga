package com.ywl.study.axon.customer.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomerChargedEvent {
    private String customerId;

    /*取款金额*/
    private Double amount;
}
