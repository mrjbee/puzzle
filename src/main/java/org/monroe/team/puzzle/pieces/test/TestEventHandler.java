package org.monroe.team.puzzle.pieces.test;

import org.monroe.team.puzzle.core.events.MbassyEventSubscriber;
import org.monroe.team.puzzle.pieces.filewatcher.events.FileEvent;
import org.springframework.stereotype.Component;

@Component
public class TestEventHandler extends MbassyEventSubscriber<FileEvent> {

    public TestEventHandler() {
        super(FileEvent.class);
    }

    @Override
    public void onEvent(final FileEvent event) {
        System.out.println(event);
    }
}
