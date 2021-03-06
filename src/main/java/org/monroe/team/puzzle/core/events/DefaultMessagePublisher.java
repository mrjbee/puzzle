package org.monroe.team.puzzle.core.events;

import org.monroe.team.puzzle.core.log.Logs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.bus.Event;
import reactor.bus.EventBus;

@Component
public class DefaultMessagePublisher implements MessagePublisher {

    @Autowired
    EventBus eventBus;

    @Override
    public void post(final Message message) {
        post(message.getClass().getName(), message);
    }

    @Override
    public void post(final String key, final Message message) {
        message.transactionId = Logs.addTransactionLevel(Logs.getTransactionId());
        Logs.bus.info("[Transaction id = {}] Post message with key {} : {} ", message.transactionId, key, message);
        eventBus.notify(key, Event.wrap(message));
    }
}
