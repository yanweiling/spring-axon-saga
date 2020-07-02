package com.ywl.study.axon.customer.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderPayFailEvent {
    private String orderId;

}
