package com.example.epic.Question;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionPart1Repository extends JpaRepository<QuestionPart1, Long> {
}