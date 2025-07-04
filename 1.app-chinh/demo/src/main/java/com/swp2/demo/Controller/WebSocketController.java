package com.swp2.demo.Controller;

import com.swp2.demo.entity.*;
import com.swp2.demo.entity.dto.ChatMessageDTO;
import com.swp2.demo.service.ChatMessageService;
import com.swp2.demo.service.UserMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.Date;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService;
    private final UserMessageService userService;

    @MessageMapping("/user.addUser")
    public void addUser(@Payload ChatMessageDTO chatMessageDto) {
        userService.setStatus(chatMessageDto.getSenderId(), Status.ONLINE);
    }

    @MessageMapping("/user.disconnectUser")
    public void disconnectUser(@Payload ChatMessageDTO chatMessageDto) {
        userService.setStatus(chatMessageDto.getSenderId(), Status.OFFLINE);
    }

    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessageDTO chatMessageDto) {
        // Create a ChatMessage entity from the DTO
        ChatMessage messageToSave = new ChatMessage();
        messageToSave.setSenderId(chatMessageDto.getSenderId());
        messageToSave.setReceiverId(chatMessageDto.getReceiverId());
        messageToSave.setContent(chatMessageDto.getContent());
        messageToSave.setTimestamp(LocalDateTime.now());

        chatMessageService.save(messageToSave);

        // Send the message to the specific user's queue
        messagingTemplate.convertAndSendToUser(
                chatMessageDto.getReceiverId(),
                "/queue/messages",
                chatMessageDto
        );
    }
}