package org.monroe.team.puzzle;

import org.monroe.team.puzzle.pieces.filewatcher.FileWatchTask;
import org.monroe.team.puzzle.pieces.metadata.extractor.PhotoFileMetadataExtractor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MediaDispatchingConfiguration {

    @Bean
    @ConfigurationProperties(prefix="media.dispatching.watcher", ignoreUnknownFields = false)
    public FileWatchTask fileWatcher(){
        return new FileWatchTask();
    }

    @Bean
    public PhotoFileMetadataExtractor photoFileMetadataExtractor(){
        return new PhotoFileMetadataExtractor();
    }
}
