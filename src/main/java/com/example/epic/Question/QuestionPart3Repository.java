package com.example.epic.Question;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionPart3Repository extends JpaRepository<QuestionPart3, Long> {
    @Query(value = "SELECT TOP 1 * FROM epic.question_part3 ORDER BY NEWID()", nativeQuery = true)
    QuestionPart3 getRandomQuestion();
}