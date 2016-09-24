package org.monroe.team.puzzle.apps.mediabrowser.indexer;

import org.monroe.team.puzzle.pieces.metadata.MediaMetadata;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

@Entity
public class MediaFileEntity {

    @Id
    private Long id;
    private String fileName;
    private Long creationDate;
    private int width;
    private int height;

    @Enumerated(EnumType.STRING)
    private MediaMetadata.Type type;

    protected MediaFileEntity() {}

    public MediaFileEntity(final Long id,
                           final String fileName,
                           final Long creationDate,
                           final MediaMetadata.Type type,
                           final int width,
                           final int height) {
        this.id = id;
        this.fileName = fileName;
        this.creationDate = creationDate;
        this.type = type;
        this.width = width;
        this.height = height;
    }

    public Long getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public Long getCreationDate() {
        return creationDate;
    }

    public MediaMetadata.Type getType() {
        return type;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(final int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(final int height) {
        this.height = height;
    }

    @Override
    public String toString() {
        return "MediaFileEntity{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", creationDate=" + creationDate +
                ", type=" + type +
                '}';
    }
}
