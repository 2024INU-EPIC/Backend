package com.example.epic.Question;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionPart1Repository extends JpaRepository<QuestionPart1, Long> {
    @Query(value = "SELECT TOP 1 * FROM epic.question_part1 ORDER BY NEWID()", nativeQuery = true)
    QuestionPart1 getRandomQuestion();
}