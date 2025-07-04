package com.swp2.demo.entity.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessageDTO {
    private String senderId;
    private String receiverId;
    private String content;
    private LocalDateTime timestamp;
}