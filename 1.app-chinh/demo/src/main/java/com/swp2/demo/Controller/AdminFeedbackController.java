package com.swp2.demo.Controller;

import java.util.List;
import java.util.Optional; // Import Optional

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize; // Import for security
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping; // Import PostMapping
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.swp2.demo.entity.Feedback;
import com.swp2.demo.service.FeedbackService;

@Controller
@RequestMapping("/admin/ratings") // Base path for admin ratings management
@PreAuthorize("hasRole('ADMIN')") // All methods in this controller require ADMIN role
public class AdminFeedbackController {

    private final FeedbackService feedbackService; // Use final and constructor injection

    @Autowired // Inject FeedbackService via constructor
    public AdminFeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    /**
     * Displays a list of all feedback records for administration.
     * Accessible at /admin/ratings
     */
    @GetMapping
    public String getAllFeedback(Model model) {
        List<Feedback> feedbacks = feedbackService.findAllFeedback();
        model.addAttribute("feedbacks", feedbacks);
        return "rating-feedback"; // Refers to src/main/resources/templates/admin/ratings_feedback.html
    }

    /**
     * Displays detailed information for a single feedback record.
     * Accessible at /admin/ratings/{id}/detail
     */
    @GetMapping("/{id}/detail")
    public String getFeedbackDetail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        // Correctly handle Optional from findById
        Optional<Feedback> feedbackOptional = feedbackService.findById(id);

        if (feedbackOptional.isEmpty()) { // Use isEmpty() for Optional check
            redirectAttributes.addFlashAttribute("errorMessage", "Feedback not found.");
            return "redirect:/admin/ratings"; // Correct redirect path
        }

        model.addAttribute("feedback", feedbackOptional.get()); // Get the Feedback object
        return "admin/feedback-detail"; // You need to create this HTML template
    }


    /**
     * Handles the deletion of a feedback record.
     * Accessible via POST to /admin/ratings/delete/{id}
     */
    @PostMapping("/delete/{id}") // Added PostMapping annotation
    public String deleteFeedback(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            feedbackService.deleteFeedbackById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Feedback with ID " + id + " deleted successfully!");
        } catch (IllegalArgumentException e) {
            // Catch specific exception for not found from service layer
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            // Catch any other unexpected errors
            e.printStackTrace(); // Log the exception for debugging
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting feedback: " + e.getMessage());
        }
        return "redirect:/admin/ratings"; // Correct redirect path
    }


    // You might also consider adding an endpoint to get the total count for the dashboard
    // @GetMapping("/count")
    // public ResponseEntity<Long> getFeedbackCount() {
    //     long count = feedbackService.getTotalFeedbackCount();
    //     return ResponseEntity.ok(count);
    // }
}