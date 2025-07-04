package com.swp2.demo.Controller;

import com.swp2.demo.entity.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ModelAttribute
    public void addGlobalAttributes(Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user != null) {
            model.addAttribute("fullName", user.getFirstName());
            model.addAttribute("userRole", user.getRole());

            Object loginType = session.getAttribute("loginType");
            boolean isUsernameLogin = "username".equals(loginType);
            model.addAttribute("isUsernameLogin", isUsernameLogin);

        }
    }
}
