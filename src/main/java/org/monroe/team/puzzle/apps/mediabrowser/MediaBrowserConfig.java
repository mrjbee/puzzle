package org.monroe.team.puzzle.apps.mediabrowser;

import org.monroe.team.puzzle.apps.mediacatalog.CatalogActor;
import org.monroe.team.puzzle.pieces.fs.FileWatchActor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MediaBrowserConfig {
    @Bean
    @ConfigurationProperties(prefix="media.browser.watcher", ignoreUnknownFields = true)
    public FileWatchActor catalogFileWatcher(){
        return new FileWatchActor();
    }

    @Bean
    @ConfigurationProperties(prefix="media.browser.index", ignoreUnknownFields = true)
    public MediaIndexerActor indexerActor(){
        return new MediaIndexerActor();
    }
}
