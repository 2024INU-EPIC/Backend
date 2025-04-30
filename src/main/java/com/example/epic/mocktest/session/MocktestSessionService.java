package com.example.epic.mocktest.session;

import com.example.epic.Assessment.AssessmentService;
import com.example.epic.mocktest.MocktestQuestion;
import com.example.epic.mocktest.MocktestQuestionRepository;
import com.example.epic.mocktest.dto.AssessmentResultDto;
import com.example.epic.mocktest.dto.CompletedSessionDto;
import com.example.epic.mocktest.dto.MocktestPayloadDto;
import com.example.epic.mocktest.dto.PartDto;
import com.example.epic.mocktest.dto.TestGradeDto;
import com.example.epic.mocktest.session.MocktestSession;
import com.example.epic.mocktest.session.MocktestSessionRepository;
import com.example.epic.mocktest.session.AssessmentMocktestService;
import com.example.epic.Question.QuestionPart1;
import com.example.epic.Question.QuestionPart2;
import com.example.epic.Question.QuestionPart3;
import com.example.epic.Question.QuestionPart4;
import com.example.epic.Question.QuestionPart5;
import com.example.epic.Question.QuestionPart1Repository;
import com.example.epic.Question.QuestionPart2Repository;
import com.example.epic.Question.QuestionPart3Repository;
import com.example.epic.Question.QuestionPart4Repository;
import com.example.epic.Question.QuestionPart5Repository;
import com.example.epic.user.SiteUser;
import com.example.epic.user.UserRepository;
import com.example.epic.mocktest.session.SessionStatus;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MocktestSessionService {

    private final MocktestSessionRepository sessionRepo;
    private final MocktestQuestionRepository questionRepo;
    private final UserRepository userRepo;                           // ← SiteUser 로드용
    private final AssessmentService assessmentService;
    private final AssessmentMocktestService persistService;
    private final QuestionPart1Repository q1Repo;
    private final QuestionPart2Repository q2Repo;
    private final QuestionPart3Repository q3Repo;
    private final QuestionPart4Repository q4Repo;
    private final QuestionPart5Repository q5Repo;

    /** sessionId → 평가 JSON 문자열 리스트 */
    private final Map<UUID, List<String>> buffer = new ConcurrentHashMap<>();

    public MocktestSessionService(
            MocktestSessionRepository sessionRepo,
            MocktestQuestionRepository questionRepo,
            UserRepository userRepo,
            AssessmentService assessmentService,
            AssessmentMocktestService persistService,
            QuestionPart1Repository q1Repo,
            QuestionPart2Repository q2Repo,
            QuestionPart3Repository q3Repo,
            QuestionPart4Repository q4Repo,
            QuestionPart5Repository q5Repo
    ) {
        this.sessionRepo      = sessionRepo;
        this.questionRepo     = questionRepo;
        this.userRepo         = userRepo;
        this.assessmentService= assessmentService;
        this.persistService   = persistService;
        this.q1Repo           = q1Repo;
        this.q2Repo           = q2Repo;
        this.q3Repo           = q3Repo;
        this.q4Repo           = q4Repo;
        this.q5Repo           = q5Repo;
    }

    /** 1) 시험 시작: SiteUser 로드 → 세션 생성 + 버퍼 초기화 */
    public UUID startSession(Long userId) {
        SiteUser user = userRepo.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 사용자: " + userId));
        MocktestQuestion mq = questionRepo.findRandom();
        MocktestSession session = MocktestSession.builder()
                .user(user)                      // ← userId가 아닌 user 엔티티
                .mocktest(mq)                    // ← 필드명이 mocktest
                .status(SessionStatus.IN_PROGRESS)
                .build();
        session = sessionRepo.save(session);
        buffer.put(session.getId(), new ArrayList<>());
        return session.getId();
    }

    /** 2) 문제 묶음 반환: session.getMocktest() 로 접근 */
    public MocktestPayloadDto getPayload(UUID sessionId) {
        MocktestSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new NoSuchElementException("Invalid session: " + sessionId));
        MocktestQuestion mq = session.getMocktest();   // ← getMocktest()

        // 각 PartDto 생성 (PartDto 생성자는 기존 코드 그대로 재사용)
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

        return new MocktestPayloadDto(
                sessionId,
                mq.getId(),
                part1, part2, part3, part4, part5
        );
    }

    /** 3) 답안 저장: 파일 → 평가 → 버퍼링 → 삭제 */
    public AssessmentResultDto saveAssessment(UUID sessionId, int part, int qNo, MultipartFile audio) {
        Path tmp = null;
        try {
            tmp = Files.createTempFile("mocktest_", ".wav");
            Files.copy(audio.getInputStream(), tmp, StandardCopyOption.REPLACE_EXISTING);

            MocktestSession session = sessionRepo.findById(sessionId)
                    .orElseThrow(() -> new NoSuchElementException("Invalid session: " + sessionId));
            MocktestQuestion mq = session.getMocktest();  // ← getMocktest()

            // part, qNo 에 따라 질문 텍스트 골라내기
            String questionText;
            switch (part) {
                case 1 -> {
                    var q = mq.getPart1();
                    questionText = (qNo == 1 ? q.getQuestion1() : q.getQuestion2());
                }
                case 2 -> {
                    var q = mq.getPart2();
                    questionText = (qNo == 3 ? q.getQuestion3() : q.getQuestion4());
                }
                case 3 -> {
                    var q = mq.getPart3();
                    questionText = switch (qNo) {
                        case 5 -> q.getQuestion5();
                        case 6 -> q.getQuestion6();
                        default -> q.getQuestion7();
                    };
                }
                case 4 -> {
                    var q = mq.getPart4();
                    questionText = switch (qNo) {
                        case 8  -> q.getQuestion8();
                        case 9  -> q.getQuestion9();
                        default -> q.getQuestion10();
                    };
                }
                case 5 -> {
                    var q = mq.getPart5();
                    questionText = q.getQuestion11();
                }
                default -> throw new IllegalArgumentException("Invalid part: " + part);
            }

            // 평가 API 호출 (동기 대기)
            String jsonResult = switch (part) {
                case 1 -> assessmentService
                        .evaluateSpeechPronunciationAsync(questionText, tmp.toString())
                        .join();
                case 2 -> assessmentService
                        .evaluateSpeechWithQuestionImageAsync(tmp.toString(), questionText)
                        .join();
                case 3 -> assessmentService
                        .evaluateSpeechWithSituationTextAsync(tmp.toString(),
                                mq.getPart3().getSituationText(),
                                questionText)
                        .join();
                case 4 -> assessmentService
                        .evaluateSpeechWithSituationImageAsync(tmp.toString(),
                                mq.getPart4().getSituationImage(),
                                mq.getPart4().getSituationText(),
                                questionText)
                        .join();
                case 5 -> assessmentService
                        .evaluateSpeechWithQuestionTextAsync(tmp.toString(), questionText)
                        .join();
                default -> throw new IllegalStateException();
            };

            buffer.get(sessionId).add(jsonResult);
            return new AssessmentResultDto(part, qNo, jsonResult);

        } catch (IOException e) {
            throw new RuntimeException("오디오 파일 처리 중 오류", e);
        } finally {
            if (tmp != null) {
                try { Files.deleteIfExists(tmp); } catch (IOException ignored) {}
            }
        }
    }

    /** 4) 시험 완료: DB 저장 + 성적 계산 + 세션·버퍼 삭제 */
    public CompletedSessionDto completeSession(UUID sessionId) {
        // 1. 버퍼에서 평가 결과 가져오기
        List<String> allJson = Optional.ofNullable(buffer.remove(sessionId))
                .orElse(Collections.emptyList());

        // 2. 세션 엔티티 조회
        MocktestSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid session: " + sessionId));

        // 3. 평가 저장: 세션이 DB에 남아있는 상태에서 저장
        Long assessmentId = persistService.saveAssessmentFromSession(sessionId, allJson);

        // 4. 세션 삭제: 평가 저장 이후에 삭제
        sessionRepo.delete(session);

        // 5. 성적 계산 및 응답 생성
        TestGradeDto grade = persistService.calculateTestGrade(assessmentId);
        return new CompletedSessionDto(sessionId, allJson, grade);
    }
}