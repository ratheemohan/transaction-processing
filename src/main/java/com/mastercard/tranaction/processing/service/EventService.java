package com.mastercard.tranaction.processing.service;

import com.mastercard.tranaction.processing.domain.event.Event;

public interface EventService {

    void processEvent(Event event);

}
