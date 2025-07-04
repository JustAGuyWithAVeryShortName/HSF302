package com.swp2.demo.service;


import com.swp2.demo.repository.PasswordResetTokenRepository;
import com.swp2.demo.entity.PasswordResetToken;
import com.swp2.demo.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetTokenService {

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private EmailService emailService;

    @Transactional // ⬅️ Để tránh TransactionRequiredException
    public void createPasswordResetToken(User user) {
        // ✅ Nếu muốn chỉ giữ token mới nhất → xóa token cũ
        passwordResetTokenRepository.deleteByUserId(user.getId());
        passwordResetTokenRepository.flush();

        String newToken = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(2);

        PasswordResetToken token = new PasswordResetToken(newToken, expiryDate, user);
        passwordResetTokenRepository.save(token);

        // ✅ Gửi email
        String resetLink = "http://localhost:8080/reset-password?token=" + newToken;
        emailService.sendEmail(user.getEmail(), "Reset Password", "Click here to reset: " + resetLink);
    }
}
