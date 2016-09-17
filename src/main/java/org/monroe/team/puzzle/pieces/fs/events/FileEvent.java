package org.monroe.team.puzzle.pieces.fs.events;

import org.monroe.team.puzzle.core.events.Event;

public class FileEvent extends Event {

    public final String filePath;
    public final String name;
    public final String ext;

    public FileEvent(final String filePath, final String name, final String ext) {
        this.filePath = filePath;
        this.name = name;
        this.ext = ext;
    }

    @Override
    public String toString() {
        return "FileEvent{" +
                "filePath='" + filePath + '\'' +
                ", name='" + name + '\'' +
                ", ext='" + ext + '\'' +
                '}';
    }

}
