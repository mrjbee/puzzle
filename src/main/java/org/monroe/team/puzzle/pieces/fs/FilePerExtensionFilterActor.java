package org.monroe.team.puzzle.pieces.fs;

import org.monroe.team.puzzle.core.events.AbstractMessageSubscriber;
import org.monroe.team.puzzle.core.events.MessagePublisher;
import org.monroe.team.puzzle.pieces.fs.events.FileMessage;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class FilePerExtensionFilterActor extends AbstractMessageSubscriber<FileMessage> {

    @Autowired
    Log log;

    @Autowired
    MessagePublisher messagePublisher;

    private final Publisher publisher;
    private final List<String> supportedExtensions;

    public FilePerExtensionFilterActor(
            final List<String> supportedExtensions,
            final Publisher publisher) {
 //       super(FileMessage.class);
        this.publisher = publisher;
        this.supportedExtensions = supportedExtensions;
    }

    @Override
    public void onMessage(String parentKey, FileMessage message) {
        for (String supportedExtension : supportedExtensions) {
            if (message.ext != null && supportedExtension.toLowerCase().equals(message.ext.toLowerCase())){
                log.info("Message going to be republished = {}", message);
                publisher.republish(parentKey, message, messagePublisher);
                return;
            }
        }
        log.info("Message was filter out = {}", message);
    }

    public interface Publisher {
        void republish(
                String parentKey,
                FileMessage fileEvent,
                MessagePublisher messagePublisher);
    }

}
