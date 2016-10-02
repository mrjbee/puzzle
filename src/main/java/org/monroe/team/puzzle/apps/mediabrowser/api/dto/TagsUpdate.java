package org.monroe.team.puzzle.apps.mediabrowser.api.dto;

import java.util.List;

public class TagsUpdate {

    private List<Long> mediaIds;
    private List<Tag> assignTags;
    private List<Tag> removeTags;

    public List<Long> getMediaIds() {
        return mediaIds;
    }

    public void setMediaIds(final List<Long> mediaIds) {
        this.mediaIds = mediaIds;
    }

    public List<Tag> getAssignTags() {
        return assignTags;
    }

    public void setAssignTags(final List<Tag> assignTags) {
        this.assignTags = assignTags;
    }

    public List<Tag> getRemoveTags() {
        return removeTags;
    }

    public void setRemoveTags(final List<Tag> removeTags) {
        this.removeTags = removeTags;
    }
}
