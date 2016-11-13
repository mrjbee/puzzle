package org.monroe.team.puzzle.apps.mediabrowser.api;

import com.google.common.cache.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

@Component
public final class ImageLoader {

    @Value("${media.browser.image.cache.mb}")
    Long cacheMaxSize;
    Cache<String, CacheEntry> imageCache;

    @PostConstruct
    public void inti(){
       imageCache = CacheBuilder.<String, CacheEntry>newBuilder()
               .maximumWeight(cacheMaxSize)
               .weigher(new Weigher<String, CacheEntry>() {
                   @Override
                   public int weigh(final String key, final CacheEntry value) {
                       int answer = 0;
                       if (value.file.length() == 0){
                           answer = Math.round(
                                   (value.image.getWidth() *
                                   value.image.getHeight() * 4 * 4 * 4 * 4) / 1000000);
                       } else {
                           answer = Math.round(value.file.length() / 1000000);
                       }
                       return answer == 0? 1: answer;
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
