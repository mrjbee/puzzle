package org.monroe.team.puzzle.core.events;


import net.engio.mbassy.listener.Enveloped;
import net.engio.mbassy.listener.Handler;
import org.monroe.team.puzzle.core.logs.Logs;

public abstract class MbassyEventSubscriber<T extends Event> implements EventSubscriber<T>{

    @Handler(rejectSubtypes = true)
    public final void handle(T event){
        Logs.setTransactionId(event.transactionId);
        Logs.bus.info("Handle event by {}. Event {} ", this.getClass().getName(), event);
        onEvent(event);
    }

}
