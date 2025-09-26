package com.example.QuizApplication.repository;

import com.example.QuizApplication.model.Result;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ResultRepository extends MongoRepository<Result, String> {
    List<Result> findTop10ByOrderByScoreDesc();
}
