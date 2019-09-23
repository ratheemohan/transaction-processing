package com.mastercard.tranaction.processing.service;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.japi.function.Function;
import akka.japi.function.Procedure;
import akka.stream.Materializer;
import akka.stream.javadsl.*;
import akka.util.ByteString;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mastercard.tranaction.processing.domain.Transaction;
import com.mastercard.tranaction.processing.domain.TransactionResult;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.BiFunction;

@Slf4j
public class TransactionStreamProcessor {

    private final ActorSystem system = ActorSystem.create("transaction-processor");
    private final Materializer materializer = Materializer.createMaterializer(system);
    private final EventService eventService = new DefaultEventService();
    private final Service transactionService = new TransactionService(eventService, materializer);
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final ObjectReader reader = objectMapper.readerFor(Transaction.class);

    private final String transactionSource;

    public TransactionStreamProcessor(final String transactionSource) {
        this.transactionSource = transactionSource;
    }

    public void readAndProcessTransaction() {
        FileIO.fromPath(Paths.get(transactionSource))
                .via(Framing.delimiter(ByteString.fromString("\n"), 256))
                .map(ByteString::utf8String)
                .map((Function<String, Transaction>) reader::readValue)
                .via(processTransactionAndTransformResult())
                .toMat(Sink.foreach(new Procedure<TransactionResult>() {
                    @Override
                    public void apply(final TransactionResult result) {
                        log.info("Transaction result={} after processing transactionId={}", result, result.getTransactionId());
                    }
                }), Keep.right());

    }

    private Flow<Transaction, TransactionResult, NotUsed> processTransactionAndTransformResult() {
        return Flow.of(Transaction.class)
                .mapAsync(3, tx -> transactionService.processTransaction(tx).handleAsync(new BiFunction<Optional<String>, Throwable, TransactionResult>() {
                    @Override
                    public TransactionResult apply(Optional<String> result, Throwable throwable) {
                        if (result.isEmpty()) {
                            return TransactionResult.success(tx.getTransactionId());
                        }
                        return TransactionResult.fail(result.get(), tx.getTransactionId());
                    }
                }));
    }
}
