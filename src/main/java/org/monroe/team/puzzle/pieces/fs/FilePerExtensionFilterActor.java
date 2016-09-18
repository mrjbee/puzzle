package org.monroe.team.puzzle.pieces.fs;

import org.monroe.team.puzzle.core.events.EventBus;
import org.monroe.team.puzzle.core.events.MbassyEventSubscriber;
import org.monroe.team.puzzle.pieces.fs.events.FileEvent;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class FilePerExtensionFilterActor extends MbassyEventSubscriber<FileEvent> {

    @Autowired
    EventBus eventBus;

    private final Publisher publisher;
    private final List<String> supportedExtensions;

    public FilePerExtensionFilterActor(
            final List<String> supportedExtensions,
            final Publisher publisher) {
        super(FileEvent.class);
        this.publisher = publisher;
        this.supportedExtensions = supportedExtensions;
    }

    @Override
    public void onEvent(final FileEvent event) {
        for (String supportedExtension : supportedExtensions) {
            if (event.ext != null && supportedExtension.toLowerCase().equals(event.ext.toLowerCase())){
                publisher.republish(event, eventBus);
            }
        }
    }

    public interface Publisher {
        void republish(FileEvent fileEvent, EventBus eventBus);
    }

}