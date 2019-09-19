package com.mastercard.tranaction.processing.domain.event;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;


@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Event {
    private final String eventId;
    private final EventType eventType;
    private final Instant eventOccurredAt;
    private final String transactionId;

    @Override
    public String toString() {
        return "Event{" +
                "eventId='" + eventId + '\'' +
                ", eventType=" + eventType +
                ", eventOccurredAt=" + eventOccurredAt +
                ", transactionId='" + transactionId + '\'' +
                '}';
    }

    public static Event buildEvent(EventType eventType, String transactionId) {
        return new Event(UUID.randomUUID().toString(), eventType, Instant.now(), transactionId);
    }
}



