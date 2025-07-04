package com.swp2.demo.entity.dto;

import com.swp2.demo.entity.MessageHome;
import com.swp2.demo.entity.User;

import java.time.LocalDateTime;

public class MessageHomeDTO {
    private String content;
    private LocalDateTime sentAt;
    private String username;
    private String firstName;
    private String lastName;

    public MessageHomeDTO(MessageHome message) {
        this.content = message.getContent();
        this.sentAt = message.getSentAt();

        User user = message.getUser();
        if (user != null) {
            this.username = user.getUsername();
            this.firstName = user.getFirstName();
            this.lastName = user.getLastName();
        }
    }

    // Getters and Setters
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
