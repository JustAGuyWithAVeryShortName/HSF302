package com.swp2.demo.entity.dto; // Or com.swp2.demo.dto

import com.swp2.demo.entity.User; // Import your User entity
import com.swp2.demo.entity.Gender; // Import Gender enum
import com.swp2.demo.entity.Role;   // Import Role enum
import com.swp2.demo.entity.Member; // Import Member enum
import com.swp2.demo.entity.Status; // Import Status enum
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder; // Optional, but good for building DTOs if needed

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder // Add builder for convenient DTO creation if you prefer
public class UserDTO {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private Gender gender;
    private LocalDate dateOfBirth;
    private Role role;
    private Member member;
    private LocalDate createdAt;
    private Status status;

    // Constructor to convert from User entity to UserDTO
    public UserDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.gender = user.getGender();
        this.dateOfBirth = user.getDateOfBirth();
        this.role = user.getRole();
        this.member = user.getMember();
        this.createdAt = user.getCreatedAt();
        this.status = user.getStatus();
    }
}