package com.ywl.study.axon.order.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Data
@AllArgsConstructor
public class OrderFailCommand {
    @TargetAggregateIdentifier
    private String orderId;

    private String reason;
}
