package com.swp2.demo.service;

import com.swp2.demo.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;


public interface UserService extends UserDetailsService {
    public User findByUsername(String username);

    User findByEmail(String email);

    User findById(Long id);
    public void save(User user);

}
