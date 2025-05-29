package com.example.epic.Question;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class QuestionService {

    @Value("${dev.mode}")
    private String devMode;

    private final QuestionPart1Repository questionPart1Repository;
    private final QuestionPart2Repository questionPart2Repository;
    private final QuestionPart3Repository questionPart3Repository;
    private final QuestionPart4Repository questionPart4Repository;
    private final QuestionPart5Repository questionPart5Repository;

    public QuestionPart1 getRandomPart1Question() {
        if ("DEVMODE".equals(devMode)) {
            return questionPart1Repository.getDevQuestion(); // 개발용 메서드
        }
        return questionPart1Repository.getRandomQuestion();
    }

    public QuestionPart2 getRandomPart2Question() {
        if ("DEVMODE".equals(devMode)) {
            return questionPart2Repository.getDevQuestion();
        }
        return questionPart2Repository.getRandomQuestion();
    }

    public QuestionPart3 getRandomPart3Question() {
        if ("DEVMODE".equals(devMode)) {
            return questionPart3Repository.getDevQuestion();
        }
        return questionPart3Repository.getRandomQuestion();
    }

    public QuestionPart4 getRandomPart4Question() {
        if ("DEVMODE".equals(devMode)) {
            return questionPart4Repository.getDevQuestion();
        }
        return questionPart4Repository.getRandomQuestion();
    }

    public QuestionPart5 getRandomPart5Question() {
        if ("DEVMODE".equals(devMode)) {
            return questionPart5Repository.getDevQuestion();
        }
        return questionPart5Repository.getRandomQuestion();
    }
}
