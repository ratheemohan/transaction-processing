package com.mastercard.tranaction.processing.service;

import akka.NotUsed;
import akka.japi.function.Function;
import akka.stream.Materializer;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.mastercard.tranaction.processing.domain.Transaction;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static com.mastercard.tranaction.processing.domain.event.Event.buildEvent;
import static com.mastercard.tranaction.processing.domain.event.EventType.FAILED_VALIDATION;
import static com.mastercard.tranaction.processing.domain.event.EventType.TRANSACTION_SUCCESSFUL;

@Slf4j
public class TransactionService implements Service {

    private static final String VALIDATION_FAILED = "Validation Failed";

    private final EventService eventService;
    private final Materializer materializer;
    private final int LOWER_TRANSACTION_LIMIT = 100;

    TransactionService(final EventService eventService, final Materializer materializer) {
        this.eventService = eventService;
        this.materializer = materializer;
    }

    @Override
    public CompletionStage<Optional<String>> processTransaction(Transaction tx) {
        log.info("Processing transaction={}", tx);
        return Source.single(tx)
                .via(validate())
                .via(computeTransaction())
                .runWith(Sink.head(), materializer);
    }

    private Flow<Transaction, ValidationResult, NotUsed> validate() {
        return Flow.of(Transaction.class)
                .mapAsync(1, (Function<Transaction, CompletionStage<ValidationResult>>) tx -> {
                    //made up this validation to prove the failure condition
                    if (tx.getAmount().compareTo(BigDecimal.valueOf(LOWER_TRANSACTION_LIMIT)) <= 0) {
                        eventService.processEvent(buildEvent(FAILED_VALIDATION, tx.getTransactionId()));
                        return CompletableFuture.completedStage(new ValidationResult(tx, VALIDATION_FAILED));
                    }
                    return CompletableFuture.completedStage(new ValidationResult(tx, null));
                });
    }

    private Flow<ValidationResult, Optional<String>, NotUsed> computeTransaction() {
        return Flow.of(ValidationResult.class)
                .mapAsync(4, (Function<ValidationResult, CompletionStage<Optional<String>>>) result -> {
                    if(result.isValidTransaction()){
                        eventService.processEvent(buildEvent(TRANSACTION_SUCCESSFUL, result.transaction.getTransactionId()));
                        return CompletableFuture.completedStage(Optional.empty());
                    }

                    return CompletableFuture.completedStage(result.getFailureMessage());
                });
    }

    @Value
    private static class ValidationResult {
        private final Transaction transaction;
        private final String failureMessage;


        boolean isValidTransaction() {
            return Optional.ofNullable(failureMessage).isEmpty();
        }

        Optional<String> getFailureMessage() {
            return Optional.ofNullable(failureMessage);
        }
    }
}
