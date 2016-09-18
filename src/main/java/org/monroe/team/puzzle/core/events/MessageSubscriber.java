package org.monroe.team.puzzle.core.events;

public interface MessageSubscriber<T> {
    void onMessage(T message);
}
