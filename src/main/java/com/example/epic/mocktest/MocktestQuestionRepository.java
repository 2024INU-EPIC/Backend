package com.example.epic.mocktest;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MocktestQuestionRepository
        extends JpaRepository<MocktestQuestion, Long> {

    @Query(value = "SELECT TOP 1 * FROM epic.mocktest_question ORDER BY NEWID()", nativeQuery = true)
    Optional<MocktestQuestion> findRandom();
}