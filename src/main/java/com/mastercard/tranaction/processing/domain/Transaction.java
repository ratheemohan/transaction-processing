package com.mastercard.tranaction.processing.domain;

import lombok.Data;
import lombok.NonNull;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Representation of a payment transaction that is created when a
 * payment is made from one bank account to another
 */
@Data
public class Transaction {

    // uniquely identifier for transaction
    @NonNull
    private final String transactionId;

    // the account id from/to where funds are transferred
    @NonNull
    private final String accountId;

    // payment amount
    @NonNull
    private final BigDecimal amount;

    //describes whether money came IN or OUT
    @NonNull
    private final TransactionType type;

    //transaction occurred time
    @NonNull
    private final Instant occurredAt;

    //transaction description
    @NonNull
    private final String description;
}
