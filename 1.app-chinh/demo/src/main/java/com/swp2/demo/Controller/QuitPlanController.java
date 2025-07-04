package com.swp2.demo.Controller;

import java.time.ZoneId;

import com.swp2.demo.repository.NotificationRepository;
import com.swp2.demo.entity.Notification;
import com.swp2.demo.entity.QuitPlan;
import com.swp2.demo.entity.User;
import com.swp2.demo.service.QuitPlanService;
import com.swp2.demo.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller responsible for handling requests related to
 * creating and viewing the user's quit smoking plan.
 */
@Controller
public class QuitPlanController {

    @Autowired
    private QuitPlanService quitPlanService;


    @Autowired
    private UserService userService;

    private User extractUser(Object principal) {
        if (principal instanceof UserDetails userDetails) {
            return userService.findByUsername(userDetails.getUsername());
        }
        if (principal instanceof OAuth2User oauth2User) {
            String email = oauth2User.getAttribute("email");
            return userService.findByEmail(email);
        }
        return null;
    }

    /**
     * Show the form for the user to enter quit smoking plan details.
     *
     * @param model Model to pass data to the view.
     * @return View name "quit-plan".
     */
    @GetMapping("/quit-plan")
    public String showQuitPlanForm(@AuthenticationPrincipal Object principal, Model model) {
        User user = extractUser(principal);
        if (user == null) {
            return "redirect:/login";
        }

        // Nếu model chưa có → nạp từ DB
        if (!model.containsAttribute("plan")) {
            List<QuitPlan> plans = quitPlanService.getPlansByUserId(user.getId());
            if (!plans.isEmpty()) {
                model.addAttribute("plan", plans.get(0)); // Lấy plan hiện tại để edit
            } else {
                model.addAttribute("plan", new QuitPlan()); // Nếu chưa có → khởi tạo mới
            }
        }
        return "quit-plan";
    }

    /**
     * Handle form submission for creating the quit plan.
     * This method performs validation, generates default plan content if needed,
     * saves the plan in user's session, then redirects to dashboard.
     *
     * @param plan               The QuitPlan object bound from the form.
     * @param reasons            List of selected reasons.
     * @param reasonDetails      Details for "other" reason.
     * @param session            HttpSession object to store the plan.
     * @param redirectAttributes For passing error messages back to the form on redirect.
     * @return Redirect string to dashboard or back to form if errors.
     */
    @PostMapping("/plan/generate")
    public String handlePlanSubmission(@AuthenticationPrincipal Object principal,
                                       @ModelAttribute("plan") QuitPlan plan,
                                       @RequestParam(required = false) List<String> reasons,
                                       @RequestParam(required = false) String reasonDetails,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes) {


        populatePlanFromRequest(plan, reasons, reasonDetails);
        // --- Validation ---
        if (plan.getStartDate() != null && plan.getTargetDate() != null &&
                plan.getTargetDate().isBefore(plan.getStartDate())) {
            redirectAttributes.addFlashAttribute("dateError", "Target date must be the same as or after the start date.");
            redirectAttributes.addFlashAttribute("plan", plan);
            return "redirect:/quit-plan";
        }

        switch (plan.getStages()) {
            case "Gradual reduction" -> plan.setMethod("reduce_gradually");
            case "Cold turkey", "Cold turkey (quit abruptly)" -> plan.setMethod("quit_abruptly");
            default -> plan.setMethod("reduce_gradually");
        }


        if ((plan.getCustomPlan() == null || plan.getCustomPlan().isBlank()) && !"custom".equals(plan.getStages())) {
            String suggestion = generatePlanSuggestion(plan);
            plan.setCustomPlan(suggestion);
        }

        User user = extractUser(principal);
        if (user == null) {
            return "redirect:/login";
        }
        plan.setUser(user);



     //   plan.setMethod(plan.getStages());


// 👉 Kiểm tra và sửa nếu ngày startDate bị lệch do múi giờ
        ZoneId serverZone = ZoneId.systemDefault();
        ZoneId vietnamZone = ZoneId.of("Asia/Ho_Chi_Minh");

       // System.out.println("🔍 Start Date from DB: " + plan.getStartDate());
       // System.out.println("🕒 Server Timezone: " + serverZone);

       // System.out.println("📌 Stage được chọn: " + plan.getStages());
       // System.out.println("✅ Method được lưu: " + plan.getMethod());


        LocalDate nowVN = LocalDate.now(vietnamZone);
        if (plan.getStartDate() != null && plan.getStartDate().isAfter(nowVN.plusDays(1))) {
            System.out.println("⚠️ Start date is suspiciously ahead of current date in VN. Resetting to today.");
            plan.setStartDate(nowVN);
        }

        quitPlanService.save(plan);

        //  User user = (User) session.getAttribute("loggedInUser");
        //  if (user == null) {
        //    System.out.println("⚠️ User in session is NULL");}


        session.setAttribute("userQuitPlan", plan);
        return "redirect:/user/plan";
    }

    /**
     * Show the dashboard page with detailed user quit plan.
     *
     * @param session HttpSession to retrieve saved plan.
     * @param model   Model to pass data to view.
     * @return View name "user-plan" or redirect if no plan found.
     */
    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping("/user/plan")
    public String viewUserPlan(@RequestParam(name = "month", required = false) Integer month,
                               @RequestParam(name = "year", required = false) Integer year,
                               HttpSession session,
                               Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        QuitPlan currentPlan = (QuitPlan) session.getAttribute("userQuitPlan");
        if (currentPlan == null) {
            List<QuitPlan> plans = quitPlanService.getPlansByUserId(user.getId());
            if (!plans.isEmpty()) {
                currentPlan = plans.get(0);
                session.setAttribute("userQuitPlan", currentPlan);
            }
        }

        if (currentPlan == null) return "redirect:/quit-plan";

        // ✅ Lấy tháng/năm từ request (nếu có) hoặc dùng tháng hiện tại
        YearMonth calendarMonth = (month != null && year != null)
                ? YearMonth.of(year, month)
                : YearMonth.now();

        model.addAttribute("plan", currentPlan);
        model.addAttribute("progress", calculateProgress(currentPlan));
        model.addAttribute("calendar", generateCalendarData(currentPlan, calendarMonth));
        model.addAttribute("monthDisplay", calendarMonth.getMonthValue() + "/" + calendarMonth.getYear());
        model.addAttribute("stats", calculateStats(currentPlan));

        // thông báo

        LocalDate today = LocalDate.now();
        LocalDate startDate = currentPlan.getStartDate();
        LocalDate endDate = currentPlan.getTargetDate();

        if (today.isAfter(endDate)) {
            String todayMessage = "🎉 Bạn đã hoàn thành kế hoạch cai thuốc!";
            boolean exists = notificationRepository.existsByUserIdAndContentAndCreatedAtBetween(
                    user.getId(),
                    todayMessage,
                    today.atStartOfDay(),
                    today.plusDays(1).atStartOfDay()
            );
            if (!exists) {
                Notification n = new Notification(user.getId(), todayMessage);
                notificationRepository.save(n);
            }
        } else if (!today.isBefore(startDate)) {
            long daysPassed = ChronoUnit.DAYS.between(startDate, today) ;
            long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
            long daysLeft = totalDays - daysPassed;

            String todayMessage;
            if (daysPassed == 0) {
                todayMessage = "🚀 Chúc mừng bạn đã bắt đầu kế hoạch cai thuốc hôm nay! Cố lên!";
            } else {
                todayMessage = "Bạn đã cai được " + daysPassed + " ngày! Cố lên, còn " + daysLeft + " ngày nữa!";
            }

            boolean exists = notificationRepository.existsByUserIdAndContentAndCreatedAtBetween(
                    user.getId(),
                    todayMessage,
                    today.atStartOfDay(),
                    today.plusDays(1).atStartOfDay()
            );
            if (!exists) {
                Notification n = new Notification(user.getId(), todayMessage);
                notificationRepository.save(n);
            }
        }


        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        model.addAttribute("notifications", notifications);


        return "user-plan";
    }
    @PostMapping("/user/plan/delete-notifications")
    @Transactional
    public String deleteAllNotifications(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        notificationRepository.deleteByUserId(user.getId());

        return "redirect:/notice";
    }




    @Transactional
    @GetMapping("/plan/new")
    public String createNewPlan(@AuthenticationPrincipal Object principal, HttpSession session) {
        User user = extractUser(principal);
        if (user != null) {
            quitPlanService.deleteByUserId(user.getId()); // Xoá tất cả kế hoạch cũ
            session.removeAttribute("userQuitPlan");
        }
        return "redirect:/questionnaire";
    }

    @GetMapping("/plan/edit")
    public String editPlan(@AuthenticationPrincipal Object principal, HttpSession session) {
        session.removeAttribute("userQuitPlan");
        User user = extractUser(principal);
        if (user != null) {
            List<QuitPlan> plans = quitPlanService.getPlansByUserId(user.getId());
            if (!plans.isEmpty()) {
                QuitPlan plan = plans.get(0);
                plan.setCustomPlan(null); // Xoá để regenerate khi /quit-plan
                quitPlanService.save(plan);
            }
        }
        return "redirect:/quit-plan";
    }


    // ===================================================================================
    // PRIVATE HELPER METHODS
    // ===================================================================================

    private void populatePlanFromRequest(QuitPlan plan, List<String> reasons, String reasonDetails) {
        List<String> fullReasons = new ArrayList<>();
        if (reasons != null) {
            fullReasons.addAll(reasons.stream()
                    .map(this::mapReasonValueToText) // Map values to readable text
                    .collect(Collectors.toList()));
        }
        // Add detailed reason if user entered it
        if (reasonDetails != null && !reasonDetails.isBlank()) {
            // Remove default "Other" and replace with details
            fullReasons.remove("Other");
            fullReasons.add(reasonDetails);
        }
        plan.setReasons(fullReasons);
    }

    private String mapReasonValueToText(String value) {
        return switch (value) {
            case "health" -> "Improve health";
            case "family" -> "For family and loved ones";
            case "financial" -> "Save money";
            case "smell" -> "Eliminate bad smell";
            case "other" -> "Other"; // Keep as placeholder
            default -> value;
        };
    }




   private String generatePlanSuggestion(QuitPlan plan) {
        if (plan.getStages() == null) return "Please select a method to get detailed suggestions.";
        return switch (plan.getStages()) {
            case "Gradual reduction" ->
                    "Week 1: Reduce 2 cigarettes per day.\nWeek 2: Reduce another 3 cigarettes per day.\nWeek 3: Try to go 24 hours without smoking.\nAlways have sugar-free gum or healthy snacks ready.";
            case "Cold turkey (quit abruptly)" ->
                    "Throw away all cigarettes, lighters, and ashtrays.\nInform family and friends about your decision for support.\nWhen cravings hit, drink a glass of cold water or take a walk.";
            case "Nicotine replacement therapy" ->
                    "Start using nicotine patches from " + plan.getStartDate() + ".\nConsult a pharmacist for appropriate dosage.\nCombine with habit changes (e.g., drink tea instead of coffee).";
            default -> "";
        };
    }

    private int calculateProgress(QuitPlan plan) {
        if (plan.getStartDate() == null || plan.getTargetDate() == null || plan.getStartDate().isAfter(plan.getTargetDate()))
            return 0;

        LocalDate today = LocalDate.now();
        if (today.isBefore(plan.getStartDate())) return 0;
        if (today.isAfter(plan.getTargetDate())) return 100;

        long totalDays = ChronoUnit.DAYS.between(plan.getStartDate(), plan.getTargetDate());
        if (totalDays <= 0) return 100;

        long daysPassed = ChronoUnit.DAYS.between(plan.getStartDate(), today);
        long progress = (daysPassed * 100) / totalDays;
        return (int) Math.min(progress, 100);
    }

    /**
     * Calculate key stats for dashboard.
     */
    private QuitStats calculateStats(QuitPlan plan) {
        if (plan.getStartDate() == null || plan.getTargetDate() == null) {
            return new QuitStats(0, 0);
        }

        LocalDate today = LocalDate.now();
        long daysSmokeFree = 0;
        // Days smoke-free from start date to today
        if (!today.isBefore(plan.getStartDate())) {
            daysSmokeFree = ChronoUnit.DAYS.between(plan.getStartDate(), today);
        }

        long daysRemaining = 0;
        // Days left from today to target date
        if (today.isBefore(plan.getTargetDate())) {
            daysRemaining = ChronoUnit.DAYS.between(today, plan.getTargetDate());
        }

        return new QuitStats(daysSmokeFree, daysRemaining);
    }

    private CalendarData generateCalendarData(QuitPlan plan, YearMonth month) {
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = month.atDay(1);
        int firstDayOfWeekValue = firstDayOfMonth.getDayOfWeek().getValue(); // Monday = 1

        List<CalendarDay> days = new ArrayList<>();
        for (int i = 1; i < firstDayOfWeekValue; i++) {
            days.add(new CalendarDay(0, "empty"));
        }

        for (int day = 1; day <= month.lengthOfMonth(); day++) {
            LocalDate date = month.atDay(day);
            String status = getDayStatus(date, today, plan.getStartDate(), plan.getTargetDate());
            days.add(new CalendarDay(day, status));
        }

        return new CalendarData(month.toString(), List.of("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"), days);
    }

    // ===== PHƯƠNG THỨC ĐÃ ĐƯỢC CẬP NHẬT =====
    private String getDayStatus(LocalDate date, LocalDate today, LocalDate startDate, LocalDate targetDate) {
        if (startDate == null || targetDate == null) {
            return "disabled";
        }

        // --- Ưu tiên 1: Đánh dấu ngày bắt đầu và ngày mục tiêu TRƯỚC TIÊN ---
        if (date.isEqual(startDate)) {
            return "start-date"; // Trạng thái mới cho ngày bắt đầu
        }
        if (date.isEqual(targetDate)) {
            return "target-date"; // Trạng thái mới cho ngày mục tiêu
        }

        // --- Ưu tiên 2: Đánh dấu ngày hôm nay (chỉ khi nó không phải ngày bắt đầu/mục tiêu) ---
        if (date.isEqual(today)) {
            return "today";
        }

        // --- Ưu tiên 3: Đánh dấu các giai đoạn của kế hoạch ---
        if (date.isBefore(startDate)) {
            return "pre-plan"; // Những ngày trước khi kế hoạch bắt đầu
        }
        if (date.isAfter(targetDate)) {
            return "post-plan"; // Những ngày sau khi kế hoạch hoàn thành
        }

        // Đối với những ngày nằm trong khoảng thời gian của kế hoạch
        if (date.isBefore(today)) {
            return "past";   // Một ngày trong kế hoạch đã trôi qua
        }
        if (date.isAfter(today)) {
            return "future";  // Một ngày trong kế hoạch sắp tới
        }

        return "disabled"; // Trạng thái mặc định an toàn
    }


    // ===================================================================================
    // DATA CLASSES (DTOs)
    // ===================================================================================


    /**
     * Dashboard statistics record.
     *
     * @param daysSmokeFree number of days without smoking.
     * @param daysRemaining days left to the target.
     */
    public record QuitStats(long daysSmokeFree, long daysRemaining) {
    }

    /**
     * Calendar data container for the month view.
     */
    public record CalendarData(String monthName, List<String> weekDays, List<CalendarDay> days) {
    }

    /**
     * Information about a calendar day.
     */
    public record CalendarDay(int dayNumber, String status) {
    }
}