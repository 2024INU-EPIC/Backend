package com.example.epic.Assessment;

import com.example.epic.Question.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/upload-audio")
public class AssessmentController {

    private final AssessmentService assessmentService;
    private final QuestionPart1Repository questionPart1Repository;
    private final QuestionPart2Repository questionPart2Repository;
    private final QuestionPart3Repository questionPart3Repository;
    private final QuestionPart4Repository questionPart4Repository;
    private final QuestionPart5Repository questionPart5Repository;

    public AssessmentController(
            AssessmentService assessmentService,
            QuestionPart1Repository questionPart1Repository,
            QuestionPart2Repository questionPart2Repository,
            QuestionPart3Repository questionPart3Repository,
            QuestionPart4Repository questionPart4Repository,
            QuestionPart5Repository questionPart5Repository
    ) {
        this.assessmentService = assessmentService;
        this.questionPart1Repository = questionPart1Repository;
        this.questionPart2Repository = questionPart2Repository;
        this.questionPart3Repository = questionPart3Repository;
        this.questionPart4Repository = questionPart4Repository;
        this.questionPart5Repository = questionPart5Repository;
    }

    @PostMapping("/part{partNo}")
    public CompletableFuture<ResponseEntity<String>> evaluateSpeaking(
            @PathVariable("partNo") int partNo,
            @RequestParam("questionId") Long questionId,
            @RequestParam("questionNo") int questionNo,
            @RequestParam(required = false) MultipartFile file) {

        try {
            String tempFilePath = saveMultipartFileToTemp(file);

            switch (partNo) {
                case 1: {
                    QuestionPart1 q = questionPart1Repository.findById(questionId)
                            .orElseThrow(() -> new IllegalArgumentException("문제를 찾을 수 없습니다."));
                    String text = getFieldByNumber(q, questionNo);
                    return assessmentService.evaluateSpeechPronunciationAsync(text, tempFilePath)
                            .thenApply(ResponseEntity::ok);
                }
                case 2: {
                    QuestionPart2 q = questionPart2Repository.findById(questionId)
                            .orElseThrow(() -> new IllegalArgumentException("문제를 찾을 수 없습니다."));
                    String imageUrl = getFieldByNumber(q, questionNo);
                    return assessmentService.evaluateSpeechWithQuestionImageAsync(tempFilePath, imageUrl)
                            .thenApply(ResponseEntity::ok);
                }
                case 3: {
                    QuestionPart3 q = questionPart3Repository.findById(questionId)
                            .orElseThrow(() -> new IllegalArgumentException("문제를 찾을 수 없습니다."));
                    String situationText = q.getSituationText();
                    String questionText = getFieldByNumber(q, questionNo);
                    return assessmentService.evaluateSpeechWithSituationTextAsync(tempFilePath, situationText, questionText)
                            .thenApply(ResponseEntity::ok);
                }
                case 4: {
                    QuestionPart4 q = questionPart4Repository.findById(questionId)
                            .orElseThrow(() -> new IllegalArgumentException("문제를 찾을 수 없습니다."));
                    String situationText = q.getSituationText();
                    String situationImage = q.getSituationImage();
                    String questionText = getFieldByNumber(q, questionNo);
                    return assessmentService.evaluateSpeechWithSituationImageAsync(tempFilePath, situationImage, situationText, questionText)
                            .thenApply(ResponseEntity::ok);
                }
                case 5: {
                    QuestionPart5 q = questionPart5Repository.findById(questionId)
                            .orElseThrow(() -> new IllegalArgumentException("문제를 찾을 수 없습니다."));
                    String questionText = getFieldByNumber(q, questionNo);
                    return assessmentService.evaluateSpeechWithQuestionTextAsync(tempFilePath, questionText)
                            .thenApply(ResponseEntity::ok);
                }
                default:
                    return CompletableFuture.completedFuture(ResponseEntity.badRequest().body("지원되지 않는 파트입니다."));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(
                    ResponseEntity.badRequest().body("Error: " + e.getMessage())
            );
        }
    }

    private String saveMultipartFileToTemp(MultipartFile file) throws Exception {
        File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".wav");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(file.getBytes());
        }
        return tempFile.getAbsolutePath();
    }

    private String getFieldByNumber(Object questionObject, int questionNo) {
        try {
            String fieldName = "question" + questionNo;
            Field field = questionObject.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return (String) field.get(questionObject);
        } catch (Exception e) {
            throw new IllegalArgumentException("필드를 찾을 수 없습니다: question" + questionNo, e);
        }
    }
}