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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
                        source.getId(),
                        source.getType().name(),
                        source.getCreationDate(),
                        new File(source.getFileName()).getName());
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
    public ResponseEntity getThumbnail(@PathVariable Long mediaId) {
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

        try {
            return ResponseEntity.ok()
                    .contentLength(file.length())
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(new InputStreamResource(new FileInputStream(file)));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
