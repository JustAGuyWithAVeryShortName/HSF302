package com.swp2.demo.repository;

import com.swp2.demo.entity.Feedback;
import com.swp2.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    List<Feedback> findAllByCoach(User coach);

}
