package com.swp2.demo.repository;

import com.swp2.demo.entity.QuitPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuitPlanRepository extends JpaRepository<QuitPlan, Long> {
    @Query("SELECT q FROM QuitPlan q WHERE q.user.id = :userId ORDER BY q.id DESC")
    List<QuitPlan> findLatestByUserId(@Param("userId") Long userId);
    boolean existsByUserId(Long userId);
    void deleteByUserId(Long userId);
    Optional<QuitPlan> findTopByUser_UsernameOrderByIdDesc(String username);

}
