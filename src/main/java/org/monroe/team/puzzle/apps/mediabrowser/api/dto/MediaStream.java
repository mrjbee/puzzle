package org.monroe.team.puzzle.apps.mediabrowser.api.dto;

import java.util.List;

public class MediaStream {

    private final Paging paging;
    private final List<MediaResource> mediaResourceIds;

    public MediaStream(final Paging paging, final List<MediaResource> mediaResourceIds) {
        this.paging = paging;
        this.mediaResourceIds = mediaResourceIds;
    }

    public Paging getPaging() {
        return paging;
    }

    public List<MediaResource> getMediaResourceIds() {
        return mediaResourceIds;
    }
}
