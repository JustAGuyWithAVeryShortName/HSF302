package com.swp2.demo.repository;


import com.swp2.demo.entity.MessageHome;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
public interface MessageHomeRepository extends JpaRepository<MessageHome, Long> {
    List<MessageHome> findAllByOrderBySentAtAsc();
}
