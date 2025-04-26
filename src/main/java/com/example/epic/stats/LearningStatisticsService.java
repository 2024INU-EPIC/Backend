package com.example.epic.stats;

import com.example.epic.user.JwtTokenUtil;
import com.example.epic.user.SiteUser;
import com.example.epic.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class LearningStatisticsService {
    @Autowired
    private final LearningStatisticsRepository learningStatisticsRepository;
    @Value("${jwt.secret}")
    private String secretkey;
    @Autowired
    private UserRepository userRepository;

    public LearningStatistics getStatistics(Long id, String _token) {
        // id 유저 조회
        SiteUser user1 = userRepository.findById(id).orElse(null);
        if(user1 == null) {
            log.info("id : {} 의 유저 존재하지 않음");
            return null;
        }
        // token 주인 유저 조회
        // token payload email
        String tokenEmail = JwtTokenUtil.getLoginId(_token, secretkey);
        SiteUser user2 = userRepository.findByEmail(tokenEmail).orElse(null);
        if(user2 == null) {
            log.info("email : {} 의 유저 존재하지 않음", tokenEmail);
            return null;
        }
        // id 조회 유저와 token 조회 유저 동일성 검사
        if(user1 != user2) {
            log.info("id와 token의 유저가 일치하지 않음");
            return null;
        }

        // id 유저 존재. token 유저 존재. 두 유저 같음. 해당 유저의 학습 통계 조회
        return learningStatisticsRepository.findById(id).orElse(null);
    }
}
