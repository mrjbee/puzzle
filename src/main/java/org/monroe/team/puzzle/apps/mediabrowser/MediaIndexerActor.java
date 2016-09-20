package org.monroe.team.puzzle.apps.mediabrowser;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import org.monroe.team.puzzle.apps.mediabrowser.indexer.MediaFileEntity;
import org.monroe.team.puzzle.apps.mediabrowser.indexer.MediaFileRepository;
import org.monroe.team.puzzle.core.events.AbstractMessageSubscriber;
import org.monroe.team.puzzle.pieces.metadata.events.MediaFileMessage;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.Charset;

public class MediaIndexerActor extends AbstractMessageSubscriber<MediaFileMessage> {

    @Autowired
    Log log;

    @Autowired
    MediaFileRepository repository;

    @Override
    public void onMessage(final MediaFileMessage message) {
        long fileHash = Hashing.md5().hashString(message.filePath, Charset.defaultCharset()).asLong();
        if (!repository.exists(fileHash)) {
            MediaFileEntity entity = repository.save(new MediaFileEntity(
                    fileHash,
                    message.filePath,
                    message.metadata.creationDate,
                    message.metadata.type
            ));
            log.info("Metadata saved as id = {} : {}",fileHash, entity);
        } else {
            log.info("Metadata with id = {} skipped. Message : {}",fileHash, message);
        }
    }
}
