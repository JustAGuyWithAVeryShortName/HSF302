package com.swp2.demo.web;

import com.swp2.demo.entity.Gender;
import jakarta.persistence.Column;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public class RegisterUser {
// NoyBlank ko dc bỏ trống
    @NotBlank(message = "thông tin bắt buộc")
    @Size(min = 1, message = "tối thiểu 1")
    private String username;

    @NotBlank(message = "thông tin bắt buộc")
    @Size(min = 1, message = "tối thiểu 1")

    private String password;


    private String email;


    @NotBlank(message = "thông tin bắt buộc")
    private String firstName;

    @NotBlank(message = "thông tin bắt buộc")
    private String lastName;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotNull(message = "Date of birth is required")
    @PastOrPresent(message = "Date of birth cannot be in the future")
    private LocalDate dateOfBirth;

    public RegisterUser() {
    }

    public RegisterUser(String username, String password, String email, String firstName, String lastName, Gender gender, LocalDate dateOfBirth) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
}
