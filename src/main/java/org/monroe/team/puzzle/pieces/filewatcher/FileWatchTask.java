package org.monroe.team.puzzle.pieces.filewatcher;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.engio.mbassy.bus.MBassador;
import org.monroe.team.puzzle.core.events.Event;
import org.monroe.team.puzzle.core.events.EventBus;
import org.monroe.team.puzzle.core.logs.Log;
import org.monroe.team.puzzle.core.logs.Logs;
import org.monroe.team.puzzle.pieces.filewatcher.events.NewFileEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

@EnableScheduling
@Component
public class FileWatchTask {

    Log log = Logs.piece("filewatcher");

    @Autowired
    EventBus eventBus;

    @Value("#{'${piece.filewatcher.watch.folder.list}'.split(';')}")
    private List<String> watchFolderPathList;

    @Value("${piece.filewatcher.watch.max.publish.at.once:2}")
    private int maxPublishAtOnce;

    private Cache<String, Boolean> exploredFileCache;

    @PostConstruct
    public void checkWatchFolders(){
        if (isFoldersNotSpecified()){
            log.info("No watch folder specified = {}", "piece.filewatcher.watch.folder.list");
            watchFolderPathList.clear();
        }else {
            for (String watchFolder : watchFolderPathList) {
                File watchFolderFile = new File(watchFolder);
                if (!watchFolderFile.exists()){
                    log.warn("Watch folder doesnt exists = {}", watchFolderFile.getAbsolutePath());
                }
            }
        }
        exploredFileCache = CacheBuilder.<String,Boolean>newBuilder().expireAfterWrite(5000, TimeUnit.MILLISECONDS).build();
    }

    @Scheduled(fixedRateString = "${piece.filewatcher.watch.rate.ms}")
    public void checkForNewFiles() {
        Logs.resetTransactionId("tm");
        if (!isFoldersNotSpecified()){
            for (String watchFolder : watchFolderPathList) {
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
        return watchFolderPathList.isEmpty() ||
                (watchFolderPathList.size() == 1 && watchFolderPathList.get(0).trim().isEmpty());
    }
}
