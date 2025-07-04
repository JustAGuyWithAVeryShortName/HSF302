package com.swp2.demo.Controller;

import com.swp2.demo.repository.AnalysisResultRepository;
import com.swp2.demo.repository.OptionRepository;
import com.swp2.demo.repository.QuestionRepository;
import com.swp2.demo.repository.UserAnswerRepository;
import com.swp2.demo.entity.*;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@Controller
public class QuestionController {

    @Autowired
    private AnalysisResultRepository analysisResultRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private OptionRepository questionOptionRepository;

    @Autowired
    private UserAnswerRepository userAnswerRepository;

    // GET: Hiển thị form câu hỏi
    @GetMapping("/questionnaire")
    public String showSurveyForm(Model model) {
        List<Question> questions = questionRepository.findAll();
        model.addAttribute("questions", questions);
        return "question";  // Thymeleaf template
    }

    // POST: Xử lý câu trả lời
    @PostMapping("/questionnaire")
    @Transactional
    public String handleSurveySubmission(@RequestParam Map<String, String> formData,
                                         HttpSession session,
                                         Model model) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            System.out.println("Không có user trong session. Chuyển hướng login.");
            return "redirect:/login";
        }

      //  System.out.println("✅ Đã login với user: " + user.getUsername());

        userAnswerRepository.deleteByUser(user);

        List<Question> questions = questionRepository.findAll();
        questions.sort(Comparator.comparing(Question::getId));

        List<UserAnswer> userAnswers = new ArrayList<>();
        List<String> selectedOptionTexts = new ArrayList<>();

        for (Question question : questions) {
            String optionIdStr = formData.get("answer" + question.getId());
            if (optionIdStr != null) {
                Long optionId = Long.parseLong(optionIdStr);
                Option option = questionOptionRepository.findById(optionId).orElse(null);
                if (option != null) {
                    UserAnswer answer = new UserAnswer(user, question, option);
                    userAnswers.add(answer);
                    selectedOptionTexts.add(option.getOptionText());
                }
            }
        }

        userAnswerRepository.saveAll(userAnswers);

        // Phân tích kết quả
        AnalysisResult result = analyze(selectedOptionTexts);

        analysisResultRepository.deleteByUser(user);
        AnalysisResultEntity resultEntity = new AnalysisResultEntity(user, result.analysis, result.recommendation);
        analysisResultRepository.save(resultEntity);

     /*Note*/   model.addAttribute("user", user);

        model.addAttribute("questions", questions);
        model.addAttribute("answers", selectedOptionTexts);
        model.addAttribute("analysisResult", result.analysis);
        model.addAttribute("recommendation", result.recommendation);

     //   System.out.println("🎯 Trả lời thành công. Chuyển đến trang result.");

        return "result"; // Trả về result.html
    }

    public static class AnalysisResult {
        public final String analysis;
        public final String recommendation;

        public AnalysisResult(String analysis, String recommendation) {
            this.analysis = analysis;
            this.recommendation = recommendation;
        }
    }

    private static AnalysisResult analyze(List<String> answers) {
        int score = 0;
        if (answers.size() < 8) return new AnalysisResult("Invalid", "Not enough data to analyze.");

        String dailyCigarettes = answers.get(1);
        switch (dailyCigarettes) {
            case "1-5" -> score += 1;
            case "6-10" -> score += 2;
            case "11-20" -> score += 3;
            case "More than 20" -> score += 4;
        }

        String startAge = answers.get(2);
        switch (startAge) {
            case "Under 16" -> score += 2;
            case "16-18" -> score += 1;
        }

        String moneySpent = answers.get(3);
        switch (moneySpent) {
            case "50,000 - 150,000 VND" -> score += 1;
            case "150,000 - 300,000 VND" -> score += 2;
            case "More than 300,000 VND" -> score += 3;
        }

        String quitAttempts = answers.get(4);
        switch (quitAttempts) {
            case "Yes, multiple times" -> score += 2;
            case "Yes, once" -> score += 1;
        }

        String stressSmoking = answers.get(5);
        switch (stressSmoking) {
            case "Yes, significantly more" -> score += 2;
            case "Yes, slightly more" -> score += 1;
        }

        if ("Primarily indoors".equals(answers.get(6))) score += 1;
        if ("Yes".equals(answers.get(7))) score += 1;

        if (score >= 10) {
            return new AnalysisResult("Very High Dependence Level", "We strongly recommend seeking professional support as soon as possible.");
        } else if (score >= 6) {
            return new AnalysisResult("Moderate Dependence Level", "You show signs of tobacco dependence. Consider finding support early.");
        } else {
            return new AnalysisResult("Low Dependence Level", "Your dependence appears low. This is a great opportunity to quit completely.");
        }
    }

    @PostMapping("/addQuestion")
    public String addQuestion(@RequestParam String questionText,
                              @RequestParam List<String> optionTexts) {

        Question question = new Question();
        question.setQuestionText(questionText);

        for (String text : optionTexts) {
            if (text != null && !text.trim().isEmpty()) {
                Option option = new Option();
                option.setOptionText(text);
                option.setQuestion(question);
                question.getOptions().add(option);
            }
        }

        questionRepository.save(question);
        return "redirect:/admin/questions";
    }
}
