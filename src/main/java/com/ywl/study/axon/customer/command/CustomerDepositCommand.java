package com.ywl.study.axon.customer.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.axonframework.commandhandling.TargetAggregateIdentifier;
@Data
@AllArgsConstructor
public class CustomerDepositCommand {
    @TargetAggregateIdentifier
    private String customerId;

    /*存款金额*/
    private Double amount;
}
