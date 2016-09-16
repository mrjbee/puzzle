package org.monroe.team.puzzle.pieces.metadata.extractor;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import org.monroe.team.puzzle.core.events.EventBus;
import org.monroe.team.puzzle.core.events.MbassyEventSubscriber;
import org.monroe.team.puzzle.core.logs.Logs;
import org.monroe.team.puzzle.pieces.filewatcher.events.NewFileEvent;
import org.monroe.team.puzzle.pieces.metadata.events.MediaFileMetadata;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class PhotoFileMetadataExtractor extends MbassyEventSubscriber<NewFileEvent>{

    @Autowired
    EventBus eventBus;

    @Override
    public void onEvent(final NewFileEvent event) {
        Metadata metadata = null;

        try {
            metadata = ImageMetadataReader.readMetadata(new File(event.filePath));
        } catch (IOException e) {
            Logs.piece("metadata-extractor").warn(e, "Failed to get creation date for = {}", event.filePath);
            return;
        } catch (ImageProcessingException e){
            Logs.piece("metadata-extractor").warn("File skiped because of {}. File {}", e.getMessage(), event.filePath);
            return;
        }
        // obtain the Exif directory
        ExifSubIFDDirectory directory
                = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);

        // query the tag's value
        Date date
                = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);

        MediaFileMetadata mediaFileMetadata = new MediaFileMetadata(
                MediaFileMetadata.Type.PICTURE,
                event.filePath,
                event.name,
                event.ext,
                date.getTime()
                );

        eventBus.post(mediaFileMetadata);
    }
}
