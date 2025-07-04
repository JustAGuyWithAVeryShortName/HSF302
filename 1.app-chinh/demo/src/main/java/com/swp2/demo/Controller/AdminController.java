package com.swp2.demo.Controller;

import com.swp2.demo.repository.OrderRepository;
import com.swp2.demo.repository.UserRepository;
import com.swp2.demo.repository.FeedbackRepository;
import com.swp2.demo.repository.NotificationRepository; // <-- IMPORTANT: Add this import!
import com.swp2.demo.entity.Member;
import com.swp2.demo.entity.Role;
import com.swp2.demo.entity.User;
import com.swp2.demo.entity.Feedback;
import com.swp2.demo.entity.dto.RegisterDTO;
import com.swp2.demo.service.FeedbackService;
import com.swp2.demo.service.UserService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.transaction.annotation.Transactional; // <-- IMPORTANT: Add this import!

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
public class AdminController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired // <-- IMPORTANT: Autowire NotificationRepository
    private NotificationRepository notificationRepository;

    private final FeedbackService feedbackService;

    @Autowired
    private UserService userService;

    public AdminController(FeedbackService feedbackService, UserService userService) {
        this.feedbackService = feedbackService;
        this.userService = userService;
    }

    @GetMapping("/admin/accounts")
    public String viewAllAccounts(Model model) {
        List<User> allUsers = userRepository.findAll();

        List<User> adminAndCoachUsers = allUsers.stream()
            .filter(user -> user.getRole() == Role.Admin || user.getRole() == Role.Coach)
            .toList();
        List<User> normalUsers = allUsers.stream()
            .filter(user -> user.getRole() != Role.Admin && user.getRole() != Role.Coach)
            .toList();

        model.addAttribute("adminCoachUsers", adminAndCoachUsers);
        model.addAttribute("normalUsers", normalUsers);

        return "accounts";
    }

    @GetMapping("/admin")
    public String admin(Model model) {
        long totalMembers = userRepository.count();
        long todayMembers = userRepository.countTodayMembers();
        long pendingOrders = orderRepository.countByStatus("PENDING");
        long freeCount = userRepository.countByMember(Member.FREE);
        long vipCount = userRepository.countByMember(Member.VIP);
        long premiumCount = userRepository.countByMember(Member.PREMIUM);
        long totalFeedbacks = feedbackService.getTotalFeedbackCount();


        model.addAttribute("totalMembers", totalMembers);
        model.addAttribute("todayMembers", todayMembers);
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("totalFeedbacks", totalFeedbacks);

        model.addAttribute("labels", List.of("Free", "VIP", "Premium"));
        model.addAttribute("values", List.of(freeCount, vipCount, premiumCount));

        int currentYear = LocalDate.now().getYear();
        List<Object[]> results = userRepository.countUsersByMonth(currentYear);

        int[] monthlyUserData = new int[12];
        for (Object[] row : results) {
            int month = (Integer) row[0];
            Long count = (Long) row[1];
            monthlyUserData[month - 1] = count.intValue();  // month từ 1-12, array index từ 0
        }

        model.addAttribute("monthlyUserData", monthlyUserData);
        return "admin";
    }

    @PostMapping("/admin/accounts/delete/{id}")
    @Transactional // Ensures all database operations within this method are part of a single transaction
    public String deleteAccount(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<User> userOptional = userRepository.findById(id);

        if (userOptional.isPresent()) {
            User userToDelete = userOptional.get();

            // Check if the user to be deleted has the ADMIN role
            if (userToDelete.getRole() == Role.Admin) {
                redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete an account with Admin role.");
            } else {
                try {
                    // --- IMPORTANT: Handle ALL related entities FIRST ---

                    // 1. Handle Notifications
                    // Delete all notifications associated with this user
                    // You need a method in NotificationRepository, e.g., 'deleteByUserId(Long userId)'
                    notificationRepository.deleteByUserId(id); // Using the user's ID

                    // 2. Handle Feedback where this user is a coach
                    // Set coach to null instead of deleting feedback if you want to keep feedback records
                    List<Feedback> feedbackAsCoach = feedbackRepository.findAllByCoach(userToDelete);
                    for (Feedback feedback : feedbackAsCoach) {
                        feedback.setCoach(null);
                        feedbackRepository.save(feedback);
                    }
                    // If a user is giving feedback to someone else, and that feedback is tied to them
                    // (e.g., feedback.setGivenBy(userToDelete)), you'd need to handle that as well.
                    // For now, assuming Feedback is only linked to a coach.

                    // 3. Handle other related entities (e.g., Orders, ChatMessages, Payments)
                    // You need to explicitly delete or nullify foreign keys for ALL tables that reference `users`.
                    // Example for Orders (if User has orders and you want to delete them):
                    // orderRepository.deleteByUserId(id); // You'd need this method in OrderRepository
                    // Example for Payments:
                    // paymentRepository.deleteByUserId(id); // You'd need this method in PaymentRepository
                    // Example for ChatMessages:
                    // chatMessageRepository.deleteBySenderId(id); // If messages linked by sender

                    // After handling all dependent entities, delete the user
                    userRepository.deleteById(id);
                    redirectAttributes.addFlashAttribute("successMessage", "Account deleted successfully!");

                } catch (DataIntegrityViolationException e) {
                    redirectAttributes.addFlashAttribute("errorMessage",
                        "This account cannot be deleted because it is still associated with existing data that prevents direct deletion. Please ensure all related data is handled or configure cascade delete at the database level.");
                    System.err.println("DataIntegrityViolationException when deleting user " + id + ": " + e.getMessage());
                    e.printStackTrace(); // Always good for debugging
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Error deleting account: " + e.getMessage());
                    System.err.println("Unexpected error when deleting user " + id + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Account not found with ID: " + id);
        }
        return "redirect:/admin/accounts";
    }

    @GetMapping("/admin/accounts/add")
    public String showAddAccountForm(Model model) {
        model.addAttribute("registerDTO", new RegisterDTO());
        return "register/formAdmin";
    }

    @PostMapping("/admin/accounts/save")
    public String saveAccount(@Valid RegisterDTO registerDTO, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("registerDTO", registerDTO);
            model.addAttribute("my_error", "Please correct the form errors.");
            return "register/formAdmin";
        }

        if (userRepository.findByUsername(registerDTO.getUsername()) != null) {
            model.addAttribute("my_error", "Username already exists.");
            model.addAttribute("registerDTO", registerDTO);
            return "register/formAdmin";
        }
        if (userRepository.findByEmail(registerDTO.getEmail()) != null) {
            model.addAttribute("my_error", "Email already exists.");
            model.addAttribute("registerDTO", registerDTO);
            return "register/formAdmin";
        }

        try {
            User newUser = new User();
            newUser.setUsername(registerDTO.getUsername());
            // TODO: In a real application, you should hash the password before saving!
            // newUser.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
            newUser.setPassword(registerDTO.getPassword()); // For now, saving as plain text
            newUser.setEmail(registerDTO.getEmail());
            newUser.setFirstName(registerDTO.getFirstName());
            newUser.setLastName(registerDTO.getLastName());
            newUser.setGender(registerDTO.getGender());
            newUser.setDateOfBirth(registerDTO.getDateOfBirth());
            newUser.setRole(registerDTO.getRole());
            newUser.setCreatedAt(LocalDate.now());
            newUser.setMember(Member.FREE);
            newUser.setStatus(com.swp2.demo.entity.Status.OFFLINE);

            userRepository.save(newUser);
            redirectAttributes.addFlashAttribute("successMessage", "Account added successfully!");
            return "redirect:/admin/accounts";
        } catch (Exception e) {
            System.err.println("Error saving new account: " + e.getMessage());
            model.addAttribute("my_error", "An error occurred while adding the account. Please try again.");
            model.addAttribute("registerDTO", registerDTO);
            return "register/formAdmin";
        }
    }

    @GetMapping("/admin/accounts/{id}")
    public String getUserDetail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        User user = userService.findById(id);

        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Account not found.");
            // Typo: "redirect:admin/ratings" should likely be "redirect:/admin/accounts" or a dedicated error page.
            // Or if it's meant to go to ratings, ensure that path exists.
            return "redirect:/admin/accounts";
        }

        model.addAttribute("user", user);
        return "profile-for-admin-feedback";
    }
}