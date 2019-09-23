package com.mastercard.tranaction.processing.service;

import com.mastercard.tranaction.processing.domain.Transaction;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface Service {
    CompletionStage<Optional<String>> processTransaction(Transaction tx);
}
