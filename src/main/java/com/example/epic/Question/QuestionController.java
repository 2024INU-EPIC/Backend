package com.example.epic.Question;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/focused-learning")
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping("/part1")
    public ResponseEntity<QuestionPart1> getPart1Question() {
        QuestionPart1 question = questionService.getRandomPart1Question();
        return ResponseEntity.ok(question);
    }

    @GetMapping("/part2")
    public ResponseEntity<QuestionPart2> getPart2Question() {
        QuestionPart2 question = questionService.getRandomPart2Question();
        return ResponseEntity.ok(question);
    }

    @GetMapping("/part3")
    public ResponseEntity<QuestionPart3> getPart3Question() {
        QuestionPart3 question = questionService.getRandomPart3Question();
        return ResponseEntity.ok(question);
    }

    @GetMapping("/part4")
    public ResponseEntity<QuestionPart4> getPart4Question() {
        QuestionPart4 question = questionService.getRandomPart4Question();
        return ResponseEntity.ok(question);
    }

    @GetMapping("/part5")
    public ResponseEntity<QuestionPart5> getPart5Question() {
        QuestionPart5 question = questionService.getRandomPart5Question();
        return ResponseEntity.ok(question);
    }
}

