package org.monroe.team.puzzle.pieces.metadata;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import org.springframework.beans.factory.annotation.Autowired;
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

        Integer width = directory.getInteger(ExifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH);
        Integer height = directory.getInteger(ExifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT);

        if (date == null){
            log.warn("Failed to get creation date. Use now() date for = {}", file.getAbsoluteFile());
            date = new Date();
        }


        return new MediaMetadata(MediaMetadata.Type.PICTURE, date.getTime(), new int[]{width, height});
    }
}
