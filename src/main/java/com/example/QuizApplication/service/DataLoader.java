package com.example.QuizApplication.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.QuizApplication.model.Question;
import com.example.QuizApplication.model.User;
import com.example.QuizApplication.repository.QuestionRepository;
import com.example.QuizApplication.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("DataLoader starting...");
        
        try {
            // Create default admin user if it doesn't exist
            User existingAdmin = userRepository.findByUsername("admin");
            if (existingAdmin == null) {
                System.out.println("Creating default admin user...");
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword("admin123");
                admin.setRole("ADMIN");
                userRepository.save(admin);
                System.out.println("Default admin user created - Username: admin, Password: admin123");
            } else {
                System.out.println("Admin user already exists");
            }
            
            long questionCount = questionRepository.count();
            System.out.println("Current question count in database: " + questionCount);

            // Only seed if the collection is empty
            if (questionCount == 0) {
                System.out.println("Database is empty, inserting sample questions...");

                // Explicitly declare as List<Question>
                List<Question> questions = Arrays.asList(
                        new Question("What is the capital of France?",
                                Arrays.asList("Paris", "London", "Berlin", "Madrid"),
                                0,
                                "general"),

                        new Question("Which language runs on the JVM?",
                                Arrays.asList("Python", "Java", "C++", "Ruby"),
                                1,
                                "programming"),

                        new Question("2 + 2 * 2 = ?",
                                Arrays.asList("6", "8", "4", "2"),
                                0,
                                "math"),

                        new Question("Which planet is known as the Red Planet?",
                                Arrays.asList("Earth", "Mars", "Jupiter", "Venus"),
                                1,
                                "general"),

                        new Question("Who wrote 'Hamlet'?",
                                Arrays.asList("Shakespeare", "Tolstoy", "Hemingway", "Dickens"),
                                0,
                                "literature")
                );

                questionRepository.saveAll(questions); // saveAll expects List<Question>
                System.out.println("Sample questions inserted into MongoDB!");
                
                // Verify insertion
                long newCount = questionRepository.count();
                System.out.println("Questions after insertion: " + newCount);
            } else {
                System.out.println("Questions already exist, skipping seeding.");
                System.out.println("Available questions: " + questionCount);
            }
        } catch (Exception e) {
            System.err.println("Error in DataLoader: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
