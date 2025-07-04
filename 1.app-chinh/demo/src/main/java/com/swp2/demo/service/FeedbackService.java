package com.swp2.demo.service;

import java.util.List;
import java.util.Optional;

import com.swp2.demo.entity.Feedback; // Your package
import com.swp2.demo.entity.User;     // Your package
import com.swp2.demo.repository.FeedbackRepository; // Your package
import com.swp2.demo.repository.UserRepository;     // Your package
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;

    public FeedbackService(FeedbackRepository feedbackRepository, UserRepository userRepository) {
        this.feedbackRepository = feedbackRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Feedback saveFeedback(Integer rating, String comment) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("User is not authenticated to submit feedback.");
        }

        String username;
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else if (principal instanceof User) {
            username = ((User) principal).getUsername();
        } else if (principal instanceof String) {
            username = (String) principal;
        } else {
            throw new IllegalStateException("Could not determine username from authentication principal type: " + principal.getClass().getName());
        }

        User currentUser = userRepository.findByUsername(username);

        if (currentUser == null) {
            throw new RuntimeException("Authenticated user with username '" + username + "' not found in the database. " +
                "Please check your UserRepository.findByUsername implementation.");
        }

        // Create the Feedback entity using the custom constructor
        // Now, the Feedback entity only expects rating, comment, and the submitting User
        Feedback feedback = new Feedback(rating, comment, currentUser);

        return feedbackRepository.save(feedback);
    }
    public long getTotalFeedbackCount() {
        return feedbackRepository.count();
    }
    @Transactional // Deletion operations should typically be transactional
    public void deleteFeedbackById(Long id) {
        // It's good practice to check if the entity exists before attempting to delete
        // to avoid DataIntegrityViolationException if foreign keys are involved
        // or just to provide a more specific error message.
        if (feedbackRepository.existsById(id)) {
            feedbackRepository.deleteById(id);
        } else {
            // Optionally throw an exception if the feedback doesn't exist
            throw new IllegalArgumentException("Feedback with ID " + id + " not found.");
        }
    }
    public List<Feedback> findAllFeedback() {
        return feedbackRepository.findAll();
    }

    public Optional<Feedback> findById(Long id) {
        return feedbackRepository.findById(id);
    }
}
