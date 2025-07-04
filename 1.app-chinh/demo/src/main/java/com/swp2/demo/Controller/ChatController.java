package com.swp2.demo.Controller;

import com.swp2.demo.repository.ChatMessageRepository;
import com.swp2.demo.entity.AnalysisResultEntity; // Import AnalysisResultEntity
import com.swp2.demo.entity.ChatMessage;
import com.swp2.demo.entity.QuitPlan;
import com.swp2.demo.entity.User;
import com.swp2.demo.entity.dto.ChatMessageDTO;
import com.swp2.demo.entity.dto.QuitPlanDTO;
import com.swp2.demo.entity.dto.UserDTO;
import com.swp2.demo.service.AnalysisResultService; // Import AnalysisResultService
import com.swp2.demo.service.ChatMessageService;
import com.swp2.demo.service.QuitPlanService;
import com.swp2.demo.service.UserMessageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional; // For handling Optional return
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    private final UserMessageService userService;
    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;
    private final AnalysisResultService analysisResultService;
    private final QuitPlanService quitPlanService;
    // Inject AnalysisResultService

    @GetMapping("/messenger")
    public String chatPage(Model model, Authentication authentication) {
        String currentPrincipalName;

        if (authentication.getPrincipal() instanceof OAuth2User oAuth2User) {
            currentPrincipalName = oAuth2User.getAttribute("email");
            logger.info("OAuth2User detected, principal email: {}", currentPrincipalName);
        } else {
            currentPrincipalName = authentication.getName();
            logger.info("Standard user detected, principal name: {}", currentPrincipalName);
        }

        model.addAttribute("username", currentPrincipalName);

        User currentUser = userService.findUserByUsername(currentPrincipalName);
        if (currentUser == null) {
            logger.error("Authenticated user not found in database for principal: {}", currentPrincipalName);
            throw new RuntimeException("Authenticated user not found in database for principal: " + currentPrincipalName);
        }

        logger.info("Found currentUser: ID={}, Username={}, Email={}, Role={}",
                currentUser.getId(), currentUser.getUsername(), currentUser.getEmail(), currentUser.getRole());

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("currentUserRoleString", currentUser.getRole().name());


        List<User> connectedUsers = userService.findConnectedUsers();

        List<User> otherUsers = connectedUsers.stream()
                .filter(u -> !u.getUsername().equals(currentUser.getUsername()))
                .collect(Collectors.toList());

        User activePartner = otherUsers.isEmpty() ? null : otherUsers.get(0);
        List<ChatMessage> messages = (activePartner != null)
                ? chatMessageService.findChatMessages(currentUser.getUsername(), activePartner.getUsername())
                : new ArrayList<>();

        model.addAttribute("activeConversationPartner", activePartner);
        model.addAttribute("messages", messages);

        User finalActivePartner = activePartner;
        List<ConversationDTO> conversations = otherUsers.stream()
                .map(user -> {
                    boolean isActive = finalActivePartner != null &&
                            finalActivePartner.getUsername().equals(user.getUsername());
                    return new ConversationDTO(user.getUsername(), user, isActive, "No recent messages", "N/A");
                })
                .collect(Collectors.toList());

        model.addAttribute("conversations", conversations);

        return "messenger";
    }

    @GetMapping("/users")
    @ResponseBody
    public ResponseEntity<List<UserDTO>> findConnectedUsers() {
        List<User> connectedUsers = userService.findConnectedUsers();
        List<UserDTO> userDTOs = connectedUsers.stream()
                .map(UserDTO::new) // Assumes UserDTO has a constructor taking a User
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDTOs);
    }

    @GetMapping("/messages/{senderId}/{receiverId}")
    @ResponseBody
    public ResponseEntity<List<ChatMessage>> findChatMessages(
            @PathVariable String senderId,
            @PathVariable String receiverId) {
        return ResponseEntity.ok(chatMessageService.findChatMessages(senderId, receiverId));
    }

    @GetMapping("/api/users/{userId}/profile")
    @ResponseBody
    public ResponseEntity<User> getUserProfile(@PathVariable("userId") String userId) {
        logger.info("Attempting to fetch profile for user: {}", userId);
        User user = userService.findUserByUsername(userId);

        if (user == null) {
            logger.warn("User with ID {} not found for profile request.", userId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User Not Found");
        }
        logger.info("Found user profile for {}: FirstName={}, LastName={}, Email={}",
                userId, user.getFirstName(), user.getLastName(), user.getEmail());
        return ResponseEntity.ok(user);
    }

    /**
     * NEW ENDPOINT: Fetches the latest survey answer (AnalysisResultEntity) for a given user.
     * @param userId The username of the user whose survey answer is to be fetched.
     * @return ResponseEntity containing the latest AnalysisResultEntity if found,
     * or a 404 Not Found status if the user or no survey result is found.
     */
    @GetMapping("/api/users/{userId}/latest-survey-answer")
    @ResponseBody
    public ResponseEntity<AnalysisResultEntity> getLatestSurveyAnswer(@PathVariable("userId") String userId) {
        logger.info("Attempting to fetch latest survey answer for user: {}", userId);
        User user = userService.findUserByUsername(userId);

        if (user == null) {
            logger.warn("User with ID {} not found for survey answer request.", userId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User Not Found");
        }

        // Assuming analysisResultService has a method to find the latest analysis result for a user
        Optional<AnalysisResultEntity> latestAnalysisResult = analysisResultService.findLatestByUserId(user.getId());

        if (latestAnalysisResult.isEmpty()) {
            logger.warn("No latest survey answer found for user ID: {}", user.getId());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No Survey Answer Found for this user.");
        }

        AnalysisResultEntity result = latestAnalysisResult.get();
        logger.info("Found latest survey answer for {}: Analysis='{}', Recommendation='{}', CreatedAt='{}'",
                userId, result.getAnalysis(), result.getRecommendation(), result.getCreatedAt());

        return ResponseEntity.ok(result);
    }
    @GetMapping("/api/users/{userId}/latest-quit-plan") // <--- NEW ENDPOINT
    @ResponseBody
    public ResponseEntity<QuitPlanDTO> getLatestQuitPlan(@PathVariable("userId") String userId) {
        logger.info("Attempting to fetch latest quit plan for user: {}", userId);
        Optional<QuitPlan> quitPlanOptional = quitPlanService.getLatestQuitPlanByUsername(userId);

        if (quitPlanOptional.isEmpty()) {
            logger.warn("No quit plan found for user: {}", userId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Quit Plan Not Found");
        }

        QuitPlanDTO quitPlanDTO = new QuitPlanDTO(quitPlanOptional.get());
        return ResponseEntity.ok(quitPlanDTO);
    }



    // DTO for conversation display
    public static class ConversationDTO {
        private String id;
        private User partner;
        private boolean isActive;
        private String lastMessage;
        private String time;

        public ConversationDTO(String id, User partner, boolean isActive, String lastMessage, String time) {
            this.id = id;
            this.partner = partner;
            this.isActive = isActive;
            this.lastMessage = lastMessage;
            this.time = time;
        }

        public String getId() { return id; }
        public User getPartner() { return partner; }
        public boolean isActive() { return isActive; }
        public String getLastMessage() { return lastMessage; }
        public String getTime() { return time; }

        public void setId(String id) { this.id = id; }
        public void setPartner(User partner) { this.partner = partner; }
        public void setActive(boolean active) { isActive = active; }
        public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
        public void setTime(String time) { this.time = time; }
    }

    @MessageMapping("/chat.sendMessage")
    public void processMessage(@Payload ChatMessageDTO chatMessageDTO) {
        logger.info("Received chat message: senderId='{}', receiverId='{}', content='{}'",
                chatMessageDTO.getSenderId(), chatMessageDTO.getReceiverId(), chatMessageDTO.getContent());

        if (chatMessageDTO.getSenderId() == null || chatMessageDTO.getSenderId().trim().isEmpty()) {
            logger.error("Sender ID is null or empty for message: {}", chatMessageDTO);
            return;
        }
        if (chatMessageDTO.getReceiverId() == null || chatMessageDTO.getReceiverId().trim().isEmpty()) {
            logger.error("Receiver ID is null or empty for message: {}", chatMessageDTO);
            return;
        }

        User senderUser = userService.findUserByUsername(chatMessageDTO.getSenderId());
        User receiverUser = userService.findUserByUsername(chatMessageDTO.getReceiverId());

        if (senderUser == null) {
            logger.error("Sender user not found for ID: '{}'. Message will not be saved.", chatMessageDTO.getSenderId());
            return;
        }
        if (receiverUser == null) {
            logger.error("Receiver user not found for ID: '{}'. Message will not be saved.", chatMessageDTO.getReceiverId());
            return;
        }

        ChatMessage message = ChatMessage.builder()
                .senderId(chatMessageDTO.getSenderId())
                .receiverId(chatMessageDTO.getReceiverId())
                .content(chatMessageDTO.getContent())
                .timestamp(LocalDateTime.now())
                .build();

        try {
            chatMessageRepository.save(message);
            logger.info("Message saved successfully from '{}' to '{}'. Content: '{}'",
                    chatMessageDTO.getSenderId(), chatMessageDTO.getReceiverId(), chatMessageDTO.getContent());
        } catch (Exception e) {
            logger.error("Failed to save chat message: '{}' from '{}' to '{}'. Error: {}",
                    chatMessageDTO.getContent(), chatMessageDTO.getSenderId(), chatMessageDTO.getReceiverId(), e.getMessage(), e);
            throw new RuntimeException("Failed to save chat message", e);
        }

        messagingTemplate.convertAndSendToUser(
                chatMessageDTO.getReceiverId(),
                "/queue/messages",
                chatMessageDTO
        );
        logger.info("Message sent via WebSocket to receiver: '{}'", chatMessageDTO.getReceiverId());
    }
}