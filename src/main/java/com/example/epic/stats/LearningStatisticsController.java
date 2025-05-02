package com.example.epic.stats;

import com.example.epic.security.JwtTokenUtil;
import com.example.epic.user.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
public class LearningStatisticsController {
    private final LearningStatisticsService learningStatisticsService;
    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;

    // 학습 통계 조회
    @GetMapping("/api/stats/learning/{id}")
    public ResponseEntity<?> getLearningStatistics(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) {
        // token 이 담긴 authorizationHeader
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        // token 추출
        String _token = authorizationHeader.replace("Bearer ", "");
        String newAccessToken = null;
        // 토큰 유효 확인
        if(jwtTokenUtil.isExpired(_token)) { // 액세스 토큰 만료
            newAccessToken = userService.handleExpiredToken(request, response);
            if(newAccessToken == null) { // 리프레쉬 토큰 만료
                // 응답 헤더의 액세스 토큰도 파기
                response.setHeader("Authorization", "");
                String message = "액세스 토큰/리프레쉬 토큰 만료";
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
            } else { // 리프레쉬 토큰이 살아있으므로 새로운 토큰 발급
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
        // 유저의 학습 통계 불러오기
        LearningStatistics statistics = learningStatisticsService.getStatistics(id, newAccessToken);
        if(statistics == null) {
            log.info("해당 유저의 학습 통계가 존재하지 않거나 잘못된 요청");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } else {
            Map<String,Object> body = new HashMap<>();
            body.put("username", statistics.getUser().getUsername());
            body.put("part1", statistics.getStatisticsPart1());
            body.put("part2", statistics.getStatisticsPart2());
            body.put("part3", statistics.getStatisticsPart3());
            body.put("part4", statistics.getStatisticsPart4());
            body.put("part5", statistics.getStatisticsPart5());
            body.put("lastGrade", statistics.getLastGrade());
            body.put("lastTestedAt", statistics.getLastTestedAt());
            return ResponseEntity.ok(body);
        }
    }
}
