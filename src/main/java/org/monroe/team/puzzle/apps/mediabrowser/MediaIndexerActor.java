package org.monroe.team.puzzle.apps.mediabrowser;

import org.monroe.team.puzzle.apps.mediabrowser.indexer.MediaFileEntity;
import org.monroe.team.puzzle.apps.mediabrowser.indexer.MediaFileRepository;
import org.monroe.team.puzzle.core.events.AbstractMessageSubscriber;
import org.monroe.team.puzzle.pieces.fs.FileHasher;
import org.monroe.team.puzzle.pieces.metadata.events.MediaFileMessage;
import org.springframework.beans.factory.annotation.Autowired;

public class MediaIndexerActor extends AbstractMessageSubscriber<MediaFileMessage> {

    @Autowired
    Log log;

    @Autowired
    MediaFileRepository repository;

    @Autowired
    FileHasher fileHasher;

    @Override
    public void onMessage(final MediaFileMessage message) {
        long fileHash = fileHasher.hash(message.filePath);
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
