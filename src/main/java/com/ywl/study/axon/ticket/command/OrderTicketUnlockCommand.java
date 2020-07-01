package com.ywl.study.axon.ticket.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Data
@AllArgsConstructor
public class OrderTicketUnlockCommand {
    @TargetAggregateIdentifier
    private String ticketId;

    private String customerId;
}
