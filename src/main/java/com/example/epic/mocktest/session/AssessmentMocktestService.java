package com.example.epic.mocktest.session;

import com.example.epic.Assessment.AssessmentMocktest;
import com.example.epic.Assessment.AssessmentMocktestRepository;
import com.example.epic.Assessment.TestGrade;
import com.example.epic.Assessment.TestGradeRepository;
import com.example.epic.mocktest.session.MocktestSession;
import com.example.epic.mocktest.session.MocktestSessionRepository;
import com.example.epic.mocktest.dto.TestGradeDto;
import com.example.epic.stats.LearningStatisticsService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AssessmentMocktestService {

    private final AssessmentMocktestRepository amRepo;
    private final TestGradeRepository        tgRepo;
    private final MocktestSessionRepository  sessionRepo;
    private final LearningStatisticsService  learningStatisticsService;
    private final ObjectMapper               mapper = new ObjectMapper();

    public AssessmentMocktestService(
            AssessmentMocktestRepository amRepo,
            TestGradeRepository        tgRepo,
            MocktestSessionRepository  sessionRepo,
            LearningStatisticsService  learningStatisticsService
    ) {
        this.amRepo       = amRepo;
        this.tgRepo       = tgRepo;
        this.sessionRepo  = sessionRepo;
        this.learningStatisticsService = learningStatisticsService;
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
        double p1 = average(partScore(evals.get(0)), partScore(evals.get(1)));
        double p2 = average(partScore(evals.get(2)), partScore(evals.get(3)));
        double p3 = average(partScore(evals.get(4)), partScore(evals.get(5)), partScore(evals.get(6)));
        double p4 = average(partScore(evals.get(7)), partScore(evals.get(8)), partScore(evals.get(9)));
        double p5 = partScore(evals.get(10));

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

        return new TestGradeDto(p1, p2, p3, p4, p5, finalScore+" "+grade);
    }

    // — 내부 유틸 메소드들 —

    private double partScore(JsonNode eval) {
        JsonNode pron;
        JsonNode gpt = null;

        boolean isPart1 = eval.has("PronunciationAssessment");
        if (isPart1) {
            pron = eval.get("PronunciationAssessment");
        } else if (eval.has("azureEvaluation") && eval.get("azureEvaluation").has("PronunciationAssessment")) {
            pron = eval.get("azureEvaluation").get("PronunciationAssessment");
            gpt  = eval.get("gptEvaluation");
        } else {
            throw new IllegalArgumentException("PronunciationAssessment가 존재하지 않음");
        }

        double a = safeGetDouble(pron, "AccuracyScore");
        double f = safeGetDouble(pron, "FluencyScore");
        double p = safeGetDouble(pron, "ProsodyScore");

        if (gpt != null) {
            double gr = safeGetDouble(gpt, "grammar");
            double t  = safeGetDouble(gpt, "topic");
            double v  = safeGetDouble(gpt, "vocabulary");
            return ((a + f + p) * 3 + (gr + t + v) * 2) / 15.0;
        } else {
            return (a + f + p) / 3.0;
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
