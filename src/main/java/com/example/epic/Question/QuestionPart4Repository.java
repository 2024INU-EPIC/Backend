package com.example.epic.Question;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuestionPart4Repository extends JpaRepository<QuestionPart4, Long> {
    // id 값으로 조회
    Optional<QuestionPart4> findByQuestionPart4Id(Long questionPart4Id);

    // 기존 랜덤 조회 메서드
    @Query(value = "SELECT TOP 1 * FROM epic.question_part4 ORDER BY NEWID()", nativeQuery = true)
    QuestionPart4 getRandomQuestion();
}