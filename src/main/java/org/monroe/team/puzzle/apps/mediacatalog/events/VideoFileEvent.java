package org.monroe.team.puzzle.apps.mediacatalog.events;

import org.monroe.team.puzzle.pieces.fs.events.FileEvent;

public class VideoFileEvent extends FileEvent{

    public VideoFileEvent(final String filePath, final String name, final String ext) {
        super(filePath, name, ext);
    }
}
