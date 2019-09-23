package com.mastercard.tranaction.processing.domain;

import lombok.AllArgsConstructor;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class TransactionResult {

    private final boolean success;
    private final String transactionId;
    private String errorMessage;

    public static TransactionResult fail(final String errorMessage, final String transactionId) {
        return new TransactionResult(false, transactionId, errorMessage);
    }

    public static TransactionResult success(final String transactionId) {
        return new TransactionResult(true, transactionId, null);
    }
}
