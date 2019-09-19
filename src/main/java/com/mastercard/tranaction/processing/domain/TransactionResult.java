package com.mastercard.tranaction.processing.domain;

import lombok.AllArgsConstructor;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class TransactionResult {

    private final boolean success;
    private String errorMessage;

    public static TransactionResult fail(final String errorMessage) {
        return new TransactionResult(false, errorMessage);
    }

    public static TransactionResult success() {
        return new TransactionResult(true, null);
    }
}
