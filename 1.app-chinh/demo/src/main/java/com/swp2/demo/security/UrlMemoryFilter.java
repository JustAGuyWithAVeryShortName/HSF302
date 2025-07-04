package com.swp2.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class UrlMemoryFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        HttpSession session = request.getSession(false);

        boolean isLoggedIn = request.getUserPrincipal() != null;

        if (!isLoggedIn &&
                "GET".equalsIgnoreCase(request.getMethod()) &&
                !uri.startsWith("/login") &&
                !uri.startsWith("/css") &&
                !uri.startsWith("/js") &&
                !uri.startsWith("/images") &&
                !uri.contains("/spring-security") &&
                !uri.startsWith("/register") &&
                !uri.startsWith("/forgot-password")) {

            if (session != null) {
                session.setAttribute("url_prior_login", uri);
            }
        }

        filterChain.doFilter(request, response);
    }
}
