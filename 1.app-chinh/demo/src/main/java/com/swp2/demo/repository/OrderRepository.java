package com.swp2.demo.repository;

import com.swp2.demo.entity.Member;
import com.swp2.demo.entity.Order;
import com.swp2.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    long countByStatus(String status);
    Optional<Order> findFirstByUserAndMemberPlanAndStatus(User user, Member memberPlan, String status);

    // 7/7
    Optional<Order> findTopByUserOrderByCreatedAtDesc(User user);
}
