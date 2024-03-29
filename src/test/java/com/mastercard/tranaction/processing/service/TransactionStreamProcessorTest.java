package com.mastercard.tranaction.processing.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransactionStreamProcessorTest {

    private final TransactionStreamProcessor processor = new TransactionStreamProcessor("/src/test/resources/transactions.txt");

    @Test
    public void shouldProcessTransaction() {
        processor.readAndProcessTransaction();
    }

}