package com.swp2.demo.Controller;

import com.swp2.demo.entity.User;
import com.swp2.demo.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {

        User user = userService.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            session.setAttribute("loggedInUser", user); // Gán user vào session
            return "redirect:/dashboard";
        } else {
            redirectAttributes.addFlashAttribute("error", "Invalid username or password.");
            return "redirect:/login";
        }
    }
    // ✅ Logout duy nhất cho cả thường + OAuth2
    @RequestMapping(value = "/logout", method = {RequestMethod.GET, RequestMethod.POST})
    public String customLogout(HttpServletRequest request, HttpSession session, Authentication authentication) throws ServletException {
        if (authentication != null) {
            boolean isOAuth2User = authentication.getPrincipal() instanceof org.springframework.security.oauth2.core.user.OAuth2User;
            if (isOAuth2User) {
                request.logout(); // ✅ OAuth2 logout
            } else {
                session.invalidate(); // ✅ Normal login logout
            }
        }
        return "redirect:/home";
    }
}
