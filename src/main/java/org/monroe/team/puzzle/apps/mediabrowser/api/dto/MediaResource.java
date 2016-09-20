package org.monroe.team.puzzle.apps.mediabrowser.api.dto;

public class MediaResource {

    private final long id;
    private final String type;
    private final long creationDate;
    private final String name;

    public MediaResource(final long id, final String type, final long creationDate, final String name) {
        this.id = id;
        this.type = type;
        this.creationDate = creationDate;
        this.name = name;
    }

    public long getId() {
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
}
