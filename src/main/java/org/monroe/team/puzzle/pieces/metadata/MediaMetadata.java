package org.monroe.team.puzzle.pieces.metadata;

import java.util.Arrays;

public class MediaMetadata {

    public enum Type {
        PICTURE, VIDEO
    }

    public final Type type;
    public final long creationDate;
    public final int[] imageSize;

    public MediaMetadata(final Type type, final long creationDate, final int[] imageSize) {
        this.type = type;
        this.creationDate = creationDate;
        this.imageSize = imageSize;
    }

    @Override
    public String toString() {
        return "MediaMetadata{" +
                "type=" + type +
                ", creationDate=" + creationDate +
                ", imageSize=" + Arrays.toString(imageSize) +
                '}';
    }
}
