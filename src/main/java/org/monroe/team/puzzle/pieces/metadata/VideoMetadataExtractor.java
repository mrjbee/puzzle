package org.monroe.team.puzzle.pieces.metadata;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.MovieBox;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.googlecode.mp4parser.util.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Date;

@Component
public class VideoMetadataExtractor implements MediaFileMetadataExtractor{

    @Autowired
    Log log;

    @Override
    public MediaMetadata metadata(final File file) {
        IsoFile isoFile = null;
        try {
            isoFile = new IsoFile(file.getAbsolutePath());
            Object nam = Path.getPath(isoFile, "/moov");
            Date date = ((MovieBox) nam).getMovieHeaderBox().getCreationTime();
            if (date == null){
                log.warn("Failed to get creation date. Use now() date for = {}", file.getAbsoluteFile());
                date = new Date();
            }
            return new MediaMetadata(MediaMetadata.Type.VIDEO, date.getTime());
        } catch (IOException e) {
            log.warn(e, "Failed to open file as ISO = {}", file.getAbsoluteFile());
            throw new RuntimeException(e);
        } finally {
            try {
                if (isoFile !=null){
                    isoFile.close();
                }
            } catch (IOException e) {
                log.warn(e, "Failed to close file = {}", file.getAbsoluteFile());
            }
        }

    }
}
