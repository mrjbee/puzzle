package org.monroe.team.puzzle.pieces.metadata;

import java.io.File;

public interface MediaFileMetadataExtractor {
    MediaMetadata metadata(File file);
}
