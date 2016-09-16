package org.monroe.team.puzzle.core.events;
import net.engio.mbassy.bus.MBassador;
import org.monroe.team.puzzle.core.logs.Logs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MbassyEventBus implements EventBus{

    @Autowired
    MBassador<Event> implementation;

    @Override
    public void post(final Event event) {
        event.transactionId = Logs.getTransactionId();
        Logs.bus.info("Post event: {} ", event);
        implementation.publishAsync(event);
    }
}
