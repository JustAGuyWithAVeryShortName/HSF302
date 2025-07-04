package com.swp2.demo.Controller;

import com.swp2.demo.entity.dto.FeedbackRequest; // Ensure this package matches your project structure
import com.swp2.demo.entity.Feedback;     // Ensure this package matches your project structure
import com.swp2.demo.service.FeedbackService; // Ensure this package matches your project structure
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()") // Only authenticated users can submit feedback
    public ResponseEntity<?> submitFeedback(@RequestBody FeedbackRequest feedbackRequest) {
        // Basic validation: ensure either rating or comment is provided
        if (feedbackRequest.getRating() == null && (feedbackRequest.getComment() == null || feedbackRequest.getComment().trim().isEmpty())) {
            return ResponseEntity.badRequest().body("Rating or comment is required.");
        }

        try {
            // Delegate the business logic to the service layer
            Feedback savedFeedback = feedbackService.saveFeedback(feedbackRequest.getRating(), feedbackRequest.getComment());

            // Return a success response. You might want to return a simpler DTO or just a success message
            // instead of the full Feedback entity, depending on your API design.
            return ResponseEntity.status(HttpStatus.CREATED).body(savedFeedback);
        } catch (IllegalStateException e) {
            // Catches errors related to authentication state (e.g., if @PreAuthorize somehow fails or is bypassed)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (RuntimeException e) {
            // Catches runtime exceptions, such as the user not being found in the database
            // Log the stack trace for debugging purposes in a real application
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            // Catch any other unexpected exceptions
            e.printStackTrace(); // Log the full stack trace for debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to submit feedback: " + e.getMessage());
        }
    }
}