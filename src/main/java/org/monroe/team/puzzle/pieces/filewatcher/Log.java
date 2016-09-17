package org.monroe.team.puzzle.pieces.filewatcher;

import org.springframework.stereotype.Component;

@Component
public class Log extends org.monroe.team.puzzle.core.logs.Log {
    Log() {
        super("file-watcher");
    }
}
