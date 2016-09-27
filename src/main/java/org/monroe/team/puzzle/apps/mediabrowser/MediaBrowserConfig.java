package org.monroe.team.puzzle.apps.mediabrowser;

import org.monroe.team.puzzle.pieces.fs.FileWatchActor;
import org.monroe.team.puzzle.pieces.fs.FileWatchOperationLog;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class MediaBrowserConfig {

    @Bean
    @ConfigurationProperties(prefix="media.browser.watcher", ignoreUnknownFields = true)
    public FileWatchActor catalogFileWatcher(
            @Qualifier("index.based.operation.log")
            FileWatchOperationLog fileWatchOperationLog){
        return new FileWatchActor(fileWatchOperationLog);
    }

    @Bean
    @ConfigurationProperties(prefix="media.browser.index", ignoreUnknownFields = true)
    public MediaIndexerActor indexerActor(){
        return new MediaIndexerActor();
    }
}
