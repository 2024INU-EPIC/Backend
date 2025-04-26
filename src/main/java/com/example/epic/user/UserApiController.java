package com.example.epic.user;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

// TODO : html에 직접 접근하는 방식에서 RESTapi 연동 방식으로 바꾸기

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class UserApiController {
    @Autowired
    private UserService userService;
    @Value("${jwt.secret}")
    private String secretkey;

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

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // 로그인
    @PostMapping("/auth/login")
    public ResponseEntity<String> login(@RequestBody @Valid UserLoginDto userLoginDto, BindingResult bindingResult, HttpServletResponse response) {

        SiteUser _siteuser = userService.login(userLoginDto);

        // 로그인 아이디나 비밀번호가 틀린 경우 global error return
        if(_siteuser == null) {
            bindingResult.reject("loginFail,", "로그인 아이디 또는 비밀번호가 틀립니다.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("로그인 아이디 혹은 비밀번호가 틀립니다.");
        }

        // 로그인 성공 => Jwt Token 발급
        long expireTimeMs = 1000 * 60 * 60; // Token 유효 시간 = 60분

        String jwtToken = JwtTokenUtil.createToken(_siteuser.getEmail(), secretkey, expireTimeMs);
        // log.info("로그인 성공 현 사용자의 로그인 토큰 : {}", jwtToken);

        // 발급한 Jwt Token을 Cookie를 통해 전송
        // 클라이언트는 다음 요청부터 Jwt Token이 담긴 쿠키 전송 => 이 값을 통해 인증, 인가 진행
        Cookie cookie = new Cookie("jwtToken", jwtToken);
        cookie.setMaxAge(60 * 60);  // 쿠키 유효 시간 : 1시간
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // 개발 중이라면 false로 테스트 가능
        cookie.setPath("/");
        response.addCookie(cookie);

        response.addHeader("Authorization", "Bearer " + jwtToken);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // 로그아웃
    @PostMapping("/auth/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        // 쿠키 파기
        Cookie cookie = new Cookie("jwtToken", null);
        log.info(String.valueOf(cookie));
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);
        // 헤더에 토큰 파기
        response.setHeader("Authorization", "");

        return ResponseEntity.status(HttpStatus.OK).body("jwt 토큰 " + request.getHeader("Cookie").substring(9) + " 는 더이상 유효하지 않음");
    }

    // 회원정보 수정
    @PatchMapping("/user/{id}")
    public ResponseEntity<String> updateUser(@RequestBody PwdUpdateDto pwdUpdateDto, @PathVariable long id, HttpServletRequest request, HttpServletResponse response) {
        String oldPassword = pwdUpdateDto.getOldPassword(); // 이전 비밀번호
        String newPassword = pwdUpdateDto.getNewPassword(); // 새로운 비밀번호
        // token이 담긴 authorizationHeader
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        // token 추출
        String _token = authorizationHeader.replace("Bearer ", "");
        // 토큰 유효 확인
        if(JwtTokenUtil.isExpired(_token, secretkey)) {
            return userService.handleExpiredToken(_token, response);
        }
        // 비밀번호 업데이트
        SiteUser updated = userService.updateUser(id, _token, oldPassword, newPassword);
        
        if(updated == null) {
            log.info("비밀번호 수정 실패");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        else {
            log.info("비밀번호 수정 성공");
            return ResponseEntity.status(HttpStatus.OK).build();
        }
    }

    // 회원탈퇴
    @DeleteMapping("/user/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable long id, HttpServletRequest request, HttpServletResponse response) {
        // token이 담긴 authorizationHeader
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        // token 추출
        String _token = authorizationHeader.replace("Bearer ", "");
        // 토큰 유효 확인
        if(JwtTokenUtil.isExpired(_token, secretkey)) {g
            return userService.handleExpiredToken(_token, response);
        }
        // 삭제하고자 하는 유저 조회 및 검사
        SiteUser deleted = userService.deleteUser(id, _token);
        if(deleted == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("탈퇴하고자 하는 유저가 존재하지 않거나 잘못된 요청");
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getMain(@PathVariable long id, HttpServletRequest request, HttpServletResponse response) {
        // token이 담긴 authorizationHeader
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        // token 추출
        String _token = authorizationHeader.replace("Bearer ", "");
        // 토큰 유효 확인
        if(JwtTokenUtil.isExpired(_token, secretkey)) {
            return userService.handleExpiredToken(_token, response);
        }
        return null;
    }
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