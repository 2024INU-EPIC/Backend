package com.example.epic.stats;

import com.example.epic.user.JwtTokenUtil;
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
    @Autowired
    private final LearningStatisticsService learningStatisticsService;
    @Autowired
    private UserService userService;
    @Value("${jwt.secret}")
    private String secretkey;

    // 학습 통계 조회
    @GetMapping("/api/stats/learning/{id}")
    public ResponseEntity<?> getLearningStatistics(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) {
        // token이 담긴 authorizationHeader
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        // token 추출
        String _token = authorizationHeader.replace("Bearer ", "");
        // 토큰 유효 확인
        if(JwtTokenUtil.isExpired(_token, secretkey)) {
            return userService.handleExpiredToken(_token, response);
        }
        // 유저의 학습 통계 불러오기
        LearningStatistics statistics = learningStatisticsService.getStatistics(id, _token);
        if(statistics == null) {
            log.info("해당 유저의 학습 통계가 존재하지 않거나 잘못된 요청");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        Map<String,Object> body = new HashMap<>();
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
