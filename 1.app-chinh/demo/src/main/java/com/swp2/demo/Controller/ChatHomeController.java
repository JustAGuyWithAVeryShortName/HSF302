package com.swp2.demo.Controller;

import com.swp2.demo.repository.MessageHomeRepository;
import com.swp2.demo.repository.UserRepository;
import com.swp2.demo.entity.MessageHome;
import com.swp2.demo.entity.User;
import com.swp2.demo.entity.dto.MessageHomeDTO; // Ensure this is correct
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload; // Import Payload
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors; // Import for stream().collect(Collectors.toList())

@Controller
public class ChatHomeController {
    @Autowired
    private MessageHomeRepository messageRepo;

    @Autowired
    private UserRepository userRepository;

    // DTO for incoming WebSocket messages from the client
    // This is what the client sends: { "content": "..." }
    public static class IncomingChatMessageDTO {
        private String content;

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        // Lombok's @Data, @NoArgsConstructor, @AllArgsConstructor could simplify this
    }

    @GetMapping("/api/chat/history")
    @ResponseBody
    public List<MessageHomeDTO> getChatHistory() {
        return messageRepo.findAllByOrderBySentAtAsc()
            .stream()
            .map(MessageHomeDTO::new) // Uses your MessageHomeDTO constructor
            .collect(Collectors.toList()); // Use .collect(Collectors.toList()) for clarity, or .toList() if Java 16+
    }


    @MessageMapping("/chat.send")
    @SendTo("/topic/public")
    // Changed signature to @Payload IncomingChatMessageDTO
    public MessageHomeDTO send(@Payload IncomingChatMessageDTO clientMessage, Principal principal) {
        String username = "anonymous";

        // Determine username from Principal (Spring Security context)
        if (principal instanceof OAuth2AuthenticationToken oauthToken) {
            Map<String, Object> attributes = oauthToken.getPrincipal().getAttributes();
            // Assuming 'email' is the unique identifier you use as username in your User entity
            username = (String) attributes.get("email");
        } else if (principal instanceof Authentication auth) {
            username = auth.getName(); // For standard UserDetails, this is the username
        } else if (principal != null) {
            username = principal.getName();
        }

        // Fetch the User entity from the database
        // Assuming userRepository.findByUsername(username) returns a User object or null
        User user = userRepository.findByUsername(username);
        if (user == null) {
            // This should ideally not happen if @PreAuthorize handles authentication correctly
            throw new RuntimeException("Authenticated user '" + username + "' not found in the database.");
        }

        // Create a new MessageHome entity using the content from the DTO and the fetched User
        MessageHome message = new MessageHome(user, clientMessage.getContent());

        // The 'sentAt' field is automatically set by @PrePersist in the MessageHome entity,
        // so you do NOT need to set it here: message.setSentAt(LocalDateTime.now());

        // Save the message to the database
        messageRepo.save(message);

        // Return a DTO for broadcasting to all clients
        return new MessageHomeDTO(message);
    }
}