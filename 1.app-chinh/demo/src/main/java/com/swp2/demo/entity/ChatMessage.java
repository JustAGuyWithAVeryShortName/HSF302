package com.swp2.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@Table(name = "chat_message") // Ensure this matches your table name
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Increase the length for senderId to accommodate email addresses (e.g., 255 characters)
    @Column(name = "sender_id", nullable = false, length = 255)
    private String senderId;

    // Increase the length for receiverId to accommodate email addresses (e.g., 255 characters)
    @Column(name = "receiver_id", nullable = false, length = 255)
    private String receiverId;

    @Column(name = "content", nullable = false, columnDefinition = "NVARCHAR(MAX)") // Use NVARCHAR(MAX) for potentially long messages
    private String content;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    // You might also consider adding an index for faster lookups on senderId and receiverId
    // @Index(name = "idx_sender_receiver", columnList = "sender_id,receiver_id")
}

