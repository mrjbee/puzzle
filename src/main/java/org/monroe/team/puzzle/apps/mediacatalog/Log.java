package org.monroe.team.puzzle.apps.mediacatalog;

import org.springframework.stereotype.Component;

@Component("media-catalog-log")
public class Log extends org.monroe.team.puzzle.core.log.Log {
    Log() {
        super("media-catalog");
    }
}
