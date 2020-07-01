package com.ywl.study.axon.ticket.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TicketCreatedEvent {
    private String ticketId;
    private String name;
}
