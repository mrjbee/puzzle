package org.monroe.team.puzzle.pieces.metadata.events;

import org.monroe.team.puzzle.pieces.fs.events.FileMessage;
import org.monroe.team.puzzle.pieces.metadata.MediaMetadata;

public class MediaFileMessage extends FileMessage {

    public final MediaMetadata metadata;

    public MediaFileMessage(final String filePath, final String fileName, final String fileExt, final MediaMetadata metadata) {
        super(filePath, fileName, fileExt);
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "MediaFileMessage{" +
               "metadata=" + metadata +
                '}'+"->"+super.toString();
    }
}
