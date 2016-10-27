package org.monroe.team.puzzle.apps.mediabrowser.indexer;

import org.monroe.team.puzzle.apps.mediabrowser.tags.MediaFileToTagLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MediaFileRepository extends JpaRepository<MediaFileEntity,Long> {

    @Query(value = "SELECT * FROM media_file_entity WHERE id IN " +
            "(SELECT DISTINCT media_id FROM media_file_to_tag_link LEFT JOIN tag_entity WHERE media_file_to_tag_link.tag_id = tag_entity.id AND tag_entity.title IN (:tags))" +
            "ORDER BY creation_date DESC LIMIT :limit OFFSET :offset",
            nativeQuery = true)
    List<MediaFileEntity> findFilteredByTag(@Param("tags") String[] tagNames,
                                            @Param("limit") long limit,
                                            @Param("offset") long offset);

}
