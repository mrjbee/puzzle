package org.monroe.team.puzzle.apps.mediabrowser.api.dto;

import java.util.List;

public class MediaResource {

    private final String id;
    private final String type;
    private final long creationDate;
    private final String name;
    private final int height;
    private final int width;
    private final List<Tag> tags;


    public MediaResource(final String id, final String type, final long creationDate, final String name, final int height, final int width, final List<Tag> tags) {
        this.id = id;
        this.type = type;
        this.creationDate = creationDate;
        this.name = name;
        this.height = height;
        this.width = width;
        this.tags = tags;
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

    public List<Tag> getTags() {
        return tags;
    }

}
