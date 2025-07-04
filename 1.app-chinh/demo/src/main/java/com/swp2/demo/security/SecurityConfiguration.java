package com.swp2.demo.security;

import com.swp2.demo.entity.Member;
import com.swp2.demo.repository.UserRepository;
import com.swp2.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder; // Consider changing this!
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.security.authorization.AuthorizationDecision;

import com.swp2.demo.entity.User;

@Configuration
public class SecurityConfiguration {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserService userService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userService);
        provider.setPasswordEncoder(NoOpPasswordEncoder.getInstance());
        return provider;
    }

    @Autowired
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    @Autowired
    private UserRepository userRepository;

    // We still define this, but its primary role will be to re-throw or let the exception propagate
    // so the @ControllerAdvice can catch it.


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .addFilterBefore(new UrlMemoryFilter(), UsernamePasswordAuthenticationFilter.class)

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/home", "/register/**", "/login", "/css/**", "/images/**", "/js/**",
                                "/member", "/forgot-password", "/reset-password",
                                "/ws/**","/about_us","/.well-known/**"

                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/questionnaire").permitAll()
                        .requestMatchers(HttpMethod.GET, "/questionnaire").authenticated()
                        .requestMatchers("/admin/**").hasAuthority("Admin")
                        .requestMatchers("/profile").authenticated()

                        .anyRequest().authenticated()
                )


                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/authenticateTheUser")
                        .successHandler(customAuthenticationSuccessHandler)
                        .permitAll()
                )

                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .successHandler(customAuthenticationSuccessHandler)
                )



                .logout(logout -> logout
                        .logoutUrl("/spring-security-logout")
                        .logoutSuccessUrl("/home")
                );
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> {
            com.swp2.demo.entity.User user = userRepository.findByUsername(username);

            if (user == null) {
                throw new UsernameNotFoundException("User / email  not found: " + username);
            }

            return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
        };
    }
}