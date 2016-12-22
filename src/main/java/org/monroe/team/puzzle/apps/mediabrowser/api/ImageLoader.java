package org.monroe.team.puzzle.apps.mediabrowser.api;

import com.google.common.cache.*;
import org.monroe.team.puzzle.core.log.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

@Component
public final class ImageLoader {

    private static int globalId = 0;
    private String logTag = "<IMAGE_CACHE-"+(globalId++)+">";

    private static Log log = new org.monroe.team.puzzle.core.log.Log("image-cache");

    @Value("${media.browser.image.cache.mb}")
    Long cacheMaxSize;
    Cache<String, CacheEntry> imageCache;

    @PostConstruct
    public void inti(){
       log.info(logTag + " Max size in KB = {}", cacheMaxSize * 1000);
       imageCache = CacheBuilder.<String, CacheEntry>newBuilder()
               .maximumWeight(cacheMaxSize * 1000)
               .weigher(new Weigher<String, CacheEntry>() {
                   @Override
                   public int weigh(final String key, final CacheEntry value) {
                       DataBuffer buff = value.image.getRaster().getDataBuffer();
                       int bytes = buff.getSize() * DataBuffer.getDataTypeSize(buff.getDataType()) / 8;
                       int kb = Math.round(bytes/1000);
                       log.info(logTag + " Weigh = {} for entry = {}", kb, key);
                       return kb;
                   }
               })
               .removalListener(new RemovalListener<String, CacheEntry>() {
                   @Override
                   public void onRemoval(final RemovalNotification<String, CacheEntry> notification) {
                       log.info(logTag + " Entry removed = {}", notification.getKey());
                   }
               })
               .build();
    }

    public BufferedImage readImage(final File file){
        try {
            return imageCache.get(file.getAbsolutePath(), new Callable<CacheEntry>() {
                @Override
                public CacheEntry call() throws Exception {
                    return new CacheEntry(ImageIO.read(file), file);
                }
            }).image;
        } catch (ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }


    final private static class CacheEntry{

        final BufferedImage image;
        final File file;

        public CacheEntry(final BufferedImage image, final File file) {
            this.image = image;
            this.file = file;
        }
    }

}
