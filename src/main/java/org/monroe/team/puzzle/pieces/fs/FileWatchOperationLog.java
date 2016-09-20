package org.monroe.team.puzzle.pieces.fs;

import java.io.File;

public interface FileWatchOperationLog {
    boolean isFileWasLogged(File file);
    void markFileAsLogged(File file);
}
