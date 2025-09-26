package com.example.QuizApplication.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.QuizApplication.model.Question;
import com.example.QuizApplication.model.Result;
import com.example.QuizApplication.repository.QuestionRepository;
import com.example.QuizApplication.repository.ResultRepository;
import com.example.QuizApplication.repository.UserRepository;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuestionRepository questionRepository;
    private final ResultRepository resultRepository;
    private final UserRepository userRepository;

    @GetMapping("/categories")
    public String showCategories(@RequestParam(required = false) String userId, Model model, HttpSession session) {
        // Try to get userId from session if not provided in URL
        if (userId == null || userId.isBlank()) {
            userId = (String) session.getAttribute("userId");
        }
        
        if (userId == null || userId.isBlank()) {
            // If userId is missing from both URL and session, redirect to login
            return "redirect:/login";
        }
        System.out.println("Categories page requested for userId: " + userId);
        
        try {
            // Get all unique categories from the database
            List<Question> allQuestions = questionRepository.findAll();
            System.out.println("Total questions found: " + allQuestions.size());
            
            List<String> categories = allQuestions.stream()
                    .map(Question::getCategory)
                    .distinct()
                    .collect(java.util.stream.Collectors.toList());
            
            System.out.println("Categories found: " + categories);
            
            model.addAttribute("categories", categories);
            model.addAttribute("userId", userId);
            return "categories";
        } catch (Exception e) {
            System.err.println("Error in showCategories: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error loading categories");
            return "categories";
        }
    }

    @GetMapping("/{category}")
    public String getQuiz(@PathVariable String category,
                          @RequestParam(required = false) String userId,
                          Model model,
                          HttpSession session) {
        
        // Try to get userId from session if not provided in URL
        if (userId == null || userId.isBlank()) {
            userId = (String) session.getAttribute("userId");
        }
        
        if (userId == null || userId.isBlank()) {
            return "redirect:/login";
        }

        System.out.println("Quiz request for category: " + category + ", userId: " + userId);

        try {
            List<Question> questions = questionRepository.findByCategory(category);

            // Check if the list is empty
            System.out.println("Questions fetched: " + questions.size());
            
            if (questions.isEmpty()) {
                System.out.println("No questions found for category: " + category);
                model.addAttribute("error", "No questions available for this category");
                return "quiz";
            }

            model.addAttribute("questions", questions); // Must match Thymeleaf
            model.addAttribute("userId", userId);
            model.addAttribute("category", category);

            System.out.println("Quiz page loaded successfully with " + questions.size() + " questions");
            return "quiz";
        } catch (Exception e) {
            System.err.println("Error loading quiz: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error loading quiz");
            return "quiz";
        }
    }

    // Handle quiz submission
    @PostMapping("/submit")
    public String submitQuiz(@RequestParam Map<String, String> allParams,
                             @RequestParam(required = false) String userId,
                             @RequestParam String category,
                             Model model,
                             HttpSession session) {
        
        // Try to get userId from session if not provided in URL
        if (userId == null || userId.isBlank()) {
            userId = (String) session.getAttribute("userId");
        }
        
        if (userId == null || userId.isBlank()) {
            return "redirect:/login";
        }
        
        System.out.println("Quiz submission received:");
        System.out.println("UserId: " + userId);
        System.out.println("Category: " + category);
        System.out.println("All parameters: " + allParams);

        try {
            // Extract answers from parameters in correct order
            List<Integer> answers = new ArrayList<>();
            System.out.println("Processing parameters:");
            for (String key : allParams.keySet()) {
                System.out.println("Key: " + key + ", Value: " + allParams.get(key));
            }
            
            // Get questions first to know how many answers to expect
            List<Question> questions = questionRepository.findByCategory(category);
            System.out.println("Questions found for category " + category + ": " + questions.size());
            
            // Extract answers in order
            for (int i = 0; i < questions.size(); i++) {
                String answerKey = "answers[" + i + "]";
                if (allParams.containsKey(answerKey)) {
                    try {
                        int answer = Integer.parseInt(allParams.get(answerKey));
                        answers.add(answer);
                        System.out.println("Question " + i + " answer: " + answer);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid answer format for question " + i + ": " + allParams.get(answerKey));
                        answers.add(-1); // Invalid answer
                    }
                } else {
                    System.out.println("No answer provided for question " + i);
                    answers.add(-1); // No answer provided
                }
            }
            
            System.out.println("Final extracted answers: " + answers);
            System.out.println("Number of answers extracted: " + answers.size());
            
            // Debug: Print correct answers
            for (int i = 0; i < questions.size(); i++) {
                System.out.println("Question " + i + " correct answer: " + questions.get(i).getCorrectAnswer());
            }

            int score = 0;
            if (!answers.isEmpty()) {
                score = calculateScore(questions, answers);
            }
            System.out.println("Calculated score: " + score);

            // Save result
            Result result = new Result();
            result.setUserId(userId);
            result.setScore(score);
            result.setDate(LocalDateTime.now());
            resultRepository.save(result);
            System.out.println("Result saved successfully");

            model.addAttribute("score", score);
            model.addAttribute("totalQuestions", questions.size());
            model.addAttribute("userId", userId);
            return "result"; // result.html template
        } catch (Exception e) {
            System.err.println("Error in quiz submission: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error processing quiz submission");
            return "result";
        }
    }

    // Display leaderboard
    @GetMapping("/leaderboard")
    public String leaderboard(Model model, HttpSession session) {
        // Check if user is logged in
        String userId = (String) session.getAttribute("userId");
        if (userId == null || userId.isBlank()) {
            return "redirect:/login";
        }
        // Fetch all results and collapse to one per userId with the highest score
        List<Result> allResults = resultRepository.findAll();

        java.util.Map<String, Result> bestResultPerUser = new java.util.HashMap<>();
        for (Result r : allResults) {
            if (r.getUserId() == null) {
                continue;
            }
            Result currentBest = bestResultPerUser.get(r.getUserId());
            if (currentBest == null) {
                bestResultPerUser.put(r.getUserId(), r);
            } else {
                boolean hasHigherScore = r.getScore() > currentBest.getScore();
                boolean sameScoreNewer = r.getScore() == currentBest.getScore()
                        && r.getDate() != null
                        && (currentBest.getDate() == null || r.getDate().isAfter(currentBest.getDate()));
                if (hasHigherScore || sameScoreNewer) {
                    bestResultPerUser.put(r.getUserId(), r);
                }
            }
        }

        List<Result> results = new java.util.ArrayList<>(bestResultPerUser.values());
        results.sort(java.util.Comparator
                .comparingInt(Result::getScore).reversed()
                .thenComparing(Result::getDate, java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder())));

        if (results.size() > 10) {
            results = results.subList(0, 10);
        }

        model.addAttribute("results", results);

        // Build userId -> username map for display
        java.util.Map<String, String> userIdToUsername = new java.util.HashMap<>();
        for (Result r : results) {
            if (r.getUserId() != null && !userIdToUsername.containsKey(r.getUserId())) {
                com.example.QuizApplication.model.User user = userRepository.findById(r.getUserId()).orElse(null);
                if (user != null && user.getUsername() != null) {
                    userIdToUsername.put(r.getUserId(), user.getUsername());
                }
            }
        }
        model.addAttribute("usernames", userIdToUsername);

        return "leaderboard"; // leaderboard.html template
    }

    // Scoring logic
    private int calculateScore(List<Question> questions, List<Integer> answers) {
        int score = 0;
        System.out.println("Calculating score:");
        System.out.println("Questions count: " + questions.size());
        System.out.println("Answers count: " + answers.size());
        
        for (int i = 0; i < questions.size() && i < answers.size(); i++) {
            int correctAnswer = questions.get(i).getCorrectAnswer();
            int userAnswer = answers.get(i);
            boolean isCorrect = correctAnswer == userAnswer;
            
            System.out.println("Question " + i + ": Correct=" + correctAnswer + ", User=" + userAnswer + ", Match=" + isCorrect);
            
            if (isCorrect) {
                score++;
            }
        }
        
        System.out.println("Final score: " + score);
        return score;
    }

}
