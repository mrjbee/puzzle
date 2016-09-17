package org.monroe.team.puzzle.pieces.metadata.events;

import org.monroe.team.puzzle.pieces.fs.events.FileEvent;
import org.monroe.team.puzzle.pieces.metadata.MediaMetadata;

public class MediaFile extends FileEvent{

    public final MediaMetadata metadata;

    public MediaFile(final String filePath, final String fileName, final String fileExt, final MediaMetadata metadata) {
        super(filePath, fileName, fileExt);
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "MediaFile{" +
               "metadata=" + metadata +
                '}'+"->"+super.toString();
    }
}
