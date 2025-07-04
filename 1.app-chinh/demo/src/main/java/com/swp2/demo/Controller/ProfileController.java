package com.swp2.demo.Controller;

import com.swp2.demo.repository.UserRepository;
import com.swp2.demo.entity.Role;
import com.swp2.demo.entity.User;
import com.swp2.demo.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.UUID;

@Controller
public class ProfileController {
    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public String showProfilePage(Model model, @AuthenticationPrincipal Object principal) {
        if (principal instanceof UserDetails userDetails) {
            String username = userDetails.getUsername();
            User user = userService.findByUsername(username);
            model.addAttribute("user", user);
        } else if (principal instanceof OAuth2User oauth2User) {
            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");

            // Kiểm tra xem user này đã tồn tại trong DB chưa
            User user = userService.findByEmail(email);
            if (user == null) {
                user = new User();
                user.setUsername(email);  // dùng email làm username cho ổn định
                user.setEmail(email);
                user.setFirstName(name);
                user.setPassword(UUID.randomUUID().toString()); // hoặc UUID.randomUUID().toString()
                user.setRole(Role.Guest);
                userService.save(user);
            }
            model.addAttribute("user", user);
        }
        return "profile";
    }

    @GetMapping("/profile/edit")
    public String showEditProfileForm(Model model, @AuthenticationPrincipal Object principal) {
        User user = null;

        if (principal instanceof UserDetails userDetails) {
            String username = userDetails.getUsername();
            user = userService.findByUsername(username);
        } else if (principal instanceof OAuth2User oauth2User) {
            String email = oauth2User.getAttribute("email");
            user = userService.findByEmail(email);
        }

        if (user != null) {

            model.addAttribute("user", user);
            return "edit-profile";
        } else {
            return "redirect:/profile";
        }
    }


    @Autowired
    private UserRepository userRepository;
    @GetMapping("/admin/accounts/{id}/profile")
    public String viewUserProfileAsAdmin(@PathVariable Long id, Model model) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("user", user);
        return "profileForAdmin";
    }


    // Xử lý cập nhật thông tin
    @PostMapping("/profile/edit")
    public String updateProfile(@Valid @ModelAttribute("user") User updatedUser,
                                BindingResult result,
                                @AuthenticationPrincipal Object principal,
                                HttpSession session,
                                Model model) {
        if (result.hasErrors()) {
            return "edit-profile";
        }

        User user = null;

        if (principal instanceof UserDetails userDetails) {
            String username = userDetails.getUsername();
            user = userService.findByUsername(username);
        } else if (principal instanceof OAuth2User oauth2User) {
            String email = oauth2User.getAttribute("email");
            user = userService.findByEmail(email);
        }

        if (user != null) {
            user.setFirstName(updatedUser.getFirstName());
            user.setLastName(updatedUser.getLastName());
            user.setGender(updatedUser.getGender());
            user.setDateOfBirth(updatedUser.getDateOfBirth());

            userService.save(user);

            session.setAttribute("loggedInUser", user);
        }

        return "redirect:/profile";
    }
}
