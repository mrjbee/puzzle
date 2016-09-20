package org.monroe.team.puzzle.pieces.fs;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.hibernate.validator.constraints.NotEmpty;
import org.monroe.team.puzzle.core.events.MessagePublisher;
import org.monroe.team.puzzle.core.fs.config.FolderPropertiesProvider;
import org.monroe.team.puzzle.core.log.Logs;
import org.monroe.team.puzzle.pieces.fs.events.FileMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
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
    MessagePublisher messagePublisher;

    @Autowired
    FolderPropertiesProvider folderPropertiesProvider;
    @Autowired
    TaskScheduler taskScheduler;

    @NotNull
    List<String> watchFolders;
    @NotNull
    Integer maxPublishAtOnce;
    @NotNull
    Integer newFileCacheTimeout;
    @NotNull
    Integer rate;
    @NotEmpty
    String chanel;

    @PostConstruct
    public void checkWatchFolders() {

        if (isFoldersNotSpecified()) {
            log.info("No watch folder specified = {}", "watchFolders[0]");
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
        taskScheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                checkForNewFiles();
            }
        }, rate);
    }

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

            FileMessage fileEvent = new FileMessage(childFile.getAbsolutePath(), name, ext);
            //"import.explored.file"
            messagePublisher.post(chanel,fileEvent);
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

    public Integer getRate() {
        return rate;
    }

    public void setRate(final Integer rate) {
        this.rate = rate;
    }

    public String getChanel() {
        return chanel;
    }

    public void setChanel(final String chanel) {
        this.chanel = chanel;
    }
}
