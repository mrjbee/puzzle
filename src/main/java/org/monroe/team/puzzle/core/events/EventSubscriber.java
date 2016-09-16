package org.monroe.team.puzzle.core.events;

public interface EventSubscriber <T> {
    void onEvent(T event);
}
