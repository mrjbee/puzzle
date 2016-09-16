package org.monroe.team.puzzle.pieces.metadata.events;

import org.monroe.team.puzzle.core.events.Event;

public class MediaFileMetadata extends Event{

    public enum Type {
        PICTURE, VIDEO
    }

    public final Type type;
    public final String filePath;
    public final String fileName;
    public final String fileExt;
    public final long creationDate;

    public MediaFileMetadata(final Type type, final String filePath, final String fileName, final String fileExt, final long creationDate) {
        this.type = type;
        this.filePath = filePath;
        this.fileName = fileName;
        this.fileExt = fileExt;
        this.creationDate = creationDate;
    }

    @Override
    public String toString() {
        return "MediaFileMetadata{" +
                "filePath='" + filePath + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileExt='" + fileExt + '\'' +
                ", creationDate=" + creationDate +
                '}';
    }
}
