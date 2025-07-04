package com.swp2.demo.service;

import com.swp2.demo.entity.*;
import com.swp2.demo.repository.ChatMessageRepository;
import com.swp2.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class UserMessageService {
    private static final Logger logger = LoggerFactory.getLogger(UserMessageService.class);

    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository; // Although not used in this service's methods, kept for completeness from original code

    /**
     * Finds a user by their username. If the user is not found, it logs a warning
     * and returns null instead of throwing an exception.
     * This allows calling methods to handle the 'user not found' scenario gracefully.
     *
     * @param username The username (or email for OAuth2 users) to search for.
     * @return The User object if found, otherwise null.
     */
    public User findUserByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            logger.warn("User not found with username: '{}'", username);
        }
        return user; // Return null if not found
    }

    /**
     * Sets the status of a user (e.g., online, offline).
     *
     * @param username The username of the user.
     * @param status The new status to set.
     */
    public void setStatus(String username, Status status) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            user.setStatus(status);
            userRepository.save(user);
            logger.info("User '{}' status updated to '{}'", username, status);
        } else {
            logger.warn("Attempted to set status for non-existent user: '{}'", username);
        }
    }

    /**
     * Finds connected users based on the current authenticated user's role.
     * Members will see Coaches, and Coaches will see Members.
     *
     * @return A list of connected User objects.
     * @throws RuntimeException if the current authenticated user is not found in the database.
     */
    public List<User> findConnectedUsers() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String currentPrincipalName;
        if (auth.getPrincipal() instanceof OAuth2User oAuth2User) {
            currentPrincipalName = oAuth2User.getAttribute("email");
            logger.info("OAuth2User detected for connected users, email: {}", currentPrincipalName);
        } else {
            currentPrincipalName = auth.getName();
            logger.info("Form login user detected for connected users, username: {}", currentPrincipalName);
        }

        User currentUser = findUserByUsername(currentPrincipalName);
        if (currentUser == null) {
            logger.error("Current user NOT found for principal name: '{}' when finding connected users.", currentPrincipalName);
            throw new RuntimeException("Current user not found for principal name: " + currentPrincipalName);
        }

        logger.info("User '{}' (role: {}) is requesting connected users", currentUser.getUsername(), currentUser.getRole());

        if (currentUser.getRole() == Role.Member) {
            return userRepository.findAllByRole(Role.Coach);
        } else if (currentUser.getRole() == Role.Coach) {
            return userRepository.findAllByRole(Role.Member);
        }

        logger.info("No connected users found for role: {}", currentUser.getRole());
        return List.of(); // Return an empty list if no matching roles
    }
}
