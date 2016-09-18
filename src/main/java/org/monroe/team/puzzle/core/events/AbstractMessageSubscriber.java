package org.monroe.team.puzzle.core.events;


import org.monroe.team.puzzle.core.log.Logs;
import reactor.bus.Event;
import reactor.fn.Consumer;

public abstract class AbstractMessageSubscriber<T extends Message>
        implements MessageSubscriber<T>, Consumer<Event<T>>{

    public final Class<T> eventClass;

    public AbstractMessageSubscriber(final Class<T> eventClass) {
        this.eventClass = eventClass;
    }

    @Override
    public void accept(final Event<T> tEvent) {
        T message = tEvent.getData();
        Logs.setTransactionId(message.transactionId);
        Logs.bus.info("Handle event by {}. Message {} ", this.getClass().getName(), message);
        onMessage(message);
    }
}
