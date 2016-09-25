package org.monroe.team.puzzle.apps.mediabrowser.api;

import org.monroe.team.puzzle.apps.mediabrowser.api.dto.MediaResource;
import org.monroe.team.puzzle.apps.mediabrowser.api.dto.MediaStream;
import org.monroe.team.puzzle.apps.mediabrowser.api.dto.Paging;
import org.monroe.team.puzzle.apps.mediabrowser.indexer.MediaFileEntity;
import org.monroe.team.puzzle.apps.mediabrowser.indexer.MediaFileRepository;
import org.monroe.team.puzzle.pieces.fs.LinuxVideoFileMetadataExtractor;
import org.monroe.team.puzzle.pieces.metadata.MediaMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class BrowserApi {

    @Autowired
    MediaFileRepository repository;
    @Autowired
    LinuxVideoFileMetadataExtractor linuxVideoFileMetadataExtractor;

    @RequestMapping(value = "/media-stream", method = RequestMethod.GET)
    public MediaStream mediaStream(
            @RequestParam(value="offset", defaultValue="0") Integer offset,
            @RequestParam(value="limit", defaultValue="100") Integer limit) {

        PageRequest pageRequest = new PageRequest(
                offset, limit, Sort.Direction.DESC, "creationDate"
        );

        Page<MediaFileEntity> mediaFileEntityList = repository.findAll(pageRequest);
        List<MediaResource> answerList = mediaFileEntityList.map(new Converter<MediaFileEntity, MediaResource>() {
            @Override
            public MediaResource convert(final MediaFileEntity source) {
                return new MediaResource(
                        Long.toString(source.getId()),
                        source.getType().name(),
                        source.getCreationDate(),
                        new File(source.getFileName()).getName(),
                        source.getHeight(),
                        source.getWidth());
            }
        }).getContent();

        return new MediaStream(
                new Paging(
                        offset,
                        limit,
                        answerList.size()
                ),
                answerList
        );
    }


    @RequestMapping(value = "thumbnail/{mediaId}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity getThumbnail(
            @PathVariable Long mediaId,
            @RequestParam(value="width", required=false) Integer width,
            @RequestParam(value="height", required=false) Integer height) {

        MediaFileEntity mediaFileEntity = repository.findOne(mediaId);
        if (mediaFileEntity == null){
            return ResponseEntity.notFound().build();
        }
        File file = new File(mediaFileEntity.getFileName());
        if (mediaFileEntity.getType() == MediaMetadata.Type.VIDEO){
            file = linuxVideoFileMetadataExtractor.getVideoThumbnail(file);
        }

        if (!file.exists()){
            return ResponseEntity.notFound().build();
        }

        if (width == null || height == null){
            try {
                return ResponseEntity.ok()
                        .contentLength(file.length())
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(new InputStreamResource(new FileInputStream(file)));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                BufferedImage originalImage = ImageIO.read(file);
                int originalWidth = originalImage.getWidth();
                int originalHeight = originalImage.getHeight();
                Dimension outputSize =new Dimension(100, 100);

                float originalAspectRation = (float) originalWidth/(float) originalHeight;
                float aspectRation = (float) width/(float) height;

                float widthDiff =  (float)width/(float)originalWidth;
                float heightDiff =  (float)height/(float)originalHeight;

                if (widthDiff > heightDiff){
                    outputSize = new Dimension(
                            Math.round(originalWidth * widthDiff),
                            Math.round(originalHeight * widthDiff )
                    );
                } else {
                    outputSize = new Dimension(
                            Math.round(originalWidth * heightDiff  ),
                            Math.round(originalHeight * heightDiff)
                    );
                }

                BufferedImage resizedImage = new BufferedImage(
                        width,
                        height,
                        BufferedImage.TYPE_INT_RGB);
                Graphics2D g = resizedImage.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

                int xOffset = (int) ((outputSize.getWidth() - width) / 2);
                int yOffset = (int) ((outputSize.getHeight() - height) / 2);

                g.drawImage(originalImage, -xOffset, -yOffset,
                        outputSize.width,
                        outputSize.height, null);
                g.dispose();

                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(resizedImage, "jpg", os);
                return ResponseEntity.ok()
                        .contentLength(os.size())
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(new InputStreamResource(new ByteArrayInputStream(os.toByteArray())));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
