package org.monroe.team.puzzle.pieces.fs;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.util.concurrent.TimeUnit;

public class TempCachedFileWatchOperationLog implements FileWatchOperationLog{

    private Cache<String, Boolean> exploredFileCache;

    @NotNull
    Integer newFileCacheTimeout;

    @PostConstruct
    public void intiCache(){
        exploredFileCache = CacheBuilder.<String, Boolean>newBuilder().expireAfterWrite(newFileCacheTimeout, TimeUnit.MILLISECONDS).build();
    }

    @Override
    public boolean isFileWasLogged(final File file) {
        Boolean alreadyDiscovered = exploredFileCache.getIfPresent(file.getAbsolutePath());
        return (alreadyDiscovered != null && alreadyDiscovered);
    }

    @Override
    public void markFileAsLogged(final File file) {
        exploredFileCache.put(file.getAbsolutePath(), true);
    }

    public Integer getNewFileCacheTimeout() {
        return newFileCacheTimeout;
    }

    public void setNewFileCacheTimeout(final Integer newFileCacheTimeout) {
        this.newFileCacheTimeout = newFileCacheTimeout;
    }
}
