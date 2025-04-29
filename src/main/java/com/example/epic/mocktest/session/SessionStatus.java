package com.example.epic.mocktest.session;

/** MocktestSession 진행 상태 */
public enum SessionStatus {
    IN_PROGRESS,   // 문제 풀이 중
    COMPLETED,     // 11문항 모두 제출 & 커밋 완료
    ABANDONED,     // 사용자가 수동 종료
    EXPIRED,       // TTL 초과로 서버가 파기
    ERROR          // 평가 API 실패 등 비정상 종료
}