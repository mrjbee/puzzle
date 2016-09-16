package org.monroe.team.puzzle.pieces.test;

import org.monroe.team.puzzle.core.events.MbassyEventSubscriber;
import org.monroe.team.puzzle.pieces.filewatcher.events.NewFileEvent;
import org.monroe.team.puzzle.pieces.test.events.TestEvent;
import org.springframework.stereotype.Component;

@Component
public class TestEventHandler extends MbassyEventSubscriber<NewFileEvent> {

    @Override
    public void onEvent(final NewFileEvent event) {
        System.out.println(event);
    }
}
