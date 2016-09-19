package org.monroe.team.puzzle.apps.mediacatalog;

import org.hibernate.validator.constraints.NotBlank;
import org.monroe.team.puzzle.core.events.AbstractMessageSubscriber;
import org.monroe.team.puzzle.core.fs.config.FolderPropertiesProvider;
import org.monroe.team.puzzle.pieces.metadata.events.MediaFileMessage;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CatalogActor extends AbstractMessageSubscriber<MediaFileMessage> {

    @Autowired
    Log log;

    @Autowired
    FolderPropertiesProvider propertiesProvider;

    @NotBlank
    String folder;

    public CatalogActor() {
        super(MediaFileMessage.class);
    }

    @Override
    public void onMessage(final MediaFileMessage message) {
        File mediaFile = new File(message.filePath);
        File rootCatalogFolder = new File(folder);
        if (!rootCatalogFolder.exists() && !rootCatalogFolder.mkdirs()){
           log.warn("Could not create media catalog folder = "+rootCatalogFolder.getAbsolutePath());
        } else {
           Date date = new Date(message.metadata.creationDate);
           DateFormat df = new SimpleDateFormat("yyyy MMMM dd");
           String folderName = df.format(date);
           File mediaFolder = new File(rootCatalogFolder, folderName);
           if (!mediaFolder.exists() && !mediaFolder.mkdirs()){
               log.warn("Could not create media folder = "+mediaFolder.getAbsolutePath());
           } else {
                File newMediaFile = new File(mediaFolder, mediaFile.getName());
                boolean result = mediaFile.renameTo(newMediaFile);
                if (!result){
                    log.warn("Could not rename media from '{}' to '{}' ",
                            mediaFile.getAbsolutePath(),
                            newMediaFile.getAbsolutePath());
                } else {
                    log.info("Rename media from '{}' to '{}' ",
                            mediaFile.getAbsolutePath(),
                            newMediaFile.getAbsolutePath());
                }
           }
        }

    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(final String folder) {
        this.folder = folder;
    }
}
