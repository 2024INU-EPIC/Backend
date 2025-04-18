package com.example.epic.Question;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuestionPart1Repository extends JpaRepository<QuestionPart1, Long> {
    // id 값으로 조회
    Optional<QuestionPart1> findByQuestionPart1Id(Long questionPart1Id);

    // 기존 랜덤 조회 메서드
    @Query(value = "SELECT TOP 1 * FROM epic.question_part1 ORDER BY NEWID()", nativeQuery = true)
    QuestionPart1 getRandomQuestion();
}