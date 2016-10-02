package org.monroe.team.puzzle.apps.mediabrowser.api.dto;

import org.monroe.team.puzzle.apps.mediabrowser.indexer.MediaFileEntity;

import javax.persistence.ManyToMany;
import java.util.Set;

public class Tag {

    private String name;
    private String color;

    public Tag(final String name, final String color) {
        this.name = name;
        this.color = color;
    }

    public Tag() {
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(final String color) {
        this.color = color;
    }

}
