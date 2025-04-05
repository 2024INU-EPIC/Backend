package com.example.epic.Question;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionPart2Repository extends JpaRepository<QuestionPart2, Long> {
    @Query(value = "SELECT TOP 1 * FROM epic.question_part2 ORDER BY NEWID()", nativeQuery = true)
    QuestionPart2 getRandomQuestion();
}