package org.monroe.team.puzzle.pieces.metadata;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.drew.metadata.png.PngDirectory;
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

        Date creationDate = null;
        Integer width = null, height = null;


        // obtain the Exif directory
        ExifSubIFDDirectory exifDirectory
                = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);

        if (exifDirectory != null){
            creationDate
                    = exifDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);

            width = exifDirectory.getInteger(ExifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH);
            height = exifDirectory.getInteger(ExifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT);
        }

        if (creationDate == null){
            log.warn("Failed to get creation date. Use now() date for = {}", file.getAbsoluteFile());
            creationDate = new Date();
        }


        if (height == null || height == 0 || width == null || width == 0){
            JpegDirectory jpegDescription = metadata.getFirstDirectoryOfType(JpegDirectory.class);
            if (jpegDescription != null){
                height = jpegDescription.getInteger(JpegDirectory.TAG_IMAGE_HEIGHT);
                width = jpegDescription.getInteger(JpegDirectory.TAG_IMAGE_WIDTH);
            } else {
                PngDirectory pngDirectory = metadata.getFirstDirectoryOfType(PngDirectory.class);
                if (pngDirectory != null){
                    height = pngDirectory.getInteger(PngDirectory.TAG_IMAGE_HEIGHT);
                    width = pngDirectory.getInteger(PngDirectory.TAG_IMAGE_WIDTH);
                }
            }
        }

        if (height == null || height == 0 || width == null || width == 0){
            log.warn("Failed to get width or(and) height for = {}", file.getAbsoluteFile());
            width = 100;
            height = 100;
        }

        return new MediaMetadata(MediaMetadata.Type.PICTURE, creationDate.getTime(), new int[]{width, height});
    }
}
