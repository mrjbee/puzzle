package org.monroe.team.puzzle.apps.mediabrowser.api.dto;

public class Tag {

    private String name;
    private String type;

    public Tag(final String name, final String type) {
        this.name = name;
        this.type = type;
        if (this.type == null){
            //fallback value
            this.type = "normal";
        }
    }

    public Tag() {}

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

}
