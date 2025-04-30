package com.example.epic.mocktest.session;

/**
 * 평가 중 오류 발생 시 세션을 종료하기 위한 커스텀 예외
 */
public class AssessmentException extends RuntimeException {
    public AssessmentException(String message, Throwable cause) {
        super(message, cause);
    }
    public AssessmentException(String message) {
        super(message);
    }
}