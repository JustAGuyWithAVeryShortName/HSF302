package com.swp2.demo.entity.dto;

import com.swp2.demo.entity.QuitPlan; // Import your QuitPlan entity
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuitPlanDTO {
    private Long id;
    private List<String> reasons;
    private LocalDate startDate;
    private LocalDate targetDate;
    private String stages;
    private String method;
    private String customPlan; // This corresponds to 'Plans' in your HTML
    private Integer dailySmokingCigarettes; // This corresponds to 'Daily Cigarettes' in your HTML
    private BigDecimal dailySpending;       // This corresponds to 'Daily Spending' in your HTML

    public QuitPlanDTO(QuitPlan quitPlan) {
        this.id = quitPlan.getId();
        this.reasons = quitPlan.getReasons();
        this.startDate = quitPlan.getStartDate();
        this.targetDate = quitPlan.getTargetDate();
        this.stages = quitPlan.getStages();
        this.method = quitPlan.getMethod();
        this.customPlan = quitPlan.getCustomPlan();
        this.dailySmokingCigarettes = quitPlan.getDailySmokingCigarettes();
        this.dailySpending = quitPlan.getDailySpending();
    }
}