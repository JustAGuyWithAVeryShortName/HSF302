package com.swp2.demo.repository;

import jakarta.transaction.Transactional;

import com.swp2.demo.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByUserIdAndContentAndCreatedAtBetween(Long userId, String content,
                                                        LocalDateTime start, LocalDateTime end);

    @Modifying // Indicates that this query modifies the database
    @Transactional // Ensures this operation runs in a transaction
    // FIX: Change 'n.user.id' to 'n.userId' if 'userId' is a direct field in Notification entity
    @Query("DELETE FROM Notification n WHERE n.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
