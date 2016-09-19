package org.monroe.team.puzzle.core.events;

public interface MessagePublisher {
    void post(Message message);
    void post(String key, Message message);
}
