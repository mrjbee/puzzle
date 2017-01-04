package org.monroe.team.puzzle.apps.mediabrowser;

import org.monroe.team.puzzle.apps.mediabrowser.indexer.MediaFileRepository;
import org.monroe.team.puzzle.pieces.fs.FileHasher;
import org.monroe.team.puzzle.pieces.fs.FileWatchOperationLog;
import org.monroe.team.puzzle.pieces.fs.TempCachedFileWatchOperationLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.File;


@Component
@ConfigurationProperties(prefix="media.browser.watcher.operation.log", ignoreUnknownFields = true)
@Qualifier("index.based.operation.log")
public class IndexBasedFileWatchOperationLog extends TempCachedFileWatchOperationLog {

    @Autowired
    MediaFileRepository repository;
    @Autowired
    FileHasher fileHasher;

    @Override
    public boolean isFileWasLogged(final File file) {

        if (super.isFileWasLogged(file)){
            //did log less then 30 min ago
            return true;
        }

        long fileHash = fileHasher.hash(file.getAbsolutePath());
        return repository.exists(fileHash);
    }

    @Override
    public void markFileAsLogged(final File file) {
        super.markFileAsLogged(file);
    }
}
