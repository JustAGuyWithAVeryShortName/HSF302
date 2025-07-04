package com.swp2.demo.repository;

import com.swp2.demo.entity.UserPlanStep;
import com.swp2.demo.entity.QuitPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserPlanStepRepository extends JpaRepository<UserPlanStep, Long> {
    List<UserPlanStep> findByQuitPlan(QuitPlan quitPlan);
}
