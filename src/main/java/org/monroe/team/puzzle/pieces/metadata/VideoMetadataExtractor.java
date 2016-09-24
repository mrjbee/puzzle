package org.monroe.team.puzzle.pieces.metadata;

import org.monroe.team.puzzle.pieces.fs.LinuxVideoFileMetadataExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Date;

@Component
public class VideoMetadataExtractor implements MediaFileMetadataExtractor{

    @Autowired
    Log log;

    @Autowired
    LinuxVideoFileMetadataExtractor metadataExtractor;

    @Override
    public MediaMetadata metadata(final File file) {
         Date date = metadataExtractor.creationDate(file);
         return new MediaMetadata(MediaMetadata.Type.VIDEO, date.getTime());
    }
}
