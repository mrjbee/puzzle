package org.monroe.team.puzzle.pieces.metadata;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import org.monroe.team.puzzle.core.logs.Logs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Date;

@Component
public class PictureMetadataExtractor implements MediaFileMetadataExtractor{

    @Autowired
    Log log;

    @Override
    public MediaMetadata metadata(final File file) {
        Metadata metadata = null;

        try {
            metadata = ImageMetadataReader.readMetadata(file);
        } catch (IOException|ImageProcessingException e) {
            log.warn(e, "Failed to get creation date for = {}", file.getAbsoluteFile());
            throw new RuntimeException(e);
        }

        // obtain the Exif directory
        ExifSubIFDDirectory directory
                = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);

        // query the tag's value
        Date date
                = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);

        return new MediaMetadata(MediaMetadata.Type.PICTURE, date.getTime());
    }
}
