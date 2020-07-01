package com.ywl.study.axon.ticket.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

/**
 * 锁票command
 */
@Data
@AllArgsConstructor
public class OrderTicketPreservedEvent {

    private String ticketId;
    private String orderId;
    private String customerId;
}
