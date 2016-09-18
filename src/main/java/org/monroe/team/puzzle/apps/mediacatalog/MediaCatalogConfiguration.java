package org.monroe.team.puzzle.apps.mediacatalog;

import org.monroe.team.puzzle.apps.mediacatalog.events.VideoFileEvent;
import org.monroe.team.puzzle.core.events.EventBus;
import org.monroe.team.puzzle.pieces.fs.FileWatchActor;
import org.monroe.team.puzzle.pieces.fs.events.FileEvent;
import org.monroe.team.puzzle.pieces.metadata.MediaFileMetadataExtractActor;
import org.monroe.team.puzzle.pieces.fs.FilePerExtensionFilterActor;
import org.monroe.team.puzzle.pieces.metadata.PictureMetadataExtractor;
import org.monroe.team.puzzle.apps.mediacatalog.events.PictureFileEvent;
import org.monroe.team.puzzle.pieces.metadata.VideoMetadataExtractor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.Arrays;

@Configuration
public class MediaCatalogConfiguration {

    @Bean
    @ConfigurationProperties(prefix="media.dispatching.watcher", ignoreUnknownFields = false)
    public FileWatchActor fileWatcher(){
        return new FileWatchActor();
    }

    @Bean
    public MediaFileMetadataExtractActor<PictureFileEvent> photoFileMetadataExtractor(EventBus eventBus, PictureMetadataExtractor extractor){
        return new MediaFileMetadataExtractActor<PictureFileEvent>(
                PictureFileEvent.class,
                eventBus,
                extractor,
                new MediaFileMetadataExtractActor.FileEventFilter<PictureFileEvent>() {
            @Override
            public File toFile(final PictureFileEvent event) {
                return new File(event.filePath);
            }
        });
    }

    @Bean
    public MediaFileMetadataExtractActor<VideoFileEvent> videoFileMetadataExtractor(EventBus eventBus, VideoMetadataExtractor extractor){
        return new MediaFileMetadataExtractActor<VideoFileEvent>(
                VideoFileEvent.class,
                eventBus,
                extractor,
                new MediaFileMetadataExtractActor.FileEventFilter<VideoFileEvent>() {
                    @Override
                    public File toFile(final VideoFileEvent event) {
                        return new File(event.filePath);
                    }
                });
    }

    @Bean
    public FilePerExtensionFilterActor pictureFilter(){
        return new FilePerExtensionFilterActor(
                Arrays.asList(".jpg", ".png", ".bmp"),
                new FilePerExtensionFilterActor.Publisher() {
                    @Override
                    public void republish(final FileEvent fileEvent, final EventBus eventBus) {
                        eventBus.post(new PictureFileEvent(
                                fileEvent.filePath,
                                fileEvent.name,
                                fileEvent.ext
                        ));
                    }
                }
        );
    }

    @Bean
    public FilePerExtensionFilterActor videoFilter(){
        return new FilePerExtensionFilterActor(
                Arrays.asList(".mp4"),
                new FilePerExtensionFilterActor.Publisher() {
                    @Override
                    public void republish(final FileEvent fileEvent, final EventBus eventBus) {
                        eventBus.post(new VideoFileEvent(
                                fileEvent.filePath,
                                fileEvent.name,
                                fileEvent.ext
                        ));
                    }
                }
        );
    }

    @Bean
    public CatalogActor catalogActor(){
        return new CatalogActor();
    }
}
