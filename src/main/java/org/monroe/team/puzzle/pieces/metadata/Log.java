package org.monroe.team.puzzle.pieces.metadata;

import org.springframework.stereotype.Component;

@Component("media-metadata")
public class Log extends org.monroe.team.puzzle.core.log.Log {
    Log() {
        super("media-metadata");
    }
}
