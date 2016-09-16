package org.monroe.team.puzzle.pieces.filewatcher.events;

import org.monroe.team.puzzle.core.events.Event;

import java.io.File;
import java.io.InputStream;

public class NewFileEvent extends Event {

    public final String filePath;
    public final String name;
    public final String ext;

    public NewFileEvent(final String filePath, final String name, final String ext) {
        this.filePath = filePath;
        this.name = name;
        this.ext = ext;
    }

    @Override
    public String toString() {
        return "NewFileEvent{" +
                "filePath='" + filePath + '\'' +
                ", name='" + name + '\'' +
                ", ext='" + ext + '\'' +
                '}';
    }

}
