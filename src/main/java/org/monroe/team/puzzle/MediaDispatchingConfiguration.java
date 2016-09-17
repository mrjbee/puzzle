package org.monroe.team.puzzle;

import org.monroe.team.puzzle.core.events.EventBus;
import org.monroe.team.puzzle.pieces.filewatcher.FileWatchActor;
import org.monroe.team.puzzle.pieces.filewatcher.events.FileEvent;
import org.monroe.team.puzzle.pieces.metadata.MediaFileMetadataExtractActor;
import org.monroe.team.puzzle.pieces.metadata.PictureMetadataExtractor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class MediaDispatchingConfiguration {

    @Bean
    @ConfigurationProperties(prefix="media.dispatching.watcher", ignoreUnknownFields = false)
    public FileWatchActor fileWatcher(){
        return new FileWatchActor();
    }

    @Bean
    public MediaFileMetadataExtractActor<FileEvent> photoFileMetadataExtractor(EventBus eventBus, PictureMetadataExtractor extractor){
        return new MediaFileMetadataExtractActor<FileEvent>(
                FileEvent.class,
                eventBus,
                extractor,
                new MediaFileMetadataExtractActor.FileEventFilter<FileEvent>() {
            @Override
            public File toFile(final FileEvent event) {
                return new File(event.filePath);
            }
        });
    }
}
