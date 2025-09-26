package com.example.QuizApplication.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.QuizApplication.model.User;
import com.example.QuizApplication.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    // Add a method to check database status
    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        // Initialize default values
        model.addAttribute("isLoggedIn", false);
        
        try {
            long userCount = userRepository.count();
            System.out.println("Total users in database: " + userCount);
            model.addAttribute("userCount", userCount);
        } catch (Exception e) {
            System.err.println("Error checking user count: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Database connection issue");
            // Don't return early, continue to check session
        }
        
        // Check if user is logged in via session
        if (session != null) {
            try {
                String userId = (String) session.getAttribute("userId");
                if (userId != null && !userId.isBlank()) {
                    User user = userRepository.findById(userId).orElse(null);
                    if (user != null) {
                        model.addAttribute("loggedInUser", user);
                        model.addAttribute("isLoggedIn", true);
                        System.out.println("User is logged in: " + user.getUsername());
                    } else {
                        // User not found in database, clear session
                        session.removeAttribute("userId");
                        System.out.println("User not found in database, clearing session");
                    }
                }
            } catch (Exception e) {
                System.err.println("Error checking user session: " + e.getMessage());
                e.printStackTrace();
                // Clear session on error
                if (session != null) {
                    session.removeAttribute("userId");
                }
            }
        }
        
        return "index";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        Model model,
                        HttpSession session) {
        System.out.println("Login attempt for username: " + username);
        
        User user = userRepository.findByUsername(username);
        System.out.println("User found: " + (user != null ? user.getUsername() : "null"));
        
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            System.out.println("Login successful, storing in session and redirecting to categories");
            // Store user ID in session
            session.setAttribute("userId", user.getId());
            model.addAttribute("userId", user.getId());
            return "redirect:/quiz/categories?userId=" + user.getId();
        }
        
        System.out.println("Login failed - invalid credentials");
        model.addAttribute("error", "Invalid credentials");
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           Model model) {
        System.out.println("Registration attempt for username: " + username);
        
        try {
            // Check if user already exists
            User existingUser = userRepository.findByUsername(username);
            if (existingUser != null) {
                System.out.println("Username already exists: " + username);
                model.addAttribute("error", "Username already exists");
                return "register";
            }
            
            // Validate password strength (server-side)
            if (!isStrongPassword(password)) {
                model.addAttribute("error", "Password too weak. Use 8+ chars with upper, lower, number, symbol.");
                return "register";
            }

            // Create new user with bcrypt encoded password
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole("USER");
            
            System.out.println("Saving new user: " + username);
            User savedUser = userRepository.save(user);
            System.out.println("User saved with ID: " + savedUser.getId());
            
            return "redirect:/login";
        } catch (Exception e) {
            System.err.println("Error during registration: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Registration failed. Please try again.");
            return "register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        try {
            if (session != null) {
                String userId = (String) session.getAttribute("userId");
                if (userId != null) {
                    System.out.println("Logging out user: " + userId);
                }
                session.removeAttribute("userId");
                System.out.println("User logged out successfully");
            }
        } catch (Exception e) {
            System.err.println("Error during logout: " + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/";
    }

    private boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) return false;
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasDigit = password.matches(".*[0-9].*");
        boolean hasSymbol = password.matches(".*[^a-zA-Z0-9].*");
        return hasLower && hasUpper && hasDigit && hasSymbol;
    }
}
