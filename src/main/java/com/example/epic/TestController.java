package com.example.epic;

import com.example.epic.Question.QuestionPart1;
import com.example.epic.Question.QuestionPart1Repository;
import com.example.epic.User.EpicUser;
import com.example.epic.User.EpicUserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    private final EpicUserRepository userRepository;

    public TestController(EpicUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/user")
    public ResponseEntity<EpicUser> getUser() {
        return userRepository.findByUserEmail("kim@example.com")
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 내부 컨트롤러를 static으로 선언하여 별도의 빈으로 인식되도록 함
    @RestController
    @RequestMapping("/test/question")
    public static class QuestionController {

        private final QuestionPart1Repository questionPart1Repository;

        public QuestionController(QuestionPart1Repository questionPart1Repository) {
            this.questionPart1Repository = questionPart1Repository;
        }

        @GetMapping("/part1")
        public ResponseEntity<QuestionPart1> getPart1() {
            return questionPart1Repository.findById(1L)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }
    }
}