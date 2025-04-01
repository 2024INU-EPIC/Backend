package com.example.epic.Assessment;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1")
public class AssessmentController {

    private final AssessmentService assessmentService;

    public AssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    /**
     * Part1: 스크립트가 있는 발음 평가
     */
    @PostMapping("/part1")
    public CompletableFuture<ResponseEntity<String>> evaluatePart1(
            @RequestParam String referenceText,
            @RequestParam MultipartFile file) {
        try {
            // WAV 파일을 임시 경로에 저장
            String tempFilePath = saveMultipartFileToTemp(file);

            // Service 호출
            return assessmentService.evaluateSpeechPronunciationAsync(referenceText, tempFilePath)
                    .thenApply(result -> ResponseEntity.ok(result));

        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(
                    ResponseEntity.badRequest().body("Error: " + e.getMessage())
            );
        }
    }

    /**
     * Part5: 스크립트 없는 말하기 평가(GPT 결합)
     */
    @PostMapping("/part5")
    public CompletableFuture<ResponseEntity<String>> evaluatePart5(
            @RequestParam MultipartFile file) {
        try {
            // 임시 경로에 저장
            String tempFilePath = saveMultipartFileToTemp(file);

            // 스크립트 없이 말하기 평가 + GPT
            String questiontext = "The only way to reduce the amount of traffic in cities today is by reducing the need for people to travel from home for work, education or shopping. Do you agree or disagree with this point of view? Use specific reasons and examples to support your choice."; // 일단 하드 코딩으로 문제 넣기
            return assessmentService.evaluateSpeechWithQuestionTextAsync(tempFilePath, questiontext)
                    .thenApply(result -> ResponseEntity.ok(result));

        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(
                    ResponseEntity.badRequest().body("Error: " + e.getMessage())
            );
        }
    }

    /**
     * MultipartFile -> 임시 파일로 저장 후 경로 반환
     */
    private String saveMultipartFileToTemp(MultipartFile file) throws Exception {
        // 임시 폴더에 랜덤 이름으로 저장
        File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".wav");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(file.getBytes());
        }
        return tempFile.getAbsolutePath();
    }
}