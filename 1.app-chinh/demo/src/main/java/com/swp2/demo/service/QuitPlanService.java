package com.swp2.demo.service;

import com.swp2.demo.repository.QuitPlanRepository;
import com.swp2.demo.entity.QuitPlan;
import com.swp2.demo.entity.UserPlanStep;
import com.swp2.demo.repository.UserPlanStepRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class QuitPlanService {

    @Autowired
    private QuitPlanRepository repository;

    @Autowired
    private UserPlanStepRepository userPlanStepRepository;

    public QuitPlan save(QuitPlan plan) {
        QuitPlan saved = repository.save(plan);
        generateDailyPlan(saved, saved.getDailySmokingCigarettes() != null ? saved.getDailySmokingCigarettes() : 20);
        return saved;
    }

    public List<QuitPlan> getPlansByUserId(Long userId) {
        return repository.findLatestByUserId(userId);
    }

    public boolean hasPlan(Long userId) {
        return repository.existsByUserId(userId);
    }

    public void deleteByUserId(Long userId) {
        repository.deleteByUserId(userId);
    }

    public void generateDailyPlan(QuitPlan quitPlan, int initialCigarettesPerDay) {
        LocalDate start = quitPlan.getStartDate();
        LocalDate end = quitPlan.getTargetDate();

        long totalDays = ChronoUnit.DAYS.between(start, end) + 1;
        String method = quitPlan.getMethod();


        int pricePerCigarette = 0;
        if (quitPlan.getDailySmokingCigarettes() != null && quitPlan.getDailySmokingCigarettes() > 0) {
            BigDecimal dailySpending = quitPlan.getDailySpending();
            BigDecimal dailyCigarettes = BigDecimal.valueOf(quitPlan.getDailySmokingCigarettes());
            pricePerCigarette = dailySpending
                    .divide(dailyCigarettes, RoundingMode.HALF_UP)
                    .intValue();
        }

        for (int i = 0; i < totalDays; i++) {
            UserPlanStep step = new UserPlanStep();
            step.setQuitPlan(quitPlan);
            step.setDate(start.plusDays(i));
            step.setDayIndex(i + 1);

            int target;

            if ("quit_abruptly".equalsIgnoreCase(method)) {
                target = 0;
            } else {
                int dailyDecrease = (int) Math.ceil((double) initialCigarettesPerDay / totalDays);
                target = Math.max(initialCigarettesPerDay - (i * dailyDecrease), 0);
            }

            step.setTargetCigarettes(target);
            step.setCompleted(false);
            step.setActualCigarettes(null);

            // 👉 TÍNH số điếu tránh (giả định người dùng *không hút*)
            int avoided = initialCigarettesPerDay - target;
            step.setAvoidedCigarettes(Math.max(avoided, 0));

            // 👉 TÍNH tiền tiết kiệm
            step.setMoneySaved(step.getAvoidedCigarettes() * pricePerCigarette);

            userPlanStepRepository.save(step);
        }
    }

    public int calculateCigarettesAvoided(List<UserPlanStep> steps) {
        return steps.stream()
                .filter(s -> s.getActualCigarettes() != null)
                .mapToInt(s -> s.getTargetCigarettes() - s.getActualCigarettes()) // ✔️ bỏ Math.max
                .sum();
    }


    public int calculateMoneySaved(List<UserPlanStep> steps, int pricePerCigarette) {
        return steps.stream()
                .filter(s -> s.getActualCigarettes() != null)
                .mapToInt(s -> Math.max(0, s.getTargetCigarettes() - s.getActualCigarettes()) * pricePerCigarette)
                .sum();
    }
    public Optional<QuitPlan> getLatestQuitPlanByUsername(String username) {
        return repository.findTopByUser_UsernameOrderByIdDesc(username);
    }


}

