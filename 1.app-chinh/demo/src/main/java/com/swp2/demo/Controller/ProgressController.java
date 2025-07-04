package com.swp2.demo.Controller;

import com.swp2.demo.entity.QuitPlan;
import com.swp2.demo.entity.UserPlanStep;
import com.swp2.demo.repository.QuitPlanRepository;
import com.swp2.demo.repository.UserPlanStepRepository;
import com.swp2.demo.service.QuitPlanService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ProgressController {

    @Autowired
    private QuitPlanRepository quitPlanRepository;

    @Autowired
    private UserPlanStepRepository userPlanStepRepository;

    @Autowired
    private QuitPlanService quitPlanService;

    public static class HealthProgress {
        private String currentStatus;
        private int percentage;
        private String nextMilestone;

        public HealthProgress(String currentStatus, int percentage, String nextMilestone) {
            this.currentStatus = currentStatus;
            this.percentage = percentage;
            this.nextMilestone = nextMilestone;
        }

        public String getCurrentStatus() {
            return currentStatus;
        }

        public int getPercentage() {
            return percentage;
        }

        public String getNextMilestone() {
            return nextMilestone;
        }
    }

    public static class Milestone {
        private String name;
        private int daysToAchieve;

        public Milestone(String name, int daysToAchieve) {
            this.name = name;
            this.daysToAchieve = daysToAchieve;
        }

        public String getName() {
            return name;
        }

        public int getDaysToAchieve() {
            return daysToAchieve;
        }
    }

    @GetMapping("/track-progress")
    public String trackProgress(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        QuitPlan plan = quitPlanRepository.findLatestByUserId(userId).stream().findFirst().orElse(null);
        if (plan == null) {
            redirectAttributes.addFlashAttribute("noPlanMessage", "You haven't created a quit plan yet. Please create one to track your progress!");
            return "redirect:/dashboard";
        }

        ZoneId zoneVN = ZoneId.of("Asia/Ho_Chi_Minh");
        ZonedDateTime now = ZonedDateTime.now(zoneVN);
        ZonedDateTime startDateTime = plan.getStartDate().atStartOfDay(zoneVN);
        long daysSinceQuit = Duration.between(startDateTime, now).toDays();
        if (daysSinceQuit < 0) daysSinceQuit = 0;

        model.addAttribute("daysSinceQuit", daysSinceQuit);
        model.addAttribute("healthProgress", getHealthProgress(daysSinceQuit));
        model.addAttribute("achievedMilestones", getAchievedMilestones(daysSinceQuit, defineMilestones()));
        model.addAttribute("upcomingMilestones", getUpcomingMilestones(daysSinceQuit, defineMilestones()));

        List<UserPlanStep> originalSteps = userPlanStepRepository.findByQuitPlan(plan);
        List<UserPlanStep> displaySteps = new ArrayList<>();

        int daily = plan.getDailySmokingCigarettes() != null ? plan.getDailySmokingCigarettes() : 20;
        boolean missedTarget = false;

        for (UserPlanStep step : originalSteps) {
            UserPlanStep copy = new UserPlanStep();
            copy.setId(step.getId());
            copy.setDate(step.getDate());
            copy.setDayIndex(step.getDayIndex());
            copy.setActualCigarettes(step.getActualCigarettes());
            copy.setQuitPlan(step.getQuitPlan());
            copy.setTargetCigarettes(step.getTargetCigarettes());

            Integer target = step.getTargetCigarettes();
            Integer actual = step.getActualCigarettes();

            if (actual != null && target != null) {
                int avoidedToday = daily - actual;
                copy.setAvoidedCigarettes(avoidedToday);
                copy.setCompleted(actual <= target); // ✅ So với target
                if (actual > target) {
                    missedTarget = true;
                }
            } else {
                copy.setCompleted(false); // ✅ Nếu chưa nhập hoặc thiếu target thì chưa hoàn thành
            }

            displaySteps.add(copy);
        }

        Map<Integer, List<UserPlanStep>> stepsByWeek = new TreeMap<>();
        for (UserPlanStep step : displaySteps) {
            int week = (int) ChronoUnit.WEEKS.between(plan.getStartDate(), step.getDate()) + 1;
            stepsByWeek.computeIfAbsent(week, k -> new ArrayList<>()).add(step);
        }

        model.addAttribute("stepsByWeek", stepsByWeek);
        model.addAttribute("planId", plan.getId());

        BigDecimal dailySpend = plan.getDailySpending() != null ? plan.getDailySpending() : BigDecimal.ZERO;
        BigDecimal pricePerCig = daily > 0
                ? dailySpend.divide(BigDecimal.valueOf(daily), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        int cumulativeAvoided = 0;
        for (UserPlanStep s : displaySteps) {
            Integer actual = s.getActualCigarettes();
            if (actual != null) {
                cumulativeAvoided += (daily - actual);
            }
        }

        int avoided = cumulativeAvoided;
        int saved = avoided > 0
                ? pricePerCig.multiply(BigDecimal.valueOf(avoided)).intValue()
                : 0;

        model.addAttribute("cigarettesAvoided", Math.max(0, avoided));
        model.addAttribute("cigarettesOver", Math.abs(Math.min(0, avoided)));
        model.addAttribute("moneySaved", String.format("%,d", saved));
        model.addAttribute("method", plan.getMethod());
        model.addAttribute("missedTarget", missedTarget);

        return "track-progress";
    }

    @PostMapping("/track-progress/update/{stepId}")
    public String updateStep(
            @PathVariable Long stepId,
            @RequestParam("actualCigarettes") int actual,
            @RequestParam("planId") Long planId) {

        UserPlanStep step = userPlanStepRepository.findById(stepId).orElse(null);
        if (step != null) {
            step.setActualCigarettes(actual);

            QuitPlan plan = step.getQuitPlan();
            int daily = plan.getDailySmokingCigarettes() != null ? plan.getDailySmokingCigarettes() : 20;

            int avoided = daily - actual;
            step.setAvoidedCigarettes(avoided);

            if (step.getTargetCigarettes() != null) {
                step.setCompleted(actual <= step.getTargetCigarettes()); // ✅ So với target
            } else {
                step.setCompleted(false);
            }

            int pricePerCigarette = 0;
            if (daily > 0 && plan.getDailySpending() != null) {
                pricePerCigarette = plan.getDailySpending()
                        .divide(BigDecimal.valueOf(daily), 2, RoundingMode.HALF_UP)
                        .intValue();
            }

            step.setMoneySaved(Math.max(0, avoided) * pricePerCigarette);
            userPlanStepRepository.save(step);
        }
        return "redirect:/track-progress";
    }

    private HealthProgress getHealthProgress(long days) {
        if (days < 1)
            return new HealthProgress("Heart rate starts to decrease.", 10, "CO level in blood drops (1 day)");
        if (days < 3)
            return new HealthProgress("CO drops, oxygen level increases.", 25, "Nicotine eliminated (3 days)");
        if (days < 14)
            return new HealthProgress("Nicotine cleared from your body.", 40, "Lung function improves (2 weeks)");
        if (days < 30)
            return new HealthProgress("Better breathing and lung capacity.", 60, "Blood circulation improves (1 month)");
        if (days < 90)
            return new HealthProgress("Improved blood flow, more energy.", 80, "Lungs begin to repair (3 months)");
        return new HealthProgress("Lungs are actively self-repairing.", 100, "You're doing great!");
    }

    private List<Milestone> defineMilestones() {
        return Arrays.asList(
                new Milestone("Golden Day", 1),
                new Milestone("Nicotine Cleansing", 3),
                new Milestone("First Week", 7),
                new Milestone("Improved Breathing", 14),
                new Milestone("One-Month Mark", 30),
                new Milestone("Energy Returns", 90),
                new Milestone("Half-Year Milestone", 180),
                new Milestone("One Year Celebration", 365)
        );
    }

    private List<Milestone> getAchievedMilestones(long days, List<Milestone> all) {
        return all.stream().filter(m -> days >= m.getDaysToAchieve()).collect(Collectors.toList());
    }

    private List<Milestone> getUpcomingMilestones(long days, List<Milestone> all) {
        return all.stream().filter(m -> days < m.getDaysToAchieve()).limit(2).collect(Collectors.toList());
    }
}
