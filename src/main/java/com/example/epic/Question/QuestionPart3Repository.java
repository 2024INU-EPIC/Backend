package com.example.epic.Question;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuestionPart3Repository extends JpaRepository<QuestionPart3, Long> {
    // id 값으로 조회
    Optional<QuestionPart3> findByQuestionPart3Id(Long questionPart3Id);

    // 기존 랜덤 조회 메서드
    @Query(value = "SELECT TOP 1 * FROM epic.question_part3 ORDER BY NEWID()", nativeQuery = true)
    QuestionPart3 getRandomQuestion();
}