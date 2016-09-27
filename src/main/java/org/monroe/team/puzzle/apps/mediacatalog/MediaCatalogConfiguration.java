package org.monroe.team.puzzle.apps.mediacatalog;

import org.monroe.team.puzzle.pieces.fs.FileWatchActor;
import org.monroe.team.puzzle.pieces.fs.FileWatchOperationLog;
import org.monroe.team.puzzle.pieces.fs.FolderCleanupActor;
import org.monroe.team.puzzle.pieces.fs.TempCachedFileWatchOperationLog;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MediaCatalogConfiguration {

    @Bean
    @Qualifier("media.dispatching.watcher.operation.log")
    @ConfigurationProperties(prefix="media.dispatching.watcher.operation.log", ignoreUnknownFields = true)
    public FileWatchOperationLog fileWatcherOperationLog(){
        return new TempCachedFileWatchOperationLog();
    }

    @Bean
    @ConfigurationProperties(prefix="media.dispatching.watcher", ignoreUnknownFields = true)
    public FileWatchActor fileWatcher(
            @Qualifier("media.dispatching.watcher.operation.log")
            FileWatchOperationLog fileWatchOperationLog){
        return new FileWatchActor(fileWatchOperationLog);
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
