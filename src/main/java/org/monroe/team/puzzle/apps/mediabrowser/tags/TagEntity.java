package org.monroe.team.puzzle.apps.mediabrowser.tags;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class TagEntity {

    @Id
    private Long id;
    private String title;
    private String type;

    protected TagEntity() {
    }

    public TagEntity(final String title, final String type) {
        this.title = title;
        this.type = type;
        this.id = (long) title.hashCode();
    }

    public Long getId() {
        return id;
    }

    protected void setId(final Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    protected void setTitle(final String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    protected void setType(final String type) {
        this.type = type;
    }
}
