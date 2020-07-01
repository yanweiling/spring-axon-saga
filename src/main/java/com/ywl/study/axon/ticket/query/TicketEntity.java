package com.ywl.study.axon.ticket.query;

import com.ywl.study.axon.ticket.command.OrderTicketMoveCommand;
import com.ywl.study.axon.ticket.command.OrderTicketPreserveCommand;
import com.ywl.study.axon.ticket.command.OrderTicketUnlockCommand;
import com.ywl.study.axon.ticket.command.TicketCreateCommand;
import com.ywl.study.axon.ticket.event.OrderTicketMovedEvent;
import com.ywl.study.axon.ticket.event.OrderTicketPreserveFailEvent;
import com.ywl.study.axon.ticket.event.OrderTicketPreservedEvent;
import com.ywl.study.axon.ticket.event.OrderTicketUnlockedEvent;
import com.ywl.study.axon.ticket.event.TicketCreatedEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Entity;
import javax.persistence.Id;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity(name="tb_ticket")
public class TicketEntity {
    private static final Logger LOG= LoggerFactory.getLogger(TicketEntity.class);
    @Id
    private String ticketId;
    private String name;

    /*锁票人员*/
    private String lockUser;
    /*锁票后，支付完成，则该票的拥有者*/
    private String owner;

}
