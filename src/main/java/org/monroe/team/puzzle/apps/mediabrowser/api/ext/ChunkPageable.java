package org.monroe.team.puzzle.apps.mediabrowser.api.ext;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class ChunkPageable implements Pageable {

    private int limit;
    private int offset;
    private final Sort sort;

    /**
     * Creates a new {@link ChunkPageable} with sort parameters applied.
     *
     * @param offset zero-based offset.
     * @param limit  the size of the elements to be returned.
     * @param sort   can be {@literal null}.
     */
    public ChunkPageable(int offset, int limit, Sort sort) {
        if (offset < 0) {
            throw new IllegalArgumentException("Offset index must not be less than zero!");
        }

        if (limit < 1) {
            throw new IllegalArgumentException("Limit must not be less than one!");
        }
        this.limit = limit;
        this.offset = offset;
        this.sort = sort;
    }

    /**
     * Creates a new {@link ChunkPageable} with sort parameters applied.
     *
     * @param offset     zero-based offset.
     * @param limit      the size of the elements to be returned.
     * @param direction  the direction of the {@link Sort} to be specified, can be {@literal null}.
     * @param properties the properties to sort by, must not be {@literal null} or empty.
     */
    public ChunkPageable(int offset, int limit, Sort.Direction direction, String... properties) {
        this(offset, limit, new Sort(direction, properties));
    }

    /**
     * Creates a new {@link ChunkPageable} with sort parameters applied.
     *
     * @param offset zero-based offset.
     * @param limit  the size of the elements to be returned.
     */
    public ChunkPageable(int offset, int limit) {
        this(offset, limit, new Sort(Sort.Direction.ASC,"id"));
    }

    @Override
    public int getPageNumber() {
        return offset / limit;
    }

    @Override
    public int getPageSize() {
        return limit;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public Sort getSort() {
        return sort;
    }

    @Override
    public Pageable next() {
        return new ChunkPageable(getOffset() + getPageSize(), getPageSize(), getSort());
    }

    public ChunkPageable previous() {
        return hasPrevious() ? new ChunkPageable(getOffset() - getPageSize(), getPageSize(), getSort()) : this;
    }


    @Override
    public Pageable previousOrFirst() {
        return hasPrevious() ? previous() : first();
    }

    @Override
    public Pageable first() {
        return new ChunkPageable(0, getPageSize(), getSort());
    }

    @Override
    public boolean hasPrevious() {
        return offset > limit;
    }


}
