package com.software.security.zeroday.repository;

import com.software.security.zeroday.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;
import java.util.stream.Stream;

public interface FileRepository extends JpaRepository<File, Long> {
    boolean existsByName(String name);

    @Query("SELECT F FROM File F WHERE F.user.id = :id")
    Optional<File> findByUser(Long id);

    @Query(value = """
    WITH RECURSIVE user_posts AS (
        SELECT id FROM post WHERE user_id = :id
        UNION ALL
        SELECT p.id FROM post p
        INNER JOIN user_posts up ON p.parent_id = up.id
    )
    SELECT DISTINCT f.* FROM file f
    LEFT JOIN user_posts up ON f.post_id = up.id
    WHERE f.user_id = :id OR up.id IS NOT NULL;
    """, nativeQuery = true)
    Stream<File> findUserAndPostsDescendantsFiles(Long id);

    @Query(value = """
    WITH RECURSIVE child_posts AS (
        SELECT id FROM post WHERE id = :id
        UNION ALL
        SELECT p.id FROM post p
        INNER JOIN child_posts cp ON p.parent_id = cp.id
    )
    SELECT f.* FROM file f
    INNER JOIN child_posts cp ON f.post_id = cp.id;
    """, nativeQuery = true)
    Stream<File> findPostAndDescendantsFiles(Long id);

    Stream<File> findAllByProcessedAndUploadedAtBefore(Boolean processed, Instant instant);
}
