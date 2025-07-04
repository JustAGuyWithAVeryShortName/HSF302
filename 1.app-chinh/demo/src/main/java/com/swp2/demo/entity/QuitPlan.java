package com.swp2.demo.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "quit_plan")
public class QuitPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection
    @CollectionTable(name = "quit_plan_reasons", joinColumns = @JoinColumn(name = "plan_id"))
    @Column(name = "reason")
    private List<String> reasons;

    private LocalDate startDate;
    private LocalDate targetDate;
    private String stages;

    @Column(name = "method", nullable = false)
    private String method;

    @PrePersist
    public void ensureDefaultMethod() {
        if (this.method == null) {
            this.method = "reduce_gradually";
        }
    }

    @Column(length = 2000)
    private String customPlan;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "daily_smoking_cigarettes")
    private Integer dailySmokingCigarettes;

    @Column(name = "daily_spending")
    private BigDecimal dailySpending;

    // ======= GETTERS & SETTERS =======
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public void setReasons(List<String> reasons) {
        this.reasons = reasons;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }

    public String getStages() {
        return stages;
    }

    public void setStages(String stages) {
        this.stages = stages;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getCustomPlan() {
        return customPlan;
    }

    public void setCustomPlan(String customPlan) {
        this.customPlan = customPlan;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getDailySmokingCigarettes() {
        return dailySmokingCigarettes;
    }

    public void setDailySmokingCigarettes(Integer dailySmokingCigarettes) {
        this.dailySmokingCigarettes = dailySmokingCigarettes;
    }

    public BigDecimal getDailySpending() {
        return dailySpending;
    }

    public void setDailySpending(BigDecimal dailySpending) {
        this.dailySpending = dailySpending;
    }
    public int getPricePerCigarette() {
        if (dailySpending != null && dailySmokingCigarettes != null && dailySmokingCigarettes > 0) {
            return dailySpending
                    .divide(new BigDecimal(dailySmokingCigarettes), 0, java.math.RoundingMode.HALF_UP)
                    .intValue();
        }
        return 0;
    }

}
