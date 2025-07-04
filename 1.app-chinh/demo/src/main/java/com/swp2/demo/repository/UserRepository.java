package com.swp2.demo.repository;


import com.swp2.demo.entity.Member;
import com.swp2.demo.entity.Role;
import com.swp2.demo.entity.Status;
import com.swp2.demo.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    long count();

    User findByUsername(String username);
    User findByEmail(String email);

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt = CURRENT_DATE")
    long countTodayMembers();

    long countByMember(Member member);

    @Query("SELECT MONTH(u.createdAt), COUNT(u) " +
            "FROM User u " +
            "WHERE YEAR(u.createdAt) = :year " +
            "GROUP BY MONTH(u.createdAt) " +
            "ORDER BY MONTH(u.createdAt)")
    List<Object[]> countUsersByMonth(@Param("year") int year);


    List<User> findAllByStatus(Status status);
    List<User> findAllByRoleAndStatus(Role role, Status status);
    List<User> findAllByRole(Role role);

}
