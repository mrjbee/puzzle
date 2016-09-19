package org.monroe.team.puzzle.pieces.metadata;

import org.monroe.team.puzzle.core.events.MessagePublisher;
import org.monroe.team.puzzle.core.events.AbstractMessageSubscriber;
import org.monroe.team.puzzle.pieces.fs.events.FileMessage;
import org.monroe.team.puzzle.pieces.metadata.events.MediaFileMessage;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;


public class MediaFileMetadataExtractActor<T extends FileMessage> extends AbstractMessageSubscriber<T> {

    @Autowired
    Log log;

    private final MessagePublisher messagePublisher;
    private final MediaFileMetadataExtractor metadataExtractor;
    private final FileEventFilter<T> fileEventFilter;

    public MediaFileMetadataExtractActor(
            final MessagePublisher messagePublisher,
            final MediaFileMetadataExtractor metadataExtractor,
            final FileEventFilter<T> fileEventFilter) {
        this.messagePublisher = messagePublisher;
        this.metadataExtractor = metadataExtractor;
        this.fileEventFilter = fileEventFilter;
    }

    @Override
    public void onMessage(final String parentKey,
                          final T message) {
        File file = fileEventFilter.toFile(message);
        if (file == null){
            log.info("File event skipped = {}", message.filePath);
            return;
        }
        publishMediaAsMediaFile(parentKey, message, metadataExtractor.metadata(file));
    }

    private void publishMediaAsMediaFile(
            final String parentKey,
            final FileMessage event,
            final MediaMetadata metadata1) {
        MediaFileMessage mediaFileEvent =
                new MediaFileMessage(
                    event.filePath,
                    event.name,
                    event.ext,
                    metadata1);
        String publishKey = parentKey+".metadata";
        log.info("Media metadata extracted and published with key {} as {}",
                publishKey, mediaFileEvent);
        messagePublisher.post(
                publishKey,
                mediaFileEvent);
    }

    public interface FileEventFilter<T> {
        File toFile(T event);
    }

}
