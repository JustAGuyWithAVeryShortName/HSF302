package com.swp2.demo.repository;

import com.swp2.demo.entity.Coach;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoachRepository extends JpaRepository<Coach, Long> {}
