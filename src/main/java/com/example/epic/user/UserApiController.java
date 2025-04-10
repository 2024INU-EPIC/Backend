package com.example.epic.user;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

// TODO : html에 직접 접근하는 방식에서 RESTapi 연동 방식으로 바꾸기

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class UserApiController {
    @Autowired
    private final UserService userService;
    @Value("${jwt.secret}")
    private String secretkey;


    // 로그인
    @PostMapping("/auth/login")
    public String login(@RequestBody @Valid UserLoginDto userLoginDto, HttpSession session) {

        SiteUser _siteuser = userService.login(userLoginDto);

        // 로그인 아이디나 비밀번호가 틀린 경우 global error return
        if(_siteuser == null) {
            return "로그인 아이디 혹은 비밀번호가 틀립니다.";
        }

        // 로그인 성공 => Jwt Token 발급

        long expireTimeMs = 1000 * 60 * 60; // Token 유효 시간 = 60분

        String jwtToken = JwtTokenUtil.createToken(_siteuser.getEmail(), secretkey, expireTimeMs);
        log.info("로그인 성공 현 사용자의 로그인 토큰 : {}", jwtToken);

        return jwtToken;
    }

    @GetMapping("/info")
    public String userInfo(Authentication auth) {
        SiteUser loginUser = userService.getLoginUserByEmail(auth.getName());

        return String.format("Email: %s\nUsername: %s", loginUser.getEmail(), loginUser.getUsername());
    }

    // 회원가입
    @PostMapping("/auth/register")
    public ResponseEntity<?> signup(@RequestBody @Valid UserCreateDto userCreateDto) {
        // 비밀번호와 비밀번호 확인에 입력한 값이 다른 경우 검증
        if (!userCreateDto.getPassword1().equals(userCreateDto.getPassword2())) {
            // 입력 검증이라서 로그 여기에 찍음
            log.info("두 비밀번호 값 불일치");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("두 비밀번호 값 불일치");
        }
        // db에 해당 이메일을 사용하는 유저가 존재하는 경우 검증
        if(userService.findSiteUser(userCreateDto.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("해당 이메일의 유저가 이미 존재");
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