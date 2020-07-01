package com.ywl.study.axon.ticket.event;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 锁票command
 */
@Data
@AllArgsConstructor
public class OrderTicketPreserveFailEvent {

    private String orderId;

}
