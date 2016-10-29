package org.monroe.team.puzzle.apps.mediabrowser.tags;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface TagRepository extends JpaRepository<TagEntity, Long> {

    @Modifying
    @Query(value = " DELETE FROM tag_entity WHERE id NOT IN " +
            "(SELECT tag_id FROM media_file_to_tag_link)",
            nativeQuery = true)
    int removeOrphan();

}
