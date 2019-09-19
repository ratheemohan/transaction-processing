package com.mastercard.tranaction.processing.service;

import akka.Done;
import akka.NotUsed;
import akka.japi.function.Function;
import akka.japi.function.Function2;
import akka.stream.Materializer;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.mastercard.tranaction.processing.domain.Transaction;
import com.mastercard.tranaction.processing.domain.event.Event;
import com.mastercard.tranaction.processing.domain.event.EventType;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;
import akka.stream.*;


import static com.mastercard.tranaction.processing.domain.event.Event.buildEvent;
import static com.mastercard.tranaction.processing.domain.event.EventType.FAILED_VALIDATION;

public class TransactionService implements Service {

    private static final String SUCCESSFUL_TRANSACTION = "Success";
    private static final String VALIDATION_FAILED = "Validation Failed";

    private final EventService eventService;
    private final Materializer materializer;

    public TransactionService(final EventService eventService, final Materializer materializer) {
        this.eventService = eventService;
        this.materializer = materializer;
    }

    @Override
    public CompletionStage<Optional<String>> processTransaction(Transaction tx) {
        final Sink<Transaction, CompletionStage<Optional<String>>> foldSink = Sink.fold(Optional.empty(), new Function2<Optional<String>, Transaction, Optional<String>>() {
            @Override
            public Optional<String> apply(Optional<String> result, Transaction arg2) throws Exception, Exception {
                return result;
            }
        });

        return Source.single(tx)
                .via(validateTransaction())
                .via(computeTransaction())
                .runWith(foldSink, materializer);


    }

    private Flow<Transaction, Optional<String>, NotUsed> computeTransaction() {
        return Flow.of(Transaction.class)
                .mapAsyncUnordered(4, new Function<Transaction, CompletionStage<Optional<String>>>() {
                    @Override
                    public CompletionStage<Optional<String>> apply(Transaction param) {
                        return CompletableFuture.completedStage(Optional.empty());
                    }
                });
    }

    private Flow<Transaction, Optional<String>, NotUsed> validateTransaction() {
        return Flow.of(Transaction.class).mapAsync(4, (Function<Transaction, CompletionStage<Optional<String>>>) transaction -> {

            if (transaction.getAmount().compareTo(BigDecimal.valueOf(100)) <= 0) {
                eventService.processEvent(buildEvent(FAILED_VALIDATION, UUID.randomUUID().toString()));
                return CompletableFuture.completedStage(Optional.of("Failed Validation"));
            } else {
                return CompletableFuture.completedStage(Optional.empty());
            }
        });
    }

}
