package org.monroe.team.puzzle.apps.mediabrowser;

import org.monroe.team.puzzle.apps.mediabrowser.indexer.MediaFileRepository;
import org.monroe.team.puzzle.pieces.fs.FileHasher;
import org.monroe.team.puzzle.pieces.fs.FileWatchOperationLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.File;


@Component
@Qualifier("index.based.operation.log")
public class IndexBasedFileWatchOperationLog implements FileWatchOperationLog {

    @Autowired
    MediaFileRepository repository;
    @Autowired
    FileHasher fileHasher;

    @Override
    public boolean isFileWasLogged(final File file) {
        long fileHash = fileHasher.hash(file.getAbsolutePath());
        return repository.exists(fileHash);
    }

    @Override
    public void markFileAsLogged(final File file) {
        //not required should be updated with index actor itself
    }
}
