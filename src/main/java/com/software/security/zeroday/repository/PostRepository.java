package com.software.security.zeroday.repository;

import com.software.security.zeroday.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
