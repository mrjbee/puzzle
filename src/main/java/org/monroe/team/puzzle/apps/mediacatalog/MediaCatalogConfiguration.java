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
    @ConfigurationProperties(prefix="media.dispatching.watcher", ignoreUnknownFields = true)
    public FolderCleanupActor cleanupWatcher(){
        return new FolderCleanupActor();
    }

    @Bean
    public MediaFileMetadataExtractActor<PictureFileMessage> photoFileMetadataExtractor(MessagePublisher messagePublisher, PictureMetadataExtractor extractor){
        return new MediaFileMetadataExtractActor<PictureFileMessage>(
                PictureFileMessage.class,
                messagePublisher,
                extractor,
                new MediaFileMetadataExtractActor.FileEventFilter<PictureFileMessage>() {
            @Override
            public File toFile(final PictureFileMessage event) {
                return new File(event.filePath);
            }
        });
    }

    @Bean
    public MediaFileMetadataExtractActor<VideoFileMessage> videoFileMetadataExtractor(MessagePublisher messagePublisher, VideoMetadataExtractor extractor){
        return new MediaFileMetadataExtractActor<VideoFileMessage>(
                VideoFileMessage.class,
                messagePublisher,
                extractor,
                new MediaFileMetadataExtractActor.FileEventFilter<VideoFileMessage>() {
                    @Override
                    public File toFile(final VideoFileMessage event) {
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
                    public void republish(final FileMessage fileEvent, final MessagePublisher messagePublisher) {
                        messagePublisher.post(new PictureFileMessage(
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
                    public void republish(final FileMessage fileEvent, final MessagePublisher messagePublisher) {
                        messagePublisher.post(new VideoFileMessage(
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
