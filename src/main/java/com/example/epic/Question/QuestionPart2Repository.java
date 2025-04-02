package com.example.epic.Question;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionPart2Repository extends JpaRepository<QuestionPart2, Long> {
}