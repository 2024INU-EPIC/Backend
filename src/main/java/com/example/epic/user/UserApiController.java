package com.example.epic.user;

import com.example.epic.security.JwtTokenUtil;
import com.example.epic.stats.LearningStatistics;
import com.example.epic.stats.LearningStatisticsService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

// TODO : html에 직접 접근하는 방식에서 RESTapi 연동 방식으로 바꾸기

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class UserApiController {
    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;
    private final LearningStatisticsService learningStatisticsService;

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
    public ResponseEntity<?> login(@RequestBody @Valid UserLoginDto userLoginDto, BindingResult bindingResult, HttpServletResponse response) {

        SiteUser _siteuser = userService.login(userLoginDto);

        // 로그인 아이디나 비밀번호가 틀린 경우 global error return
        if(_siteuser == null) {
            bindingResult.reject("loginFail,", "로그인 아이디 또는 비밀번호가 틀립니다.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("로그인 아이디 혹은 비밀번호가 틀립니다.");
        }

        // 로그인 성공 => Jwt Token 발급
        String jwtToken = jwtTokenUtil.createAccessToken(_siteuser.getEmail());
        // log.info("로그인 성공 현 사용자의 로그인 토큰 : {}", jwtToken);

        // refresh Token 생성
        String refreshToken = jwtTokenUtil.createRefreshToken();

        // 한 명의 유저는 하나의 리프레쉬 토큰만 가질 수 있음
        // 때문에 로그인 유저가 리프레쉬 토큰을 가지고 있을 경우 새로운 토큰으로 교체
        // 리프레쉬 토큰이 없다면 새로운 토큰 저장
        jwtTokenUtil.updateRefreshToken(_siteuser, refreshToken);

        // 발급한 Jwt Token 을 Cookie 를 통해 전송
        // 클라이언트는 다음 요청부터 Jwt Token 이 담긴 쿠키 전송 => 이 값을 통해 인증, 인가 진행
        Cookie cookie1 = new Cookie("jwtToken", jwtToken);
        cookie1.setMaxAge(60 * 60);  // 쿠키 유효 시간 : 1시간
        cookie1.setHttpOnly(true);
        cookie1.setSecure(false); // 개발 중이라면 false로 테스트 가능
        cookie1.setPath("/");
        response.addCookie(cookie1);

        // refresh Token 도 쿠키 전송
        Cookie cookie2 = new Cookie("refreshToken", refreshToken);
        cookie2.setMaxAge(60 * 60);
        cookie2.setHttpOnly(true);
        cookie2.setSecure(false);
        cookie2.setPath("/");
        response.addCookie(cookie2);

        response.addHeader("Authorization", "Bearer " + jwtToken);

        Map<String, Object> maps = new HashMap<>();
        maps.put("userId", _siteuser.getId());
        System.out.println("Instant.now(): " + Instant.now());
        return ResponseEntity.status(HttpStatus.OK).body(maps);
    }

    // 로그아웃
    @PostMapping("/auth/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        // accessToken 쿠키 파기
        Cookie cookie1 = new Cookie("jwtToken", null);
        log.info(String.valueOf(cookie1));
        cookie1.setMaxAge(0);
        cookie1.setHttpOnly(true);
        cookie1.setPath("/");
        response.addCookie(cookie1);
        // refreshToken 쿠키 파기
        Cookie cookie2 = new Cookie("refreshToken", null);
        cookie2.setMaxAge(0);
        cookie2.setHttpOnly(true);
        cookie2.setPath("/");
        response.addCookie(cookie2);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(cookie1 + ", " + cookie2);
    }

    // 회원정보 수정
    @PatchMapping("/user/{id}")
    public ResponseEntity<String> updateUser(@RequestBody PwdUpdateDto pwdUpdateDto, @PathVariable long id, HttpServletRequest request, HttpServletResponse response) {
        String oldPassword = pwdUpdateDto.getOldPassword(); // 이전 비밀번호
        String newPassword = pwdUpdateDto.getNewPassword(); // 새로운 비밀번호
        // token 이 담긴 authorizationHeader
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        // token 추출
        String _token = authorizationHeader.replace("Bearer ", "");
        // 토큰 유효 확인
        String newAccessToken = null;
        if(jwtTokenUtil.isExpired(_token)) { // 액세스 토큰 만료
            newAccessToken = userService.handleExpiredToken(request, response);
            if(newAccessToken == null) { // 리프레쉬 토큰 만료
                // 응답 헤더의 액세스 토큰도 파기
                response.setHeader("Authorization", "");
                String message = "액세스 토큰/리프레쉬 토큰 만료";
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(message);
            } else { // 리프레쉬 토큰 살아있음
                // header 토큰 값 재설정
                response.addHeader("Authorization", "Bearer " + newAccessToken);
                // 쿠키 재설정
                Cookie cookie = new Cookie("jwtToken", newAccessToken);
                cookie.setMaxAge(60 * 60);
                cookie.setHttpOnly(true);
                cookie.setSecure(false);
                cookie.setPath("/");
                response.addCookie(cookie);
            }
        } else {
            newAccessToken = _token;
        }
        // 비밀번호 업데이트
        SiteUser updated = userService.updateUser(id, newAccessToken, oldPassword, newPassword);
        
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
    public ResponseEntity<String> deleteUser(@PathVariable long id, HttpServletRequest request, HttpServletResponse response) throws ExpiredJwtException {
        // token 이 담긴 authorizationHeader
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        // token 추출
        String _token = authorizationHeader.replace("Bearer ", "");
        // 탈퇴 시 전달할 메시지
        String message = "회원 탈퇴 완료";
        try {
            // 액세스 토큰 유효성 검사
            jwtTokenUtil.isExpired(_token);
            // 삭제하고자 하는 유저 학습 통계 삭제
            LearningStatistics stats = learningStatisticsService.deleteStats(id);
            if(stats == null) {
                message += " 유저의 학습 통계 존재하지 않음";
            }
            // 삭제하고자 하는 유저 검사 및 삭제
            SiteUser deleted = userService.deleteUser(id, _token);
            if(deleted == null) { // id 혹은 _token 유저 존재하지 않거나 두 유저가 일치하지 않음
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("탈퇴하고자 하는 유저가 존재하지 않거나 잘못된 요청");
            }
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(message);
        } catch(ExpiredJwtException e) {
            String newAccessToken = jwtTokenUtil.recreateAccessToken(_token);
            // 삭제하고자 하는 유저 학습 통계 삭제
            LearningStatistics stats = learningStatisticsService.deleteStats(id);
            if(stats == null) {
                message += " 유저의 학습 통계 존재하지 않음";
            }
            // 삭제하고자 하는 유저 검사 및 삭제
            SiteUser deleted = userService.deleteUser(id, newAccessToken);
            log.info("id: {} 의 유저 삭제 완료", id);
            if(deleted == null) { // id 혹은 _token 유저 존재하지 않거나 두 유저가 일치하지 않음
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("탈퇴하고자 하는 유저가 존재하지 않거나 잘못된 요청");
            } else {
                // accessToken 쿠키 파기
                Cookie cookie1 = new Cookie("jwtToken", null);
                log.info(String.valueOf(cookie1));
                cookie1.setMaxAge(0);
                cookie1.setHttpOnly(true);
                cookie1.setPath("/");
                response.addCookie(cookie1);
                // refreshToken 쿠키 파기
                Cookie cookie2 = new Cookie("refreshToken", null);
                cookie2.setMaxAge(0);
                cookie2.setHttpOnly(true);
                cookie2.setPath("/");
                response.addCookie(cookie2);

                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(message);
            }

        }
    }

    // 메인페이지(내 정보 조회)
    @GetMapping("/{id}")
    public ResponseEntity<?> getMain(@PathVariable long id, HttpServletRequest request, HttpServletResponse response) {
        // token 이 담긴 authorizationHeader
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        // token 추출
        String _token = authorizationHeader.replace("Bearer ", "");
        // 토큰 유효 확인
        String newAccessToken = null;
        if(jwtTokenUtil.isExpired(_token)) { // 액세스 토큰 만료
            newAccessToken = userService.handleExpiredToken(request, response);
            if(newAccessToken == null) { // 리프레쉬 토큰 만료
                String message = "액세스 토큰/리프레쉬 토큰 만료";
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
            } else { // 리프레쉬 토큰 살아있음
                // header 토큰 값 재설정
                response.addHeader("Authorization", "Bearer " + newAccessToken);

                // 쿠키 재설정
                Cookie cookie = new Cookie("jwtToken", newAccessToken);
                cookie.setMaxAge(60 * 60);
                cookie.setHttpOnly(true);
                cookie.setSecure(false);
                cookie.setPath("/");
                response.addCookie(cookie);

            }
        } else {
            newAccessToken = _token;
        }
        ResponseEntity<?> mainInfo = userService.getMainInfo(id, newAccessToken);
        if (mainInfo == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("모의고사 기록이 없거나 잘못된 요청");
        } else {
            return mainInfo;
        }
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
}