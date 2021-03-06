package org.monroe.team.puzzle.apps.mediabrowser.api;

import org.monroe.team.puzzle.apps.mediabrowser.Log;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

@RestController
@RequestMapping("/api")
public class BrowserApi {

    @Autowired
    Log log;

    @Autowired
    MediaFileRepository mediFileRepository;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    MediaFileToTagLinkRepository mediaFileToTagLinkRepository;

    @Autowired
    LinuxVideoFileMetadataExtractor linuxVideoFileMetadataExtractor;

    @Autowired
    ImageLoader imageLoader;

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
            @RequestParam(value = "limit", defaultValue = "100") Integer limit,
            @RequestParam(value = "tags", defaultValue = ",") String tagsFilter) {

        final Pageable pageRequest = new ChunkPageable(
                offset, limit, Sort.Direction.DESC, "creationDate"
        );

        String[] tags = tagsFilter.split(",");
        if (tags.length == 0) {
            Page<MediaFileEntity> mediaFileEntityList = mediFileRepository.findAll(pageRequest);
            List<MediaResource> answerList = mediaFileEntityList.map(new Converter<MediaFileEntity, MediaResource>() {
                @Override
                public MediaResource convert(final MediaFileEntity source) {
                    return asMediaResource(source);
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
        } else {
            List<MediaFileEntity> fileEntities = mediFileRepository.findFilteredByTag(tags, limit, offset);
            List<MediaResource> mediaResources = new LinkedList<>();
            for (MediaFileEntity fileEntity : fileEntities) {
                mediaResources.add(asMediaResource(fileEntity));
            }

            return new MediaStream(
                    new Paging(
                            offset,
                            limit,
                            mediaResources.size()
                    ),
                    mediaResources
            );
        }
    }

    private MediaResource asMediaResource(final MediaFileEntity source) {
        List<MediaFileToTagLink> tagLinks =
                mediaFileToTagLinkRepository
                        .findAll(Example.of(new MediaFileToTagLink(source.getId(), null)));
        List<Tag> tags = new ArrayList<Tag>(tagLinks.size());
        for (MediaFileToTagLink tagLink : tagLinks) {
            TagEntity tagEntity = tagRepository.findOne(tagLink.getId().getTagId());
            tags.add(new Tag(tagEntity.getTitle(), tagEntity.getType()));
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
            List<MediaFileToTagLink> tagLinks =
                    mediaFileToTagLinkRepository
                            .findAll(Example.of(new MediaFileToTagLink(mediaFileEntity.getId(), null)));
            mediaFileToTagLinkRepository.delete(tagLinks);
            return ResponseEntity.ok().build();
        } else {
            throw new IllegalStateException("Could not remove file:" + file.getAbsolutePath());
        }
    }

    @RequestMapping(value = "media/{mediaId}", method = RequestMethod.GET)
    public ResponseEntity getMedia(
            @RequestParam(value = "disposition", defaultValue = "inline") String disposition,
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
            return enableCache(ResponseEntity.ok())
                    .header("Content-Disposition", disposition+"; filename=\"" + file.getName() + "\"")
                    .contentLength(file.length())
                    .contentType(isAVideo(mediaFileEntity) ?
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
        boolean isVideoResource = isAVideo(mediaFileEntity);
        if (isVideoResource) {
            file = linuxVideoFileMetadataExtractor.getVideoThumbnail(file);
        }

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        try {
            BufferedImage resizedImage = prepareThumbnailImage(width, height, file, isVideoResource);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, "jpg", os);
            return enableCache(ResponseEntity.ok())
                    .contentLength(os.size())
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(new InputStreamResource(new ByteArrayInputStream(os.toByteArray())));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private BufferedImage prepareThumbnailImage(
            final Integer width,
            final Integer height,
            final File file,
            final boolean isVideoResource) {
        BufferedImage originalImage = imageLoader.readImage(file);
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
        if (isVideoResource) {
            g.setColor(new Color(0, 0, 0, 151));
            g.fillRect(0, 0, width, height);
            g.drawImage(playImage,
                    width / 2 - playImage.getWidth() / 2,
                    height / 2 - playImage.getHeight() / 2, null);
        }
        g.dispose();
        return resizedImage;
    }

    private boolean isAVideo(final MediaFileEntity mediaFileEntity) {
        return mediaFileEntity.getType() == MediaMetadata.Type.VIDEO;
    }

    @RequestMapping(value = "tags/update", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity updateTags(@RequestBody TagsUpdate tagsUpdate) {

        List<TagEntity> tagEntities = new ArrayList<>(tagsUpdate.getAssignTags().size());
        for (Tag tag : tagsUpdate.getAssignTags()) {
            tagEntities.add(tagRepository.save(new TagEntity(tag.getName(), tag.getType())));
        }

        if (tagsUpdate.getRemoveTags() != null && !tagsUpdate.getRemoveTags().isEmpty()){
            for (Tag tag : tagsUpdate.getRemoveTags()) {
                for (Long mediaId : tagsUpdate.getMediaIds()) {
                    mediaFileToTagLinkRepository.delete(
                            new MediaFileToTagLink(
                                mediaId,
                                new TagEntity(tag.getName(), tag.getType()).getId()
                            )
                    );
                }
            }
        }

        for (Long mediaId : tagsUpdate.getMediaIds()) {
            for (TagEntity tagEntity : tagEntities) {
                mediaFileToTagLinkRepository.save(new MediaFileToTagLink(mediaId, tagEntity.getId()));
            }
        }


        return ResponseEntity.accepted().build();
    }

    @RequestMapping(value = "tags", method = RequestMethod.GET)
    @ResponseBody
    @Transactional
    public List<Tag> tags() {
        int removeCount = tagRepository.removeOrphan();
        List<Tag> tags = new LinkedList<>();
        for (TagEntity tagEntity : tagRepository.findAll(new Sort("title"))) {
            tags.add(new Tag(tagEntity.getTitle(),tagEntity.getType()));
        }
        return tags;
    }


    private static ResponseEntity.BodyBuilder enableCache(ResponseEntity.BodyBuilder builder){
        Date expdate = new Date ();
        expdate.setTime (expdate.getTime() + 86400);
        String expiresValue = expdate.toGMTString();

        return builder
                //TODO read about public here
                .header("Pragma","public")
                .header("Cache-Control","max-age=86400")
                .header("Expires", expiresValue);
    }
}