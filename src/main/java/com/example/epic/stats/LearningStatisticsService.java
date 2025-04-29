package com.example.epic.stats;

import com.example.epic.Assessment.TestGrade;
import com.example.epic.user.JwtTokenUtil;
import com.example.epic.user.SiteUser;
import com.example.epic.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LearningStatisticsService {

    private final LearningStatisticsRepository learningStatisticsRepository;
    private final UserRepository               userRepository;

    @Value("${jwt.secret}")
    private String secretKey;

    @Transactional(readOnly = true)
    public LearningStatistics getStatistics(Long id, String token) {
        SiteUser userById = userRepository.findById(id).orElse(null);
        if (userById == null) {
            log.info("id : {} 의 유저 존재하지 않음", id);
            return null;
        }
        String email = JwtTokenUtil.getLoginId(token, secretKey);
        SiteUser userByToken = userRepository.findByEmail(email).orElse(null);
        if (userByToken == null) {
            log.info("email : {} 의 유저 존재하지 않음", email);
            return null;
        }
        // 참조 비교 대신 ID 비교
        if (!userById.getId().equals(userByToken.getId())) {
            log.info("id와 token의 유저가 일치하지 않음");
            return null;
        }
        return learningStatisticsRepository.findById(id).orElse(null);
    }

    @Transactional
    public void updateStatistics(SiteUser user, TestGrade grade) {
        LearningStatistics stats = learningStatisticsRepository.findByUser(user)
                .orElse(LearningStatistics.builder()
                        .user(user)
                        .statisticsPart1(0.0)
                        .statisticsPart2(0.0)
                        .statisticsPart3(0.0)
                        .statisticsPart4(0.0)
                        .statisticsPart5(0.0)
                        .totalTests(0)
                        .build());

        int newCount = stats.getTotalTests() + 1;
        stats.setStatisticsPart1(avg(stats.getStatisticsPart1(), grade.getPart1Grade(), newCount));
        stats.setStatisticsPart2(avg(stats.getStatisticsPart2(), grade.getPart2Grade(), newCount));
        stats.setStatisticsPart3(avg(stats.getStatisticsPart3(), grade.getPart3Grade(), newCount));
        stats.setStatisticsPart4(avg(stats.getStatisticsPart4(), grade.getPart4Grade(), newCount));
        stats.setStatisticsPart5(avg(stats.getStatisticsPart5(), grade.getPart5Grade(), newCount));

        stats.setTotalTests(newCount);
        stats.setLastGrade(grade.getTestGrade());
        stats.setLastTestedAt(java.time.LocalDateTime.now());

        learningStatisticsRepository.save(stats);
    }

    private Double avg(Double currentAvg, Float newScore, int count) {
        double prev = (currentAvg == null ? 0.0 : currentAvg);
        return (prev * (count - 1) + newScore) / count;
    }
}