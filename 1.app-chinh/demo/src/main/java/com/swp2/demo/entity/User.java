package com.swp2.demo.entity;



import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import java.util.ArrayList;
import java.util.List; // Import List


@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})

@Entity
@Table(name="Users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_id")
    private Long id;

    @Column(name="username")
    private String username;

    @Column(name="password", length = 256)
    private String password;

    @Column(name="first_name")
    private String firstName;
    @Column(name="last_name")
    private String lastName;
    @Column(name="email")
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role = Role.Guest;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_plan")
    private Member member ;

    @Column(name = "created_at")
    private LocalDate createdAt = LocalDate.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.OFFLINE;

    // --- New Relationships for cascading delete ---

    // For UserAnswer
    // Add @JsonIgnore to break the circular reference
    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PasswordResetToken> passwordResetTokens;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserAnswer> userAnswers = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuitPlan> quitPlans = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnalysisResultEntity> analysisResults = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<Order> orders = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<Feedback> authoredFeedback = new ArrayList<>();


    // For Feedback (User as coach) - if a user can also be a coach
    // This is more complex, as a coach might have many feedback entries from different users.
    // Deleting a coach user should probably NOT delete all feedback where they are the coach.
    // Instead, consider setting 'coach_id' to NULL or reassigning it.
    // So, for this relationship, we might NOT use cascade.
    // @OneToMany(mappedBy = "coach", cascade = CascadeType.REMOVE)
    // private List<Feedback> coachingFeedback = new ArrayList<>();

    // Payment has user_Id and username, not a direct ManyToOne relationship,
    // so it would need manual handling or a proper JPA relationship if intended.
    // ChatMessage also uses String IDs, likely manual handling is best.

    // --- End New Relationships ---


    public User() {
    }

    // You might need to update your constructor to include new list fields
    // Or just rely on default initialization.

    // Add getters and setters for the new collections

    public User(Long id, String username, String password, String firstName, String lastName, String email, Gender gender, LocalDate dateOfBirth, Role role, Member member, LocalDate createdAt, Status status, List<PasswordResetToken> passwordResetTokens, List<UserAnswer> userAnswers, List<QuitPlan> quitPlans, List<AnalysisResultEntity> analysisResults, List<Order> orders, List<Feedback> authoredFeedback) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.role = role;
        this.member = member;
        this.createdAt = createdAt;
        this.status = status;
        this.passwordResetTokens = passwordResetTokens;
        this.userAnswers = userAnswers;
        this.quitPlans = quitPlans;
        this.analysisResults = analysisResults;
        this.orders = orders;
        this.authoredFeedback = authoredFeedback;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<PasswordResetToken> getPasswordResetTokens() {
        return passwordResetTokens;
    }

    public void setPasswordResetTokens(List<PasswordResetToken> passwordResetTokens) {
        this.passwordResetTokens = passwordResetTokens;
    }

    public List<UserAnswer> getUserAnswers() {
        return userAnswers;
    }

    public void setUserAnswers(List<UserAnswer> userAnswers) {
        this.userAnswers = userAnswers;
    }

    public List<QuitPlan> getQuitPlans() {
        return quitPlans;
    }

    public void setQuitPlans(List<QuitPlan> quitPlans) {
        this.quitPlans = quitPlans;
    }

    public List<AnalysisResultEntity> getAnalysisResults() {
        return analysisResults;
    }

    public void setAnalysisResults(List<AnalysisResultEntity> analysisResults) {
        this.analysisResults = analysisResults;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public List<Feedback> getAuthoredFeedback() {
        return authoredFeedback;
    }

    public void setAuthoredFeedback(List<Feedback> authoredFeedback) {
        this.authoredFeedback = authoredFeedback;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", member=" + member +
                '}';
    }
}