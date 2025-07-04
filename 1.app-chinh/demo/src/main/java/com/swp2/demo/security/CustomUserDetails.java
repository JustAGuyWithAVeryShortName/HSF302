package com.swp2.demo.security;

import com.swp2.demo.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public Long getId() {
        return user.getId();
    }


    public CustomUserDetails(User user) {
        this.user = user;
    }

    public User getUser() {
        return this.user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(user.getRole().name())); // Role là enum → name() trả về String
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Bạn có thể thêm logic riêng nếu muốn
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Có thể kiểm tra locked trong entity nếu cần
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Có thể thêm thời hạn mật khẩu nếu muốn
    }

    @Override
    public boolean isEnabled() {
        return true; // Có thể thêm field active/disable nếu muốn quản lý
    }
}
