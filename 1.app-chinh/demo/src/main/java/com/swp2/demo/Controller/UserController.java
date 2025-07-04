package com.swp2.demo.Controller;

import com.swp2.demo.entity.User;
import com.swp2.demo.service.QuitPlanService;
import com.swp2.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class UserController {

    @Autowired
    private QuitPlanService quitPlanService;

    @Autowired
    private UserService userService;


    @GetMapping("/dashboard")
    public String userDashboard(Model model, @AuthenticationPrincipal Object principal) {
        User user = extractUser(principal);
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("username", user.getUsername());
        model.addAttribute("memberType", user.getMember());

        boolean hasPlan = quitPlanService.hasPlan(user.getId());
        model.addAttribute("hasPlan", hasPlan);

        return "userpage";
    }

    // ================= Helper ====================
    private User extractUser(Object principal) {
        if (principal instanceof UserDetails userDetails) {
            return userService.findByUsername(userDetails.getUsername());
        }
        if (principal instanceof OAuth2User oauth2User) {
            String email = oauth2User.getAttribute("email");
            return userService.findByEmail(email);  // Phải có phương thức này trong UserService
        }
        return null;
    }
}