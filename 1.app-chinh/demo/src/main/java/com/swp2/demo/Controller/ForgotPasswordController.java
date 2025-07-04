package com.swp2.demo.Controller;

import com.swp2.demo.repository.PasswordResetTokenRepository;
import com.swp2.demo.repository.UserRepository;
import com.swp2.demo.entity.PasswordResetToken;
import com.swp2.demo.entity.User;
import com.swp2.demo.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.UUID;

@Controller
public class ForgotPasswordController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password"; // tên file Thymeleaf template
    }

    @Transactional
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, Model model, HttpServletRequest request) {
        User user = userRepository.findByEmail(email);

        if (user != null) {
            // ✅ Xóa các token cũ của user này (nếu muốn mỗi lần chỉ có 1 token đang hoạt động)
            tokenRepository.deleteByUserId(user.getId());
            tokenRepository.flush();

            // ✅ Tạo token mới
            String token = UUID.randomUUID().toString();

            PasswordResetToken passwordResetToken = new PasswordResetToken();
            passwordResetToken.setToken(token);
            passwordResetToken.setUser(user);
            passwordResetToken.setExpiryDate(LocalDateTime.now().plusHours(2));
            tokenRepository.save(passwordResetToken);

            // ✅ Tạo URL reset
            String resetUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
                    + "/reset-password?token=" + token;

            String content = "Để đặt lại mật khẩu, vui lòng nhấn vào liên kết sau:\n" + resetUrl;

            emailService.sendEmail(user.getEmail(), "Đặt lại mật khẩu", content);
            model.addAttribute("message", "Password reset email has been sent!");
        } else {
            model.addAttribute("error", "Account with this email was not found.");
        }

        return "forgot-password";
    }
}
