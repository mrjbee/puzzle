package org.monroe.team.puzzle.apps.mediacatalog.events;

import org.monroe.team.puzzle.pieces.fs.events.FileMessage;

public class VideoFileMessage extends FileMessage {

    public VideoFileMessage(final String filePath, final String name, final String ext) {
        super(filePath, name, ext);
    }

    @Override
    public String toString() {
        return "VideoFileMessage{"+super.toString()+"}";
    }
}
