package com.example.epic.Question;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuestionPart5Repository extends JpaRepository<QuestionPart5, Long> {
    // id 값으로 조회
    Optional<QuestionPart5> findByQuestionPart5Id(Long questionPart5Id);

    // 기존 랜덤 조회 메서드
    @Query(value = "SELECT TOP 1 * FROM epic.question_part5 ORDER BY NEWID()", nativeQuery = true)
    QuestionPart5 getRandomQuestion();

    // 시연용 랜덤 조회 메서드
    @Query(value = "SELECT TOP 1 * FROM epic.question_part5 WHERE question_part5_id IN (2, 3, 5) ORDER BY NEWID()", nativeQuery = true)
    QuestionPart5 getDevQuestion();
}