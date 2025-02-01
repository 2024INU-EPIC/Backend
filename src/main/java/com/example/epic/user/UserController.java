package com.example.epic.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

// TODO : html에 직접 접근하는 방식에서 RESTapi 연동 방식으로 바꾸기

@RequiredArgsConstructor
@Controller
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    @GetMapping("/signup")
    public String signup(UserCreateForm userCreateForm) {
        return "signup_form";
    }

    @PostMapping("/signup")
    public String signup(@Valid UserCreateForm userCreateForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "signup_form";
        }

        if (!userCreateForm.getPassword1().equals(userCreateForm.getPassword2())) {
            bindingResult.rejectValue("password2", "passwordInCorrect",
                    "The two passwords do not match.");
            return "signup_form";
        }

        userService.create(userCreateForm.getUsername(),
                userCreateForm.getEmail(), userCreateForm.getPassword1());

        return "redirect:/user/main";
    }
    @GetMapping("/login")
    public String login(){
        return "login_form";
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Optional<SiteUser>> dashboard(@AuthenticationPrincipal UserDetails userDetails) {
        Optional<SiteUser> user = userService.getByUsername(userDetails.getUsername());

        // TODO: user 정보 기반으로 단어장 db랑 학습 통계 db에 접근

        // TODO: 받아온 정보들을 json 파일 형식으로 넘겨주기
        return ResponseEntity.ok(user);
    }

    // TODO: /user/profile 구현하기
    @GetMapping("/user/mypage")
    public String userPage(@AuthenticationPrincipal UserDetails userDetails) {
        return "user_page";
    };
}