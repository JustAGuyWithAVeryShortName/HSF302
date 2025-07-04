package com.swp2.demo.Controller;

import com.swp2.demo.entity.User;
import com.swp2.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
public class PasswordController {

    @Autowired
    private UserService userService;


    @GetMapping("/change-password")
    public String showChangePasswordForm() {

        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam("oldPassword") String oldPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 Model model,
                                 Principal principal) {

        User user = userService.findByUsername(principal.getName());

        if (!oldPassword.equals(user.getPassword())) {
            model.addAttribute("message", "Mật khẩu cũ không chính xác.");
            return "change-password";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("message", "Mật khẩu mới và xác nhận không khớp.");
            return "change-password";
        }


        user.setPassword(newPassword);
        userService.save(user);
        model.addAttribute("message", "Đổi mật khẩu thành công.");
        return "change-password";
    }
}
