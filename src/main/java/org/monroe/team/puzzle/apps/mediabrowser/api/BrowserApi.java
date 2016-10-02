package org.monroe.team.puzzle.apps.mediabrowser.api;

import org.monroe.team.puzzle.apps.mediabrowser.api.dto.*;
import org.monroe.team.puzzle.apps.mediabrowser.api.ext.ChunkPageable;
import org.monroe.team.puzzle.apps.mediabrowser.indexer.MediaFileEntity;
import org.monroe.team.puzzle.apps.mediabrowser.indexer.MediaFileRepository;
import org.monroe.team.puzzle.apps.mediabrowser.tags.MediaFileToTagLink;
import org.monroe.team.puzzle.apps.mediabrowser.tags.MediaFileToTagLinkRepository;
import org.monroe.team.puzzle.apps.mediabrowser.tags.TagEntity;
import org.monroe.team.puzzle.apps.mediabrowser.tags.TagRepository;
import org.monroe.team.puzzle.pieces.fs.LinuxVideoFileMetadataExtractor;
import org.monroe.team.puzzle.pieces.metadata.MediaMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api")
public class BrowserApi {

    @Autowired
    MediaFileRepository mediFileRepository;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    MediaFileToTagLinkRepository mediaFileToTagLinkRepository;

    @Autowired
    LinuxVideoFileMetadataExtractor linuxVideoFileMetadataExtractor;

    private BufferedImage playImage;

    @PostConstruct
    public void initializeResources() {
        try {
            playImage = ImageIO.read(this.getClass().getClassLoader().getResource("video-play-icon.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @RequestMapping(value = "/media-stream", method = RequestMethod.GET)
    public MediaStream mediaStream(
            @RequestParam(value = "offset", defaultValue = "0") Integer offset,
            @RequestParam(value = "limit", defaultValue = "100") Integer limit) {

        final Pageable pageRequest = new ChunkPageable(
                offset, limit, Sort.Direction.DESC, "creationDate"
        );

        Page<MediaFileEntity> mediaFileEntityList = mediFileRepository.findAll(pageRequest);
        List<MediaResource> answerList = mediaFileEntityList.map(new Converter<MediaFileEntity, MediaResource>() {
            @Override
            public MediaResource convert(final MediaFileEntity source) {

                List<MediaFileToTagLink> tagLinks =
                        mediaFileToTagLinkRepository
                                .findAll(Example.of(new MediaFileToTagLink(source.getId(), null)));
                List<Tag> tags = new ArrayList<Tag>(tagLinks.size());
                for (MediaFileToTagLink tagLink : tagLinks) {
                    TagEntity tagEntity = tagRepository.findOne(tagLink.getId().getTagId());
                    tags.add(new Tag(tagEntity.getTitle(), tagEntity.getColor()));
                }


                return new MediaResource(
                        Long.toString(source.getId()),
                        source.getType().name(),
                        source.getCreationDate(),
                        new File(source.getFileName()).getName(),
                        source.getHeight(),
                        source.getWidth(),
                        tags);
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

    @RequestMapping(value = "media/{mediaId}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity deleteMedia(@PathVariable Long mediaId) {
        MediaFileEntity mediaFileEntity = mediFileRepository.findOne(mediaId);
        if (mediaFileEntity == null) {
            return ResponseEntity.notFound().build();
        }
        File file = new File(mediaFileEntity.getFileName());
        if ((!file.exists()) || file.delete()) {
            //TODO: cleanup metadata in case of video
            mediFileRepository.delete(mediaFileEntity.getId());
            return ResponseEntity.ok().build();
        } else {
            throw new IllegalStateException("Could not remove file:" + file.getAbsolutePath());
        }
    }

    @RequestMapping(value = "media/{mediaId}", method = RequestMethod.GET)
    public ResponseEntity getMedia(
            @PathVariable Long mediaId) {
        MediaFileEntity mediaFileEntity = mediFileRepository.findOne(mediaId);
        if (mediaFileEntity == null) {
            return ResponseEntity.notFound().build();
        }
        File file = new File(mediaFileEntity.getFileName());
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "inline; filename=\"" + file.getName() + "\"")
                    .contentLength(file.length())
                    .contentType(mediaFileEntity.getType() == MediaMetadata.Type.VIDEO ?
                            MediaType.parseMediaType("video/mp4") : MediaType.IMAGE_JPEG)
                    .body(new InputStreamResource(fileInputStream));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    @RequestMapping(value = "thumbnail/{mediaId}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity getThumbnail(
            @PathVariable Long mediaId,
            @RequestParam(value = "width", required = false, defaultValue = "100") Integer width,
            @RequestParam(value = "height", required = false, defaultValue = "100") Integer height) {

        MediaFileEntity mediaFileEntity = mediFileRepository.findOne(mediaId);
        if (mediaFileEntity == null) {
            return ResponseEntity.notFound().build();
        }
        File file = new File(mediaFileEntity.getFileName());
        if (mediaFileEntity.getType() == MediaMetadata.Type.VIDEO) {
            file = linuxVideoFileMetadataExtractor.getVideoThumbnail(file);
        }

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        try {
            BufferedImage originalImage = ImageIO.read(file);
            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();
            Dimension outputSize = null;
            float widthDiff = (float) width / (float) originalWidth;
            float heightDiff = (float) height / (float) originalHeight;

            if (widthDiff > heightDiff) {
                outputSize = new Dimension(
                        Math.round(originalWidth * widthDiff),
                        Math.round(originalHeight * widthDiff)
                );
            } else {
                outputSize = new Dimension(
                        Math.round(originalWidth * heightDiff),
                        Math.round(originalHeight * heightDiff)
                );
            }

            BufferedImage resizedImage = new BufferedImage(
                    width,
                    height,
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g = resizedImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int xOffset = (int) ((outputSize.getWidth() - width) / 2);
            int yOffset = (int) ((outputSize.getHeight() - height) / 2);

            g.drawImage(originalImage, -xOffset, -yOffset,
                    outputSize.width,
                    outputSize.height, null);
            if (mediaFileEntity.getType() == MediaMetadata.Type.VIDEO) {
                g.setColor(new Color(0, 0, 0, 151));
                g.fillRect(0, 0, width, height);
                g.drawImage(playImage,
                        width / 2 - playImage.getWidth() / 2,
                        height / 2 - playImage.getHeight() / 2, null);
            }
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

    @RequestMapping(value = "tags/update", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity updateTags(@RequestBody TagsUpdate tagsUpdate) {

        List<TagEntity> tagEntities = new ArrayList<>(tagsUpdate.getAssignTags().size());
        for (Tag tag : tagsUpdate.getAssignTags()) {
            tagEntities.add(tagRepository.save(new TagEntity(tag.getName(), tag.getColor())));
        }

        for (Long mediaId : tagsUpdate.getMediaIds()) {
            for (TagEntity tagEntity : tagEntities) {
                mediaFileToTagLinkRepository.save(new MediaFileToTagLink(mediaId, tagEntity.getId()));
            }
        }

        if (tagsUpdate.getRemoveTags() != null && !tagsUpdate.getRemoveTags().isEmpty()){
            //TODO: implement removing
        }

        return ResponseEntity.accepted().build();
    }
}