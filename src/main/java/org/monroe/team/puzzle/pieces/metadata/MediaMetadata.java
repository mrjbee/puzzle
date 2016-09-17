package org.monroe.team.puzzle.pieces.metadata;

public class MediaMetadata {

    public enum Type {
        PICTURE, VIDEO
    }

    public final Type type;
    public final long creationDate;

    public MediaMetadata(final Type type, final long creationDate) {
        this.type = type;
        this.creationDate = creationDate;
    }

    @Override
    public String toString() {
        return "MediaMetadata{" +
                "creationDate=" + creationDate +
                '}';
    }
}
