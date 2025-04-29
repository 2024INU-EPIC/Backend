package com.example.epic.mocktest.session;

import com.example.epic.mocktest.MocktestQuestion;
import com.example.epic.mocktest.MocktestQuestionRepository;
import com.example.epic.mocktest.MocktestQuestionService;
import com.example.epic.mocktest.dto.CompletedSessionDto;
import com.example.epic.mocktest.dto.MocktestPayloadDto;
import com.example.epic.Assessment.AssessmentMocktest;
import com.example.epic.Assessment.TestGrade;
import com.example.epic.mocktest.session.AssessmentMocktestService;
import com.example.epic.stats.LearningStatisticsService;
import com.example.epic.user.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MocktestSessionService {

    private final MocktestSessionRepository sessionRepo;
    private final MocktestQuestionRepository mocktestQuestionRepository;   // ← 추가
    private final MocktestQuestionService questionService;
    private final AssessmentMocktestService assessmentService;
    private final LearningStatisticsService learningStatisticsService;

    /**
     * 1) 세션 시작: mocktestId 검증 → 세션 생성 → 문제 페이로드 반환
     */
    public MocktestPayloadDto startSession(Long mocktestId, SiteUser user) {
        // 1. mocktestId 존재 확인
        mocktestQuestionRepository.findById(mocktestId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "MocktestQuestion not found: " + mocktestId));

        // 2. 새 세션 ID 생성
        UUID sessionId = UUID.randomUUID();

        // 3. ID만 설정한 MocktestQuestion 프록시 참조 생성
        MocktestQuestion mocktestRef = mocktestQuestionRepository.getReferenceById(mocktestId);

        // 4. 세션 엔티티 저장
        MocktestSession session = MocktestSession.builder()
                .id(sessionId)
                .user(user)
                .mocktest(mocktestRef)                          // ← new 대신 getReferenceById
                .status(SessionStatus.IN_PROGRESS)
                .build();
        sessionRepo.save(session);

        // 5. 생성된 sessionId로 페이로드 반환
        return questionService.getPayload(sessionId, mocktestId);
    }

    /**
     * 3) 개별 문제 응답 저장
     */
    @Transactional
    public void saveAssessment(UUID sessionId, int qNo, String json) {
        // qNo 범위 검증 추가
        if (qNo < 1 || qNo > 11) {
            throw new IllegalArgumentException("Invalid question number: " + qNo);
        }

        MocktestSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Session not found: " + sessionId));

        switch (qNo) {
            case 1:  session.setAssessmentQuestion1(json); break;
            case 2:  session.setAssessmentQuestion2(json); break;
            // …
            case 11: session.setAssessmentQuestion11(json); break;
        }
        sessionRepo.save(session);
    }

    /**
     * 4) 세션 완료 처리
     */
    @Transactional
    public CompletedSessionDto completeSession(UUID sessionId) {
        MocktestSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Session not found: " + sessionId));

        // A) 임시 세션 데이터 → AssessmentMocktest 저장
        AssessmentMocktest a = assessmentService.saveAssessmentFromSession(session);

        // B) 점수 계산 & TestGrade 저장
        TestGrade g = assessmentService.calculateTestGrade(a);

        // C) 학습 통계 업데이트
        learningStatisticsService.updateStatistics(session.getUser(), g);

        // D) 세션 상태 완료 처리
        session.setStatus(SessionStatus.COMPLETED);
        sessionRepo.save(session);

        // E) 평가 JSON 묶음 반환
        List<String> all = List.of(
                session.getAssessmentQuestion1(),
                session.getAssessmentQuestion2(),
                session.getAssessmentQuestion3(),
                session.getAssessmentQuestion4(),
                session.getAssessmentQuestion5(),
                session.getAssessmentQuestion6(),
                session.getAssessmentQuestion7(),
                session.getAssessmentQuestion8(),
                session.getAssessmentQuestion9(),
                session.getAssessmentQuestion10(),
                session.getAssessmentQuestion11()
        );
        return new CompletedSessionDto(sessionId, all);
    }
}