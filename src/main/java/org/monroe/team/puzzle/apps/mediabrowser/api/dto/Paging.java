package org.monroe.team.puzzle.apps.mediabrowser.api.dto;

public class Paging {

    private final long offset;
    private final long limit;
    private final long actualCount;

    public Paging(final long offset, final long limit, final long actualCount) {
        this.offset = offset;
        this.limit = limit;
        this.actualCount = actualCount;
    }

    public long getOffset() {
        return offset;
    }

    public long getLimit() {
        return limit;
    }

    public long getActualCount() {
        return actualCount;
    }
}
