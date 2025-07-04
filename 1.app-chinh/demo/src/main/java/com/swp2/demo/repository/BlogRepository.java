package com.swp2.demo.repository;

import com.swp2.demo.entity.Blog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogRepository extends JpaRepository<Blog, Long> {
    long count();
}
