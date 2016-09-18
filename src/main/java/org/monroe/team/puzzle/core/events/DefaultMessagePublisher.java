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
        message.transactionId = Logs.getTransactionId();
        Logs.bus.info("Post message: {} ", message);
        eventBus.notify(message.getClass().toString(), Event.wrap(message));
    }
}
