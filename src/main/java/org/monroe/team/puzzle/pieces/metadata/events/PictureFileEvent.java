package org.monroe.team.puzzle.pieces.metadata.events;

import org.monroe.team.puzzle.pieces.fs.events.FileEvent;

public class PictureFileEvent extends FileEvent {
    public PictureFileEvent(final String filePath, final String name, final String ext) {
        super(filePath, name, ext);
    }
}
