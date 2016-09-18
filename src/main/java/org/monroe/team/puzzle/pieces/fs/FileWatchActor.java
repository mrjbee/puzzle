package org.monroe.team.puzzle.pieces.fs;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.monroe.team.puzzle.core.events.EventBus;
import org.monroe.team.puzzle.core.fs.config.FolderPropertiesProvider;
import org.monroe.team.puzzle.core.log.Logs;
import org.monroe.team.puzzle.pieces.fs.events.FileEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.nio.file.FileSystems;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class FileWatchActor {

    @Autowired
    Log log;

    private Cache<String, Boolean> exploredFileCache;

    @Autowired
    EventBus eventBus;

    @Autowired
    FolderPropertiesProvider folderPropertiesProvider;

    @NotNull
    List<String> watchFolders;
    @NotNull
    Integer maxPublishAtOnce;
    @NotNull
    Integer newFileCacheTimeout;


    @PostConstruct
    public void checkWatchFolders() {

        if (isFoldersNotSpecified()) {
            log.info("No watch folder specified = {}", "piece.filewatcher.watch.folder.list");
            watchFolders.clear();
        } else {
            for (String watchFolder : watchFolders) {
                File watchFolderFile = new File(watchFolder);
                if (!watchFolderFile.exists()) {
                    log.warn("Watch folder doesnt exists = {}", watchFolderFile.getAbsolutePath());
                }
            }
        }
        exploredFileCache = CacheBuilder.<String, Boolean>newBuilder().expireAfterWrite(newFileCacheTimeout, TimeUnit.MILLISECONDS).build();
    }

    @Scheduled(fixedRateString = "${piece.filewatcher.watch.rate.ms}")
    public void checkForNewFiles() {
        Logs.resetTransactionId("tm");
        if (!isFoldersNotSpecified()) {
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
        if (childFiles != null) {
            for (File childFile : childFiles) {

                if (published >= maxAllowedToPublishAtOnce) {
                    return published;
                }

                if (childFile.isDirectory()) {
                    published = traversFolderForANewFile(childFile, published, maxAllowedToPublishAtOnce);
                } else {
                    boolean isPublished = publishNewFileIfNotAlreadyPublished(childFile);
                    if (isPublished) {
                        published++;
                    }
                }
            }
        }
        return published;
    }

    private boolean publishNewFileIfNotAlreadyPublished(final File childFile) {

        if (childFile.getName().equals(".puzzleconf")) {
            exploredFileCache.put(childFile.getAbsolutePath(), true);
            return false;
        }

        String filePath = childFile.getAbsolutePath();
        Boolean alreadyDiscovered = exploredFileCache.getIfPresent(filePath);
        if (alreadyDiscovered == null || !alreadyDiscovered) {
            String fullName = childFile.getName();

            Properties conf = folderPropertiesProvider.forFolder(childFile.getParentFile());
            if (conf.getProperty("exclude") != null) {
                for (String exclude : conf.getProperty("exclude").split("\\|\\|")) {
                    if (FileSystems.getDefault().getPathMatcher(exclude).matches(new File(fullName).toPath())) {
                        exploredFileCache.put(childFile.getAbsolutePath(), true);
                        return false;
                    }
                }
            }

            String name = fullName;
            String ext = null;
            int extensionDotPosition = fullName.lastIndexOf(".");
            if (extensionDotPosition > 0) {
                name = fullName.substring(0, extensionDotPosition);
                ext = fullName.substring(extensionDotPosition);
            }

            FileEvent fileEvent = new FileEvent(childFile.getAbsolutePath(), name, ext);
            eventBus.post(fileEvent);
            exploredFileCache.put(fileEvent.filePath, true);
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
