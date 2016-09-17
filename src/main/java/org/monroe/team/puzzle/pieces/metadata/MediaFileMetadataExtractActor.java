package org.monroe.team.puzzle.pieces.metadata;

import org.monroe.team.puzzle.core.events.EventBus;
import org.monroe.team.puzzle.core.events.MbassyEventSubscriber;
import org.monroe.team.puzzle.pieces.fs.events.FileEvent;
import org.monroe.team.puzzle.pieces.metadata.events.MediaFile;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;


public class MediaFileMetadataExtractActor<T extends FileEvent> extends MbassyEventSubscriber<T> {

    @Autowired
    Log log;

    private final EventBus eventBus;
    private final MediaFileMetadataExtractor metadataExtractor;
    private final FileEventFilter<T> fileEventFilter;

    public MediaFileMetadataExtractActor(
            Class<T> eventClass,
            final EventBus eventBus,
            final MediaFileMetadataExtractor metadataExtractor,
            final FileEventFilter<T> fileEventFilter) {
        super(eventClass);
        this.eventBus = eventBus;
        this.metadataExtractor = metadataExtractor;
        this.fileEventFilter = fileEventFilter;
    }

    @Override
    public void onEvent(final T event) {
        File file = fileEventFilter.toFile(event);
        if (file == null){
            log.info("File event skipped = {}",event.filePath);
            return;
        }
        publishMediaAsMediaFile(event, metadataExtractor.metadata(file));
    }

    private void publishMediaAsMediaFile(final FileEvent event, final MediaMetadata metadata1) {
        MediaFile mediaFile = new MediaFile(
                event.filePath,
                event.name,
                event.ext,
                metadata1);
        eventBus.post(mediaFile);
    }

    public interface FileEventFilter<T> {
        File toFile(T event);
    }

}
