package org.monroe.team.puzzle.pieces.filewatcher;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.monroe.team.puzzle.core.events.EventBus;
import org.monroe.team.puzzle.core.logs.Log;
import org.monroe.team.puzzle.core.logs.Logs;
import org.monroe.team.puzzle.pieces.filewatcher.events.NewFileEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FileWatchTask {

    final static Log log = Logs.piece("filewatcher");

    private Cache<String, Boolean> exploredFileCache;

    @Autowired
    EventBus eventBus;

    @NotNull
    List<String> watchFolders;
    @NotNull
    Integer maxPublishAtOnce;
    @NotNull
    Integer newFileCacheTimeout;


    @PostConstruct
    public void checkWatchFolders(){

        if (isFoldersNotSpecified()){
            log.info("No watch folder specified = {}", "piece.filewatcher.watch.folder.list");
            watchFolders.clear();
        }else {
            for (String watchFolder : watchFolders) {
                File watchFolderFile = new File(watchFolder);
                if (!watchFolderFile.exists()){
                    log.warn("Watch folder doesnt exists = {}", watchFolderFile.getAbsolutePath());
                }
            }
        }
        exploredFileCache = CacheBuilder.<String,Boolean>newBuilder().expireAfterWrite(newFileCacheTimeout, TimeUnit.MILLISECONDS).build();
    }

    @Scheduled(fixedRateString = "${piece.filewatcher.watch.rate.ms}")
    public void checkForNewFiles() {
        Logs.resetTransactionId("tm");
        if (!isFoldersNotSpecified()){
            for (String watchFolder : watchFolders) {
                File watchFolderFile = new File(watchFolder);
                int publicationCount = traversFolderForANewFile(watchFolderFile, 0, maxPublishAtOnce);
            }
        }
    }

    private int traversFolderForANewFile(final File file,
                                          int published,
                                          final int maxAllowedToPublishAtOnce) {
        if (!file.exists()) return published;
        File[] childFiles = file.listFiles();
        for (File childFile : childFiles) {

            if (published >= maxAllowedToPublishAtOnce){
                return published;
            }

            if (childFile.isDirectory()){
                published = traversFolderForANewFile(childFile, published, maxAllowedToPublishAtOnce);
            }else {
                boolean isPublished = publishNewFileIfNotAlreadyPublished(childFile);
                if (isPublished) {
                    published++;
                }
            }
        }
        return published;
    }

    private boolean publishNewFileIfNotAlreadyPublished(final File childFile) {
        String filePath = childFile.getAbsolutePath();
        Boolean alreadyDiscovered = exploredFileCache.getIfPresent(filePath);
        if (alreadyDiscovered == null || !alreadyDiscovered){
            String fullName = childFile.getName();
            String name = fullName;
            String ext = null;
            int extensionDotPosition = fullName.lastIndexOf(".");
            if (extensionDotPosition > 0){
                name = fullName.substring(0, extensionDotPosition);
                ext = fullName.substring(extensionDotPosition);
            }
            NewFileEvent newFileEvent = new NewFileEvent(childFile.getAbsolutePath(), name, ext);
            eventBus.post(newFileEvent);
            exploredFileCache.put(newFileEvent.filePath, true);
            return true;
        } else {
            return false;
        }
    }

    private boolean isFoldersNotSpecified() {
        return watchFolders.isEmpty() ||
                (watchFolders.size() == 1 && watchFolders.get(0).trim().isEmpty());
    }

    public List<String> getWatchFolders() {
        return watchFolders;
    }

    public void setWatchFolders(final List<String> watchFolders) {
        this.watchFolders = watchFolders;
    }

    public Integer getMaxPublishAtOnce() {
        return maxPublishAtOnce;
    }

    public void setMaxPublishAtOnce(final Integer maxPublishAtOnce) {
        this.maxPublishAtOnce = maxPublishAtOnce;
    }

    public Integer getNewFileCacheTimeout() {
        return newFileCacheTimeout;
    }

    public void setNewFileCacheTimeout(final Integer newFileCacheTimeout) {
        this.newFileCacheTimeout = newFileCacheTimeout;
    }
}
