package com.example.epic.mocktest.session;

import com.example.epic.Assessment.AssessmentMocktest;
import com.example.epic.Assessment.AssessmentMocktestRepository;
import com.example.epic.Assessment.TestGrade;
import com.example.epic.Assessment.TestGradeRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AssessmentMocktestService {

    private final AssessmentMocktestRepository assessmentRepo;
    private final TestGradeRepository         testGradeRepo;
    private final ObjectMapper               objectMapper;

    public AssessmentMocktest saveAssessmentFromSession(MocktestSession session) {
        AssessmentMocktest a = AssessmentMocktest.builder()
                .user(session.getUser())
                .mocktest(session.getMocktest())
                .q1(session.getAssessmentQuestion1())
                .q2(session.getAssessmentQuestion2())
                .q3(session.getAssessmentQuestion3())
                .q4(session.getAssessmentQuestion4())
                .q5(session.getAssessmentQuestion5())
                .q6(session.getAssessmentQuestion6())
                .q7(session.getAssessmentQuestion7())
                .q8(session.getAssessmentQuestion8())
                .q9(session.getAssessmentQuestion9())
                .q10(session.getAssessmentQuestion10())
                .q11(session.getAssessmentQuestion11())
                .build();
        return assessmentRepo.save(a);
    }

    public TestGrade calculateTestGrade(AssessmentMocktest a) {
        try {
            // Part1
            float part1 = (parsePart1(a.getQ1()) + parsePart1(a.getQ2())) / 2;
            // Part2~5
            float part2 = (parseAzureGpt(a.getQ3()) + parseAzureGpt(a.getQ4())) / 2;
            float part3 = (parseAzureGpt(a.getQ5()) + parseAzureGpt(a.getQ6()) + parseAzureGpt(a.getQ7())) / 3;
            float part4 = (parseAzureGpt(a.getQ8()) + parseAzureGpt(a.getQ9()) + parseAzureGpt(a.getQ10())) / 3;
            float part5 = parseAzureGpt(a.getQ11());

            // 총점 & 등급
            float raw = ((part1 + part2 + part3 + part4) * 3 + part5 * 5) * 0.05714f;
            int totalRounded = Math.round(raw / 10) * 10;
            String gradeStr = mapScoreToGrade(totalRounded);

            TestGrade tg = TestGrade.builder()
                    .assessment(a)
                    .testDate(LocalDateTime.now())
                    .part1Grade(part1)
                    .part2Grade(part2)
                    .part3Grade(part3)
                    .part4Grade(part4)
                    .part5Grade(part5)
                    .testGrade(totalRounded+gradeStr)
                    .build();
            return testGradeRepo.save(tg);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("평가 JSON 파싱 중 실패했습니다.", e);
        }
    }

    // Part1 전용: PronunciationAssessment만 사용
    private float parsePart1(String json) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(json);
        JsonNode p = root.get("PronunciationAssessment");
        float acc = p.get("AccuracyScore").floatValue();
        float flu = p.get("FluencyScore").floatValue();
        float pro = p.get("ProsodyScore").floatValue();
        return (acc + flu + pro) / 3;
    }

    // Part2~5 공통: Azure + GPT 평가
    private float parseAzureGpt(String json) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(json);
        JsonNode azure = root.has("azureEvaluation")
                ? root.get("azureEvaluation").get("PronunciationAssessment")
                : root.get("PronunciationAssessment");
        float acc = azure.get("AccuracyScore").floatValue();
        float flu = azure.get("FluencyScore").floatValue();
        float pro = azure.get("ProsodyScore").floatValue();

        JsonNode gpt = root.get("gptEvaluation");
        float gr = gpt.get("grammar").floatValue();
        float tp = gpt.get("topic").floatValue();
        float voc = gpt.get("vocabulary").floatValue();

        return ((acc + flu + pro) * 3 + (gr + tp + voc) * 2) / 15;
    }

    private String mapScoreToGrade(int score) {
        if (score == 200) return " AH";
        if (score >= 180) return " AM";
        if (score >= 160) return " AL";
        if (score >= 140) return " IH";
        if (score >= 130) return " IM3";
        if (score >= 120) return " IM2";
        if (score >= 110) return " IM1";
        if (score >= 90)  return " IL";
        if (score >= 60)  return " NH";
        if (score >= 30)  return " NM";
        return " NL";
    }
}
