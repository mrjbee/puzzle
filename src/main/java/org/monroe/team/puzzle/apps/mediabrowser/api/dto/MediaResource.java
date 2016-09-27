package org.monroe.team.puzzle.apps.mediabrowser.api.dto;

public class MediaResource {

    private final String id;
    private final String type;
    private final long creationDate;
    private final String name;
    private final int height;
    private final int width;


    public MediaResource(final String id, final String type, final long creationDate, final String name, final int height, final int width) {
        this.id = id;
        this.type = type;
        this.creationDate = creationDate;
        this.name = name;
        this.height = height;
        this.width = width;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public String getName() {
        return name;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }
}
