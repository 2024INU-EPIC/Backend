package com.example.epic.mocktest;

import org.springframework.data.jpa.repository.*;

public interface MocktestQuestionRepository
        extends JpaRepository<MocktestQuestion, Long> {

    @Query(value = "SELECT TOP 1 * FROM epic.mocktest_question ORDER BY NEWID()", nativeQuery = true)
    MocktestQuestion findRandom();
}