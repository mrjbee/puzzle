package org.monroe.team.puzzle.core.events;


import org.hibernate.validator.constraints.NotBlank;
import org.monroe.team.puzzle.core.log.Logs;
import reactor.bus.Event;
import reactor.fn.Consumer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractMessageSubscriber<T extends Message>
        implements MessageSubscriber<T>, Consumer<Event<T>>{

    @NotBlank
    public String subscriberKey;
    private Pattern keyPattern;

    @Override
    public void accept(final Event<T> tEvent) {
        T message = tEvent.getData();
        String actualKey = tEvent.getKey().toString();
        Logs.setTransactionId(message.transactionId);
        Matcher matcher = keyPattern.matcher(actualKey);
        String keyCapture = null;
        if(matcher.find() && matcher.groupCount() > 0) {
            keyCapture = matcher.group(1);
        }
        Logs.bus.info("Handle event with a key {} [{}] by {}. Message {} ", actualKey,
                keyCapture, this.getClass().getName(), message);
        onMessage(keyCapture, message);
    }

    @Override
    public void onMessage(final String parentKey, final T message) {
        onMessage(message);
    }


    public void onMessage(final T message) {}

    public String getSubscriberKey() {
        return subscriberKey;
    }

    public void setSubscriberKey(final String subscriberKey) {
        this.subscriberKey = subscriberKey;
        this.keyPattern = Pattern.compile(subscriberKey);
    }
}
