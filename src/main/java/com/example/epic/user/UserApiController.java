package com.example.epic.user;

import jakarta.validation.Valid;

import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.support.SessionStatus;

import javax.naming.Binding;
import java.util.Optional;

// TODO : html에 직접 접근하는 방식에서 RESTapi 연동 방식으로 바꾸기

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class UserApiController {
    @Autowired
    private final UserService userService;
    @Autowired
    private final UserRepository userRepository;

    // 로그인
    /*@PostMapping("/auth/login")
    public String login(@RequestBody @Valid UserLoginDto userLoginDto, BindingResult bindingResult) {
        return "";
    }*/

    // 회원가입
    @PostMapping("/auth/register")
    public ResponseEntity<SiteUser> signup(@RequestBody @Valid UserCreateDto userCreateDto, BindingResult bindingResult, SessionStatus sessionStatus) {
        /*if (bindingResult.hasErrors()) {
            return "signup_form";
        }*/
        // 비밀번호와 비밀번호 확인에 입력한 값이 다를 경우
        if (!userCreateDto.getPassword1().equals(userCreateDto.getPassword2())) {
            bindingResult.rejectValue("password2", "passwordInCorrect",
                    "비밀번호가 일치하지 않습니다.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        if(userService.findSiteUser(userCreateDto.getEmail(), userCreateDto.getPassword2()) != null) {
            bindingResult.reject("userExist", "이미 존재하는 사용자입니다.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        // 회원가입 과정
        SiteUser created = userService.create(userCreateDto);

        return ResponseEntity.status(HttpStatus.OK).body(created);
    }

    // 로그아웃
    /*@PostMapping("/auth/logout")
    public String logout(@AuthenticationPrincipal UserDetails userDetails) {
        return "";
    }*/

    // 대쉬보드
    /*@GetMapping("/dashboard")
    public ResponseEntity<Optional<SiteUser>> dashboard(@AuthenticationPrincipal UserDetails userDetails) {
        Optional<SiteUser> user = userService.getByUsername(userDetails.getUsername());

        // TODO: user 정보 기반으로 단어장 db랑 학습 통계 db에 접근

        // TODO: 받아온 정보들을 json 파일 형식으로 넘겨주기
        return ResponseEntity.ok(user);
    }
*/
    // TODO: /user/profile 구현하기
    /*@GetMapping("/{id}")
    public String userPage(@AuthenticationPrincipal UserDetails userDetails) {
        return "user_page";
    };*/
}