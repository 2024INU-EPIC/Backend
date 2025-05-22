package com.example.epic.mocktest.session;

import com.example.epic.Assessment.AssessmentMocktest;
import com.example.epic.Assessment.AssessmentMocktestRepository;
import com.example.epic.Assessment.TestGrade;
import com.example.epic.Assessment.TestGradeRepository;
import com.example.epic.mocktest.MocktestQuestion;
import com.example.epic.mocktest.dto.PartDto;
import com.example.epic.mocktest.session.MocktestSession;
import com.example.epic.mocktest.session.MocktestSessionRepository;
import com.example.epic.mocktest.dto.TestGradeDto;
import com.example.epic.mocktest.dto.TestGradeHistoryDto;
import com.example.epic.stats.LearningStatisticsService;
import com.example.epic.user.SiteUser;
import com.example.epic.user.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AssessmentMocktestService {

    private final AssessmentMocktestRepository amRepo;
    private final TestGradeRepository        tgRepo;
    private final MocktestSessionRepository  sessionRepo;
    private final LearningStatisticsService  learningStatisticsService;
    private final UserRepository userRepo;
    private final ObjectMapper               mapper = new ObjectMapper();

    public AssessmentMocktestService(
            AssessmentMocktestRepository amRepo,
            TestGradeRepository tgRepo,
            MocktestSessionRepository sessionRepo,
            LearningStatisticsService learningStatisticsService,
            UserRepository userRepo
    ) {
        this.amRepo = amRepo;
        this.tgRepo = tgRepo;
        this.sessionRepo = sessionRepo;
        this.learningStatisticsService = learningStatisticsService;
        this.userRepo = userRepo;
    }

    /**
     * 1) 세션 버퍼(List<String> evalJsons) 전체를 DB에 저장하고,
     *    저장된 AssessmentMocktest 엔티티의 ID를 반환합니다.
     */
    public Long saveAssessmentFromSession(UUID sessionId, List<String> evalJsons) {
        // 1-1. 세션에서 User, MocktestQuestion 꺼내기
        MocktestSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid session: " + sessionId));

        // 1-2. AssessmentMocktest 엔티티 생성 및 FK 설정
        AssessmentMocktest am = AssessmentMocktest.builder()
                .user(session.getUser())
                .mocktest(session.getMocktest())
                .build();

        // 1-3. q1~q11 에 JSON 문자열 채우기
        if (evalJsons.size() >= 11) {
            am.setQ1(evalJsons.get(0));
            am.setQ2(evalJsons.get(1));
            am.setQ3(evalJsons.get(2));
            am.setQ4(evalJsons.get(3));
            am.setQ5(evalJsons.get(4));
            am.setQ6(evalJsons.get(5));
            am.setQ7(evalJsons.get(6));
            am.setQ8(evalJsons.get(7));
            am.setQ9(evalJsons.get(8));
            am.setQ10(evalJsons.get(9));
            am.setQ11(evalJsons.get(10));
        }

        // 1-4. 저장 및 ID 반환
        AssessmentMocktest saved = amRepo.save(am);
        return saved.getId();
    }

    /**
     * 2) 방금 저장된 AssessmentMocktest ID로 점수를 계산하고,
     *    TestGrade 엔티티를 생성하여 저장한 뒤 DTO로 반환합니다.
     *    학습 통계도 함께 업데이트합니다.
     */
    public TestGradeDto calculateTestGrade(Long assessmentId) {
        // 2-1. 저장된 평가 결과 로드
        AssessmentMocktest am = amRepo.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("No assessment data for id: " + assessmentId));

        // 2-2. JSON 문자열→JsonNode 변환
        List<JsonNode> evals = new ArrayList<>();
        try {
            evals.add(mapper.readTree(am.getQ1()));
            evals.add(mapper.readTree(am.getQ2()));
            evals.add(mapper.readTree(am.getQ3()));
            evals.add(mapper.readTree(am.getQ4()));
            evals.add(mapper.readTree(am.getQ5()));
            evals.add(mapper.readTree(am.getQ6()));
            evals.add(mapper.readTree(am.getQ7()));
            evals.add(mapper.readTree(am.getQ8()));
            evals.add(mapper.readTree(am.getQ9()));
            evals.add(mapper.readTree(am.getQ10()));
            evals.add(mapper.readTree(am.getQ11()));
        } catch (IOException e) {
            throw new RuntimeException("JSON 파싱 오류", e);
        }


        // 2-3. 파트별 점수 계산
        double p1 = average(partScore(evals.get(0), 1), partScore(evals.get(1), 2));
        double p2 = average(partScore(evals.get(2), 3), partScore(evals.get(3), 4));
        double p3 = average(partScore(evals.get(4), 5), partScore(evals.get(5), 6), partScore(evals.get(6), 7));
        double p4 = average(partScore(evals.get(7), 8), partScore(evals.get(8), 9), partScore(evals.get(9), 10));
        double p5 = partScore(evals.get(10), 11);

        // 2-4. 최종 점수 및 등급 계산
        int finalScore = (int) (Math.round((((p1 + p2 + p3 + p4) * 3 + p5 * 5) * 2 / 17.0) / 10.0) * 10);
        String grade = computeGrade(finalScore);

        // 2-5. TestGrade 엔티티 생성 및 저장
        TestGrade tg = new TestGrade();
        tg.setAssessment(am);                         // ← 1:1 매핑 FK 설정
        tg.setTestDate(LocalDateTime.now());
        tg.setPart1Grade((float)p1);
        tg.setPart2Grade((float)p2);
        tg.setPart3Grade((float)p3);
        tg.setPart4Grade((float)p4);
        tg.setPart5Grade((float)p5);
        tg.setTestGrade(finalScore+" "+grade);
        TestGrade savedTg = tgRepo.save(tg);

        //    2-6. 학습 통계 업데이트
        //    AssessmentMocktest → SiteUser 가져와서, 저장된 TestGrade 엔티티로 반영
        learningStatisticsService.updateStatistics(am.getUser(), savedTg);

        // 2-7. 사용자 정보 업데이트
        SiteUser user = am.getUser();
        user.setUserLevel(finalScore+" "+grade);
        user.setLastTestedAt(LocalDateTime.now());
        userRepo.save(user);

        return new TestGradeDto(p1, p2, p3, p4, p5, finalScore+" "+grade);
    }

    /**
     * 4.1 사용자별 시험 기록 조회
     */
    public List<TestGradeHistoryDto> findGradesByUser(Long userId) {
        return amRepo.findByUser_Id(userId)
                .stream()
                .map(am -> {
                    // 모의고사 ID로 저장된 TestGrade 엔티티 로드
                    TestGrade tg = tgRepo.findByAssessment(am)
                            .orElseThrow(() -> new NoSuchElementException(
                                    "성적이 존재하지 않습니다: assessmentId=" + am.getId()));
                    return new TestGradeHistoryDto(
                            tg.getId(),
                            tg.getTestDate(),
                            tg.getPart1Grade(),
                            tg.getPart2Grade(),
                            tg.getPart3Grade(),
                            tg.getPart4Grade(),
                            tg.getPart5Grade(),
                            tg.getTestGrade()
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * 4.2 특정 성적 상세 조회
     */
    public Map<String, Object> getDetail(Long gradeId) {
        // 1) AssessmentMocktest 로드
        AssessmentMocktest am = amRepo.findById(gradeId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 기록: " + gradeId));

        // 2) TestGrade 로드
        TestGrade tg = tgRepo.findByAssessment(am)
                .orElseThrow(() -> new NoSuchElementException("성적 정보가 없습니다: assessmentId=" + gradeId));

        MocktestQuestion mq = am.getMocktest();

        // 3) 각 PartDto 구성
        PartDto part1 = new PartDto(null, null,
                List.of(
                        mq.getPart1().getQuestion1(),
                        mq.getPart1().getQuestion2()
                )
        );
        PartDto part2 = new PartDto(null, null,
                List.of(
                        mq.getPart2().getQuestion3(),
                        mq.getPart2().getQuestion4()
                )
        );
        PartDto part3 = new PartDto(null, mq.getPart3().getSituationText(),
                List.of(
                        mq.getPart3().getQuestion5(),
                        mq.getPart3().getQuestion6(),
                        mq.getPart3().getQuestion7()
                )
        );
        PartDto part4 = new PartDto(mq.getPart4().getSituationImage(), mq.getPart4().getSituationText(),
                List.of(
                        mq.getPart4().getQuestion8(),
                        mq.getPart4().getQuestion9(),
                        mq.getPart4().getQuestion10()
                )
        );
        PartDto part5 = new PartDto(null, null,
                List.of(
                        mq.getPart5().getQuestion11()
                )
        );

        // 4) 응답 구성
        Map<String,Object> body = new HashMap<>();
        body.put("questions", List.of(part1, part2, part3, part4, part5));
        body.put("evaluations", List.of(
                am.getQ1(), am.getQ2(), am.getQ3(), am.getQ4(), am.getQ5(),
                am.getQ6(), am.getQ7(), am.getQ8(), am.getQ9(), am.getQ10(), am.getQ11()
        ));

        return body;
    }

    // — 내부 유틸 메소드들 —
    private double partScore(JsonNode eval, int questionNumber) {
        JsonNode pron;
        JsonNode gpt = null;
        String userResponse;

        boolean isPart1 = eval.has("PronunciationAssessment");
        if (isPart1) {
            pron = eval.get("PronunciationAssessment");
            userResponse = eval.has("UserResponse") ? eval.get("UserResponse").asText() : "";
        } else if (eval.has("azureEvaluation") && eval.get("azureEvaluation").has("PronunciationAssessment")) {
            pron = eval.get("azureEvaluation").get("PronunciationAssessment");
            gpt = eval.get("gptEvaluation");
            userResponse = eval.get("azureEvaluation").has("UserResponse") ? eval.get("azureEvaluation").get("UserResponse").asText() : "";
        } else {
            throw new IllegalArgumentException("PronunciationAssessment가 존재하지 않음");
        }

        double a = safeGetDouble(pron, "AccuracyScore");
        double f = safeGetDouble(pron, "FluencyScore");
        double p = safeGetDouble(pron, "ProsodyScore");

        double baseScore;
        if (gpt != null) {
            double gr = safeGetDouble(gpt, "grammar");
            double t = safeGetDouble(gpt, "topic");
            double v = safeGetDouble(gpt, "vocabulary");
            baseScore = ((a + f + p) * 2 + (gr + t + v) * 3) / 15.0;
        } else {
            baseScore = (a + f + p) / 3.0;
        }

        // 사용자 발화 단어 수
        int userResponseLength = userResponse.trim().split("\\s+").length;

        if (isPart1) {
            // Part 1: IssueWords 기반 오류 감점
            int issueCount = 0;
            if (eval.has("IssueWords")) {
                for (JsonNode word : eval.get("IssueWords")) {
                    String errorType = word.path("ErrorType").asText("");
                    double acc = word.path("AccuracyScore").asDouble(100.0);

                    if (!"None".equals(errorType) || acc < 80.0) {
                        issueCount++;
                    }
                }
            }

            // 단어 하나당 2% 감점, 최대 40%
            double errorPenalty = Math.min(0.4, issueCount * 0.02);
            baseScore *= (1.0 - errorPenalty);

        } else {
            // Part 2~5: 제한 시간 기반 적정 발화 길이 비교 감점
            int expectedLength = getExpectedWordCount(questionNumber);
            double lengthRatio = (double) userResponseLength / expectedLength;

            if (lengthRatio < 0.5) {
                baseScore *= 0.7;
            } else if (lengthRatio < 0.8) {
                baseScore *= 0.85;
            } else if (lengthRatio > 1.3) {
                baseScore *= 0.9;
            }
        }

        return baseScore;
    }

    private int getExpectedWordCount(int questionNumber) {
        switch (questionNumber) {
            case 1: case 2:
                return 80;  // 45초
            case 3: case 4: case 7: case 10:
                return 50;  // 30초
            case 5: case 6: case 8: case 9:
                return 30;  // 15초
            case 11:
                return 110; // 60초
            default:
                return 50;  // fallback
        }
    }

    private double safeGetDouble(JsonNode node, String key) {
        return (node != null && node.has(key)) ? node.get(key).asDouble() : 0.0;
    }

    private double average(double... scores) {
        double sum = 0;
        for (double s : scores) sum += s;
        return sum / scores.length;
    }

    private String computeGrade(double score) {
        if (score >= 200)      return "AH";
        else if (score >= 180) return "AM";
        else if (score >= 160) return "AL";
        else if (score >= 140) return "IH";
        else if (score >= 130) return "IM3";
        else if (score >= 120) return "IM2";
        else if (score >= 110) return "IM1";
        else if (score >=  90) return "IL";
        else if (score >=  60) return "NH";
        else if (score >=  30) return "NM";
        else                   return "NL";
    }
}
