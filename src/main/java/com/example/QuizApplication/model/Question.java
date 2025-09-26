package com.example.QuizApplication.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "questions")
public class Question {

    @Id
    private String id;
    private String question;
    private List<String> options;   // Multiple choice options
    private int correctAnswer;      // Index of the correct option
    private String category = "general"; // default category

    // Optional: custom constructor without id
    public Question(String question, List<String> options, int correctAnswer, String category) {
        this.question = question;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.category = category != null ? category : "general";
    }
}
