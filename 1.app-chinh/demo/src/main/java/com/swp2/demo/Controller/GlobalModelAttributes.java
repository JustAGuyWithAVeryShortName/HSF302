package com.swp2.demo.Controller;

import com.swp2.demo.repository.NotificationRepository;
import com.swp2.demo.repository.UserRepository;
import com.swp2.demo.entity.Notification;
import com.swp2.demo.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

import java.util.List;

@ControllerAdvice
public class GlobalModelAttributes {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepo;

    @ModelAttribute
    public void addNotificationsToAllPages(Model model, Authentication authentication) {
        if (authentication == null) return;

        User user = null;

        if (authentication instanceof OAuth2AuthenticationToken oauth) {
            String email = (String) oauth.getPrincipal().getAttributes().get("email");
            user = userRepo.findByEmail(email);
        } else {
            String username = authentication.getName();
            user = userRepo.findByUsername(username);
        }

        if (user != null) {
            List<Notification> allNotifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
            List<Notification> topNotifications = allNotifications.size() > 3
                    ? allNotifications.subList(0, 3)
                    : allNotifications;

            model.addAttribute("notifications", topNotifications);
            model.addAttribute("allNotifications", allNotifications);
        }
    }
}
