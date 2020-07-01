package com.ywl.study.axon.customer.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.axonframework.commandhandling.TargetAggregateIdentifier;
@Data
@AllArgsConstructor
public class CustomerChargeCommand {
    /*关联的聚合对象ID*/
    @TargetAggregateIdentifier
    private String customerId;

    /*取款金额*/
    private Double amount;
}
