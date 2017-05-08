package org.monroe.team.puzzle.apps;

import org.monroe.team.puzzle.apps.mediacatalog.events.PictureFileMessage;
import org.monroe.team.puzzle.apps.mediacatalog.events.VideoFileMessage;
import org.monroe.team.puzzle.core.events.MessagePublisher;
import org.monroe.team.puzzle.pieces.fs.FilePerExtensionFilterActor;
import org.monroe.team.puzzle.pieces.fs.events.FileMessage;
import org.monroe.team.puzzle.pieces.metadata.MediaFileMetadataExtractActor;
import org.monroe.team.puzzle.pieces.metadata.PictureMetadataExtractor;
import org.monroe.team.puzzle.pieces.metadata.VideoMetadataExtractor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.Arrays;

@Configuration
public class CommonConfig {
    @Bean
    @ConfigurationProperties(prefix="photo-metadata-extractor", ignoreUnknownFields = true)
    public MediaFileMetadataExtractActor<PictureFileMessage> photoFileMetadataExtractor(MessagePublisher messagePublisher, PictureMetadataExtractor extractor){
        return new MediaFileMetadataExtractActor<PictureFileMessage>(
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
    @ConfigurationProperties(prefix="video-metadata-extractor", ignoreUnknownFields = true)
    public MediaFileMetadataExtractActor<VideoFileMessage> videoFileMetadataExtractor(MessagePublisher messagePublisher, VideoMetadataExtractor extractor){
        return new MediaFileMetadataExtractActor<VideoFileMessage>(
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
    @ConfigurationProperties(prefix="picture-file-filter", ignoreUnknownFields = true)
    public FilePerExtensionFilterActor pictureFilter(){
        return new FilePerExtensionFilterActor(
                Arrays.asList(".jpeg",".jpg", ".png", ".bmp"),
                new FilePerExtensionFilterActor.Publisher() {
                    @Override
                    public void republish(
                            final String parentKey,
                            final FileMessage fileEvent,
                            final MessagePublisher messagePublisher) {
                        messagePublisher.post(
                                parentKey+".file.picture",
                                new PictureFileMessage(
                                        fileEvent.filePath,
                                        fileEvent.name,
                                        fileEvent.ext
                                ));
                    }
                }
        );
    }

    @Bean
    @ConfigurationProperties(prefix="video-file-filter", ignoreUnknownFields = true)
    public FilePerExtensionFilterActor videoFilter(){
        return new FilePerExtensionFilterActor(
                Arrays.asList(".mp4",".mov"),
                new FilePerExtensionFilterActor.Publisher() {
                    @Override
                    public void republish(
                            final String parentKey,
                            final FileMessage fileEvent,
                            final MessagePublisher messagePublisher) {
                        messagePublisher.post(
                                parentKey+".file.video",
                                new VideoFileMessage(
                                        fileEvent.filePath,
                                        fileEvent.name,
                                        fileEvent.ext
                                ));
                    }
                }
        );
    }
}
