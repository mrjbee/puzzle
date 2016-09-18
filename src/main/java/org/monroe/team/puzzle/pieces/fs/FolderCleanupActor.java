package org.monroe.team.puzzle.pieces.fs;

import org.monroe.team.puzzle.core.events.MessagePublisher;
import org.monroe.team.puzzle.core.fs.config.FolderPropertiesProvider;
import org.monroe.team.puzzle.core.log.Logs;
import org.monroe.team.puzzle.pieces.fs.events.FileMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.nio.file.FileSystems;
import java.util.List;
import java.util.Properties;

public class FolderCleanupActor {

    @Autowired
    Log log;

    @Autowired
    MessagePublisher messagePublisher;

    @Autowired
    FolderPropertiesProvider folderPropertiesProvider;

    @NotNull
    List<String> watchFolders;

    @PostConstruct
    public void checkWatchFolders() {

        if (isFoldersNotSpecified()) {
            log.info("No watch folder for cleanup specified = {}", "watchFolders[0]");
            watchFolders.clear();
        } else {
            for (String watchFolder : watchFolders) {
                File watchFolderFile = new File(watchFolder);
                if (!watchFolderFile.exists()) {
                    log.warn("Watch cleanup folder doesnt exists = {}", watchFolderFile.getAbsolutePath());
                }
            }
        }
    }

    @Scheduled(fixedRateString = "${piece.filewatcher.watch.rate.ms}")
    public void checkForEmptyFolders() {
        Logs.resetTransactionId("tm");
        if (!isFoldersNotSpecified()) {
            for (String watchFolder : watchFolders) {
                File watchFolderFile = new File(watchFolder);
                traversFolderForAnEmptyFolder(watchFolderFile, false);
            }
        }
    }

    private void traversFolderForAnEmptyFolder(final File file, boolean removeIfPossible) {
        if (!file.exists()) return;
        File[] childFiles = file.listFiles();
        if (childFiles != null) {
            for (File childFile : childFiles) {
                if (childFile.isDirectory()) {
                    traversFolderForAnEmptyFolder(childFile, true);
                }
            }
        }

        childFiles = file.listFiles();
        if (removeIfPossible && file.isDirectory() &&
                (childFiles == null || childFiles.length == 0)){
            file.delete();
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

}
