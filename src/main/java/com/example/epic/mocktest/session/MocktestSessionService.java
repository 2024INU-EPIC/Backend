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
import com.example.epic.mocktest.session.AssessmentException;
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
        MocktestSession session = null;
        try {
            // ① 음성 파일 미제공 체크
            if (audio == null || audio.isEmpty()) {
                throw new AssessmentException("음성 파일이 제공되지 않아 시험을 종료합니다.");
            }
            // ② 임시 WAV 파일 생성
            tmp = Files.createTempFile("mocktest_", ".wav");
            Files.copy(audio.getInputStream(), tmp, StandardCopyOption.REPLACE_EXISTING);

            // ③ 세션 조회
            session = sessionRepo.findById(sessionId)
                    .orElseThrow(() -> new NoSuchElementException("유효하지 않은 세션: " + sessionId));

            // ④ 질문 텍스트 선택
            String questionText = selectQuestionText(session.getMocktest(), part, qNo);

            // ⑤ 평가 API 호출 (동기 대기)
            String jsonResult = switch (part) {
                case 1 -> assessmentService.evaluateSpeechPronunciationAsync(questionText, tmp.toString()).join();
                case 2 -> assessmentService.evaluateSpeechWithQuestionImageAsync(tmp.toString(), questionText).join();
                case 3 -> assessmentService.evaluateSpeechWithSituationTextAsync(tmp.toString(),
                        session.getMocktest().getPart3().getSituationText(), questionText).join();
                case 4 -> assessmentService.evaluateSpeechWithSituationImageAsync(tmp.toString(),
                        session.getMocktest().getPart4().getSituationImage(),
                        session.getMocktest().getPart4().getSituationText(), questionText).join();
                case 5 -> assessmentService.evaluateSpeechWithQuestionTextAsync(tmp.toString(), questionText).join();
                default -> throw new IllegalArgumentException("Invalid part: " + part);
            };

            // ⑥ 버퍼에 결과 저장
            buffer.get(sessionId).add(jsonResult);
            return new AssessmentResultDto(part, qNo, jsonResult);

        } catch (Exception ex) {
            // ⑦ 오류 발생 시 세션 및 버퍼 정리
            buffer.remove(sessionId);
            if (session != null) {
                sessionRepo.delete(session);
            }
            // ⑧ 컨트롤러에서 한 번에 처리할 수 있게 커스텀 예외 던지기
            throw (ex instanceof AssessmentException)
                    ? (AssessmentException) ex
                    : new AssessmentException("평가 중 오류가 발생하여 세션을 종료합니다.", ex);
        } finally {
            // ⑨ 임시 파일 삭제
            if (tmp != null) {
                try { Files.deleteIfExists(tmp); }
                catch (IOException ignored) {}
            }
        }
    }

    // 질문 선택 메서드
    private String selectQuestionText(MocktestQuestion mq, int part, int qNo) {
        return switch (part) {
            case 1 -> (qNo == 1 ? mq.getPart1().getQuestion1() : mq.getPart1().getQuestion2());
            case 2 -> (qNo == 3 ? mq.getPart2().getQuestion3() : mq.getPart2().getQuestion4());
            case 3 -> switch (qNo) {
                case 5 -> mq.getPart3().getQuestion5();
                case 6 -> mq.getPart3().getQuestion6();
                default -> mq.getPart3().getQuestion7();
            };
            case 4 -> switch (qNo) {
                case 8  -> mq.getPart4().getQuestion8();
                case 9  -> mq.getPart4().getQuestion9();
                default -> mq.getPart4().getQuestion10();
            };
            case 5 -> mq.getPart5().getQuestion11();
            default -> throw new IllegalArgumentException("Invalid part: " + part);
        };
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

    public void cancelSession(UUID sessionId) {
        // 버퍼·DB 레코드 클리어
        buffer.remove(sessionId);
        sessionRepo.findById(sessionId).ifPresent(sessionRepo::delete);
    }

    @Scheduled(fixedRate = 10 * 60 * 1000)  // 10분마다 실행
    public void cleanupStaleSessions() {
        Instant cutoff = Instant.now().minus(Duration.ofMinutes(5));  // 5분 무응답 세션
        List<MocktestSession> stale = sessionRepo
                .findByStatusAndLastActivityBefore(SessionStatus.IN_PROGRESS, cutoff);
        stale.forEach(s -> {
            buffer.remove(s.getId());
            sessionRepo.delete(s);
        });
    }
}