package com.example.epic;

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
}

