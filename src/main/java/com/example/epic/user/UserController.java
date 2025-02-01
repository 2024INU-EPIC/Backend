package com.example.epic.user;

import ch.qos.logback.core.model.Model;
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
import org.springframework.web.bind.annotation.RequestBody;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
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
    public ResponseEntity<?> dashboard(@AuthenticationPrincipal UserDetails userDetails) {
        SiteUser user = userService.getByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        //UserStatistics statistics = statisticsService.getStatisticsForUser(user);
        //List<ExamRecord> examRecords = examRecordService.getExamRecordsForUser(user);

        // TODO: user 정보 기반으로 단어장 db랑 학습 통계 db에 접근
        Map<String, Object> response = new HashMap<>();
        response.put("user", user);
        //response.put("statistics", statistics);
        //response.put("examRecords", examRecords);

        // TODO: 받아온 정보들을 json 파일 형식으로 넘겨주기
        return ResponseEntity.ok(response);
    }

    // TODO: /user/mypage 구현하기
    @GetMapping("/mypage")
    public ResponseEntity<?> getMypageData(@AuthenticationPrincipal UserDetails userDetails) {
        SiteUser user = userService.getByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        //UserStatistics statistics = statisticsService.getStatisticsForUser(user);
        //List<ExamRecord> examRecords = examRecordService.getExamRecordsForUser(user);

        Map<String, Object> response = new HashMap<>();
        response.put("user", user);
        //response.put("statistics", statistics);
        //response.put("examRecords", examRecords);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/mypage/update-password")
    public ResponseEntity<?> updatePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PasswordForm passwordForm,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body("Validation error in submitted data.");
        }

        SiteUser user = userService.getByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify current password.
        if (!userService.checkPassword(user, passwordForm.getCurrentPassword())) {
            return ResponseEntity.badRequest().body("Current password is incorrect.");
        }
        // Verify new password confirmation.
        if (!passwordForm.getNewPassword().equals(passwordForm.getConfirmNewPassword())) {
            return ResponseEntity.badRequest().body("New passwords do not match.");
        }

        // Update password.
        userService.updatePassword(user, passwordForm.getNewPassword());
        return ResponseEntity.ok("Password updated successfully.");
    }

    @PostMapping("/mypage/delete")
    public ResponseEntity<?> deleteAccount(@AuthenticationPrincipal UserDetails userDetails) {
        SiteUser user = userService.getByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        userService.deleteUser(user);
        return ResponseEntity.ok("Account deleted successfully.");
    }
}