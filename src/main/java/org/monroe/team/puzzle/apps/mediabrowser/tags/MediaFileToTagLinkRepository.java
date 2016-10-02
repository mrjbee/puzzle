package org.monroe.team.puzzle.apps.mediabrowser.tags;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaFileToTagLinkRepository extends JpaRepository<MediaFileToTagLink, MediaFileToTagLink.Key> {
}
