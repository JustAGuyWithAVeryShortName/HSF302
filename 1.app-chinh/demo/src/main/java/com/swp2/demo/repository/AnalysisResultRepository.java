package com.swp2.demo.repository;


import com.swp2.demo.entity.AnalysisResultEntity;
import com.swp2.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnalysisResultRepository extends JpaRepository<AnalysisResultEntity, Long> {
    List<AnalysisResultEntity> findByUser(User user);
    void deleteByUser(User user);
    Optional<AnalysisResultEntity> findTopByUser_IdOrderByCreatedAtDesc(Long userId);

}