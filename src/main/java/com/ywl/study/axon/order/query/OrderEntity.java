package com.ywl.study.axon.order.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.commandhandling.model.AggregateIdentifier;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "tb_order")
public class OrderEntity {
    @Id
    private String orderId;

    private String titile;

    private String ticketId;

    private String customerId;

    private Double amount;

    private String reason;

    private ZonedDateTime createDate;
    private String status;
}
