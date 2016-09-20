package org.monroe.team.puzzle.apps.mediabrowser;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import org.monroe.team.puzzle.core.events.AbstractMessageSubscriber;
import org.monroe.team.puzzle.pieces.metadata.events.MediaFileMessage;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.Charset;

public class MediaIndexerActor extends AbstractMessageSubscriber<MediaFileMessage> {

    @Autowired
    Log log;

    @Override
    public void onMessage(final MediaFileMessage message) {
        long fileHash = Hashing.md5().hashString(message.filePath, Charset.defaultCharset()).asLong();
        log.info("Accept metadata for id = {} : {}",fileHash, message);
    }
}
