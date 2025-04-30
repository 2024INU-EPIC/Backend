package com.example.epic.Assessment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TestGradeRepository
        extends JpaRepository<TestGrade, Long> {

    // 특정 AssessmentMocktest에 매핑된 TestGrade 조회
    Optional<TestGrade> findByAssessment(AssessmentMocktest assessment);
}