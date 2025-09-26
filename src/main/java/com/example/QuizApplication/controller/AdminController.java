package com.example.QuizApplication.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.QuizApplication.model.Question;
import com.example.QuizApplication.model.User;
import com.example.QuizApplication.repository.QuestionRepository;
import com.example.QuizApplication.repository.UserRepository;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private QuestionRepository questionRepository;
    
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/login")
    public String adminLoginPage() {
        return "admin/login";
    }

    @PostMapping("/login")
    public String adminLogin(@RequestParam String username,
                            @RequestParam String password,
                            Model model) {
        System.out.println("Admin login attempt for username: " + username);
        
        User user = userRepository.findByUsername(username);
        
        if (user != null && user.getPassword().equals(password) && "ADMIN".equals(user.getRole())) {
            System.out.println("Admin login successful");
            model.addAttribute("adminId", user.getId());
            return "redirect:/admin/dashboard?adminId=" + user.getId();
        }
        
        System.out.println("Admin login failed - invalid credentials or not admin");
        model.addAttribute("error", "Invalid admin credentials");
        return "admin/login";
    }

    @GetMapping("/dashboard")
    public String adminDashboard(@RequestParam String adminId, Model model) {
        // Verify admin access
        User admin = userRepository.findById(adminId).orElse(null);
        if (admin == null || !"ADMIN".equals(admin.getRole())) {
            return "redirect:/admin/login";
        }
        
        try {
            List<Question> allQuestions = questionRepository.findAll();
            List<String> categories = allQuestions.stream()
                    .map(Question::getCategory)
                    .distinct()
                    .collect(java.util.stream.Collectors.toList());
            
            model.addAttribute("questions", allQuestions);
            model.addAttribute("categories", categories);
            model.addAttribute("adminId", adminId);
            model.addAttribute("totalQuestions", allQuestions.size());
            
            return "admin/dashboard";
        } catch (Exception e) {
            System.err.println("Error loading admin dashboard: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error loading dashboard");
            return "admin/dashboard";
        }
    }

    @GetMapping("/add-question")
    public String addQuestionPage(@RequestParam String adminId, Model model) {
        // Verify admin access
        User admin = userRepository.findById(adminId).orElse(null);
        if (admin == null || !"ADMIN".equals(admin.getRole())) {
            return "redirect:/admin/login";
        }
        
        model.addAttribute("adminId", adminId);
        return "admin/add-question";
    }

    @PostMapping("/add-question")
    public String addQuestion(@RequestParam String adminId,
                             @RequestParam String question,
                             @RequestParam String option1,
                             @RequestParam String option2,
                             @RequestParam String option3,
                             @RequestParam String option4,
                             @RequestParam int correctAnswer,
                             @RequestParam String category,
                             Model model) {
        
        // Verify admin access
        User admin = userRepository.findById(adminId).orElse(null);
        if (admin == null || !"ADMIN".equals(admin.getRole())) {
            return "redirect:/admin/login";
        }
        
        try {
            // Validate inputs
            if (question.trim().isEmpty() || option1.trim().isEmpty() || 
                option2.trim().isEmpty() || option3.trim().isEmpty() || option4.trim().isEmpty()) {
                model.addAttribute("error", "All fields are required");
                model.addAttribute("adminId", adminId);
                return "admin/add-question";
            }
            
            if (correctAnswer < 0 || correctAnswer > 3) {
                model.addAttribute("error", "Correct answer must be between 0 and 3");
                model.addAttribute("adminId", adminId);
                return "admin/add-question";
            }
            
            // Create new question
            Question newQuestion = new Question();
            newQuestion.setQuestion(question.trim());
            newQuestion.setOptions(Arrays.asList(option1.trim(), option2.trim(), option3.trim(), option4.trim()));
            newQuestion.setCorrectAnswer(correctAnswer);
            newQuestion.setCategory(category.trim().isEmpty() ? "general" : category.trim());
            
            questionRepository.save(newQuestion);
            System.out.println("New question added: " + newQuestion.getQuestion());
            
            return "redirect:/admin/dashboard?adminId=" + adminId;
        } catch (Exception e) {
            System.err.println("Error adding question: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error adding question. Please try again.");
            model.addAttribute("adminId", adminId);
            return "admin/add-question";
        }
    }

    @GetMapping("/delete-question")
    public String deleteQuestion(@RequestParam String adminId,
                               @RequestParam String questionId) {
        
        // Verify admin access
        User admin = userRepository.findById(adminId).orElse(null);
        if (admin == null || !"ADMIN".equals(admin.getRole())) {
            return "redirect:/admin/login";
        }
        
        try {
            questionRepository.deleteById(questionId);
            System.out.println("Question deleted: " + questionId);
        } catch (Exception e) {
            System.err.println("Error deleting question: " + e.getMessage());
        }
        
        return "redirect:/admin/dashboard?adminId=" + adminId;
    }

    @GetMapping("/create-admin")
    public String createAdminPage() {
        return "admin/create-admin";
    }

    @PostMapping("/create-admin")
    public String createAdmin(@RequestParam String username,
                            @RequestParam String password,
                            Model model) {
        try {
            // Check if user already exists
            User existingUser = userRepository.findByUsername(username);
            if (existingUser != null) {
                model.addAttribute("error", "Username already exists");
                return "admin/create-admin";
            }
            
            // Create new admin user
            User admin = new User();
            admin.setUsername(username);
            admin.setPassword(password);
            admin.setRole("ADMIN");
            
            userRepository.save(admin);
            System.out.println("Admin user created: " + username);
            
            model.addAttribute("success", "Admin user created successfully");
            return "admin/create-admin";
        } catch (Exception e) {
            System.err.println("Error creating admin: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error creating admin user");
            return "admin/create-admin";
        }
    }
}
