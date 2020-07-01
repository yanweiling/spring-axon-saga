package com.ywl.study.axon.ticket.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Data
@AllArgsConstructor
public class OrderTicketUnlockedEvent {

    private String ticketId;

}
