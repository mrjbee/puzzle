package org.monroe.team.puzzle.apps.mediacatalog;

import org.monroe.team.puzzle.apps.mediacatalog.events.PictureFileMessage;
import org.monroe.team.puzzle.apps.mediacatalog.events.VideoFileMessage;
import org.monroe.team.puzzle.core.events.MessagePublisher;
import org.monroe.team.puzzle.pieces.fs.FileWatchActor;
import org.monroe.team.puzzle.pieces.fs.FolderCleanupActor;
import org.monroe.team.puzzle.pieces.fs.events.FileMessage;
import org.monroe.team.puzzle.pieces.metadata.MediaFileMetadataExtractActor;
import org.monroe.team.puzzle.pieces.fs.FilePerExtensionFilterActor;
import org.monroe.team.puzzle.pieces.metadata.PictureMetadataExtractor;
import org.monroe.team.puzzle.pieces.metadata.VideoMetadataExtractor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.Arrays;

@Configuration
public class MediaCatalogConfiguration {

    @Bean
    @ConfigurationProperties(prefix="media.dispatching.watcher", ignoreUnknownFields = true)
    public FileWatchActor fileWatcher(){
        return new FileWatchActor();
    }

    @Bean
    @ConfigurationProperties(prefix="media.dispatching.cleanup.watcher", ignoreUnknownFields = true)
    public FolderCleanupActor cleanupWatcher(){
        return new FolderCleanupActor();
    }

    @Bean
    @ConfigurationProperties(prefix="media.dispatching.catalog", ignoreUnknownFields = true)
    public CatalogActor catalogActor(){
        return new CatalogActor();
    }
}
