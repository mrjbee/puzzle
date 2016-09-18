package org.monroe.team.puzzle.pieces.fs;

import org.springframework.stereotype.Component;

@Component
public class Log extends org.monroe.team.puzzle.core.log.Log {
    Log() {
        super("file-system");
    }
}
