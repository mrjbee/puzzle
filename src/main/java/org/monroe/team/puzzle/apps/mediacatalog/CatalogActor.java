package org.monroe.team.puzzle.apps.mediacatalog;

import org.monroe.team.puzzle.core.events.MbassyEventSubscriber;
import org.monroe.team.puzzle.core.fs.config.FolderPropertiesProvider;
import org.monroe.team.puzzle.pieces.metadata.events.MediaFileEvent;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class CatalogActor extends MbassyEventSubscriber<MediaFileEvent> {

    @Autowired
    Log log;

    @Autowired
    FolderPropertiesProvider propertiesProvider;

    public CatalogActor() {
        super(MediaFileEvent.class);
    }

    @Override
    public void onEvent(final MediaFileEvent event) {
        File mediaFile = new File(event.filePath);
        Properties conf = propertiesProvider.forFolder(mediaFile.getParentFile());
        String rootMediaFolder = conf.getProperty("media-catalog.root.folder");
        if (rootMediaFolder == null){
            log.warn("No media catalog folder defined in {} for resource: {} ","media-catalog.root.folder", event.filePath);
        } else {
           File rootCatalogFolder = new File(rootMediaFolder);
           if (!rootCatalogFolder.exists() && !rootCatalogFolder.mkdirs()){
               log.warn("Could not create media catalog folder = "+rootCatalogFolder.getAbsolutePath());
           } else {
               Date date = new Date(event.metadata.creationDate);
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
    }
}
