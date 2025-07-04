package com.swp2.demo.Controller;

import com.swp2.demo.repository.PasswordResetTokenRepository;
import com.swp2.demo.repository.UserRepository;
import com.swp2.demo.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Controller
public class ResetPasswordController {

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private UserRepository userRepository;

    //@Autowired
    //private PasswordEncoder passwordEncoder;

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam(value = "token", required = false) String token, Model model) {
        if (token == null || token.isBlank()) {
            model.addAttribute("error", "Thiếu token. Vui lòng kiểm tra lại liên kết đặt lại mật khẩu.");
            return "redirect:/login?tokenMissing";
        }

        var resetToken = passwordResetTokenRepository.findByToken(token);
        if (resetToken.isPresent() && resetToken.get().getExpiryDate().isAfter(LocalDateTime.now())) {
            model.addAttribute("token", token);
            return "reset-password";
        } else {
            return "redirect:/login?tokenExpired";
        }
    }


    @PostMapping("/reset-password")
    @Transactional
    public String processResetPassword(@RequestParam("token") String token,
                                       @RequestParam("password") String password,
                                       @RequestParam("confirmPassword") String confirmPassword,
                                       Model model) {
        var resetToken = passwordResetTokenRepository.findByToken(token);

        if (resetToken.isEmpty() || resetToken.get().getExpiryDate().isBefore(LocalDateTime.now())) {
            model.addAttribute("error", "Liên kết đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.");
            return "reset-password-error";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("token", token); // giữ lại token để user nhập lại form
            model.addAttribute("error", "Mật khẩu mới và xác nhận không khớp.");
            return "reset-password";
        }

        User user = resetToken.get().getUser();
        user.setPassword(password);
       // user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);

        // ✅ Xóa token sau khi đổi mật khẩu thành công
        passwordResetTokenRepository.delete(resetToken.get());

        model.addAttribute("message", "Đặt lại mật khẩu thành công! Bạn có thể đăng nhập lại.");
        return "redirect:/login?resetSuccess";    }
}
