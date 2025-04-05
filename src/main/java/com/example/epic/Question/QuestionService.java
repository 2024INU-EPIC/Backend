package com.example.epic.Question;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionPart1Repository questionPart1Repository;
    private final QuestionPart2Repository questionPart2Repository;
    private final QuestionPart3Repository questionPart3Repository;
    private final QuestionPart4Repository questionPart4Repository;
    private final QuestionPart5Repository questionPart5Repository;

    public QuestionPart1 getRandomPart1Question() {
        return questionPart1Repository.getRandomQuestion();
    }
    public QuestionPart2 getRandomPart2Question() {
        return questionPart2Repository.getRandomQuestion();
    }
    public QuestionPart3 getRandomPart3Question() {
        return questionPart3Repository.getRandomQuestion();
    }
    public QuestionPart4 getRandomPart4Question() {
        return questionPart4Repository.getRandomQuestion();
    }
    public QuestionPart5 getRandomPart5Question() {
        return questionPart5Repository.getRandomQuestion();
    }
}
