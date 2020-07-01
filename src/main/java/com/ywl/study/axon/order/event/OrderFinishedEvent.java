package com.ywl.study.axon.order.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderFinishedEvent {
    private String orderId;

}
