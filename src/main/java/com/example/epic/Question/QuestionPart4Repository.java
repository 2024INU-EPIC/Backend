package com.example.epic.Question;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionPart4Repository extends JpaRepository<QuestionPart4, Long> {
    @Query(value = "SELECT TOP 1 * FROM epic.question_part4 ORDER BY NEWID()", nativeQuery = true)
    QuestionPart4 getRandomQuestion();
}