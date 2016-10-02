package org.monroe.team.puzzle.apps.mediabrowser.tags;


import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import java.io.Serializable;

@Entity
public class MediaFileToTagLink {

    @EmbeddedId
    Key id;

    protected MediaFileToTagLink() {
    }

    public MediaFileToTagLink(Long mediaId, Long tagId) {
        id = new Key();
        id.setMediaId(mediaId);
        id.setTagId(tagId);
    }

    public Key getId() {
        return id;
    }

    protected void setId(final Key id) {
        this.id = id;
    }

    @Embeddable
    public static class Key implements Serializable {

        @Column(nullable = false)
        private Long mediaId;

        @Column(nullable = false)
        private Long tagId;

        public Key() {
        }

        public Key(final Long mediaId, final Long tagId) {
            this.mediaId = mediaId;
            this.tagId = tagId;
        }

        public Long getMediaId() {
            return mediaId;
        }

        public void setMediaId(final Long mediaId) {
            this.mediaId = mediaId;
        }

        public Long getTagId() {
            return tagId;
        }

        public void setTagId(final Long tagId) {
            this.tagId = tagId;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key key = (Key) o;

            if (mediaId != key.mediaId) return false;
            return tagId == key.tagId;

        }

        @Override
        public int hashCode() {
            int result = (int) (mediaId ^ (mediaId >>> 32));
            result = 31 * result + (int) (tagId ^ (tagId >>> 32));
            return result;
        }
    }
}
