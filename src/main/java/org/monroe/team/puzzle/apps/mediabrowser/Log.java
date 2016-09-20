package org.monroe.team.puzzle.apps.mediabrowser;

import org.springframework.stereotype.Component;

@Component("media-browser-log")
public class Log extends org.monroe.team.puzzle.core.log.Log {
    Log() {
        super("media-browser");
    }
}
