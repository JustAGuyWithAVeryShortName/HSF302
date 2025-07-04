package com.swp2.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "user_plan_step")
public class UserPlanStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    private int dayIndex;

    private Integer targetCigarettes;

    private Integer actualCigarettes;

    private boolean completed;

    private Integer avoidedCigarettes;
    private Integer moneySaved;


    @ManyToOne
    @JoinColumn(name = "quit_plan_id")
    private QuitPlan quitPlan;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getDayIndex() {
        return dayIndex;
    }

    public void setDayIndex(int dayIndex) {
        this.dayIndex = dayIndex;
    }

    public Integer getTargetCigarettes() {
        return targetCigarettes;
    }

    public void setTargetCigarettes(Integer targetCigarettes) {
        this.targetCigarettes = targetCigarettes;
    }

    public Integer getActualCigarettes() {
        return actualCigarettes;
    }

    public void setActualCigarettes(Integer actualCigarettes) {
        this.actualCigarettes = actualCigarettes;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Integer getAvoidedCigarettes() {
        return avoidedCigarettes;
    }

    public void setAvoidedCigarettes(Integer avoidedCigarettes) {
        this.avoidedCigarettes = avoidedCigarettes;
    }

    public Integer getMoneySaved() {
        return moneySaved;
    }

    public void setMoneySaved(Integer moneySaved) {
        this.moneySaved = moneySaved;
    }

    public QuitPlan getQuitPlan() {
        return quitPlan;
    }

    public void setQuitPlan(QuitPlan quitPlan) {
        this.quitPlan = quitPlan;
    }
}
