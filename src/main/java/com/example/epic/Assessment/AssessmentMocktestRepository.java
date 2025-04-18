package com.example.epic.Assessment;

import com.example.epic.Assessment.AssessmentMocktest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AssessmentMocktestRepository
        extends JpaRepository<AssessmentMocktest, Long> {

    // 사용자‑날짜별 조회 예시
    List<AssessmentMocktest> findByUserIdAndCreatedAtBetween(
            Long userId, LocalDateTime from, LocalDateTime to);
}
