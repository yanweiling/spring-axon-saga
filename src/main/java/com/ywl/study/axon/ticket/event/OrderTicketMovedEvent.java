package com.ywl.study.axon.ticket.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Data
@AllArgsConstructor
public class OrderTicketMovedEvent {

    private String ticketId;
    private String orderId;
    private String customerId;
}
