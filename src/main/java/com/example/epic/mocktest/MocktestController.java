package com.example.epic.mocktest;

import com.example.epic.mocktest.dto.AssessmentResultDto;
import com.example.epic.mocktest.dto.CompletedSessionDto;
import com.example.epic.mocktest.dto.MocktestPayloadDto;
import com.example.epic.mocktest.dto.TestGradeHistoryDto;
import com.example.epic.mocktest.session.AssessmentException;
import com.example.epic.mocktest.session.AssessmentMocktestService;
import com.example.epic.mocktest.session.MocktestSessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/mocktest")
public class MocktestController {

    private final MocktestSessionService sessionService;
    private final AssessmentMocktestService amService;

    public MocktestController(
            MocktestSessionService sessionService,
            AssessmentMocktestService amService
    ) {
        this.sessionService = sessionService;
        this.amService      = amService;
    }

    /** 1) 시험 시작: 세션 생성 + 문제 묶음 반환 */
    @PostMapping("/start")
    public ResponseEntity<MocktestPayloadDto> start(
            @RequestParam Long userId
    ) {
        UUID sessionId = sessionService.startSession(userId);
        MocktestPayloadDto payload = sessionService.getPayload(sessionId);
        return ResponseEntity.ok(payload);
    }

    @PostMapping("/{sessionId}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable UUID sessionId) {
        sessionService.cancelSession(sessionId);
        return ResponseEntity.ok().build();
    }

    /** 2) 답안 업로드: 평가 수행 + 임시 버퍼링 */
    @PostMapping("/{sessionId}/save/{part}/{qNo}")
    public ResponseEntity<AssessmentResultDto> saveAnswer(
            @PathVariable UUID sessionId,
            @PathVariable int part,
            @PathVariable int qNo,
            @RequestParam("audio") MultipartFile audio
    ) {
        AssessmentResultDto result = sessionService.saveAssessment(sessionId, part, qNo, audio);
        return ResponseEntity.ok(result);
    }

    /** 3) 시험 완료: 평가 저장 + 성적 계산 반환 */
    @PostMapping("/{sessionId}/complete")
    public ResponseEntity<CompletedSessionDto> complete(
            @PathVariable UUID sessionId
    ) {
        CompletedSessionDto completed = sessionService.completeSession(sessionId);
        return ResponseEntity.ok(completed);
    }

    /** 4.1 시험 기록 조회 */
    @GetMapping("/history/{userId}")
    public ResponseEntity<List<TestGradeHistoryDto>> getHistory(@PathVariable Long userId) {
        List<TestGradeHistoryDto> history = amService.findGradesByUser(userId);
        return ResponseEntity.ok(history);
    }

    /** 4.2 특정 기록 상세 조회 */
    @GetMapping("/{gradeId}/detail")
    public ResponseEntity<Map<String,Object>> getDetail(@PathVariable Long gradeId) {
        Map<String,Object> detail = amService.getDetail(gradeId);
        return ResponseEntity.ok(detail);
    }

    @ExceptionHandler(AssessmentException.class)
    public ResponseEntity<String> handleAssessmentError(AssessmentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }
}
