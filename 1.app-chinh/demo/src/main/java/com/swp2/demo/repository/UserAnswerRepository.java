package com.swp2.demo.repository;

import com.swp2.demo.entity.User;
import com.swp2.demo.entity.UserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {
    void deleteByUser(User user);
}
