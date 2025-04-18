package com.example.epic.mocktest;

import com.example.epic.mocktest.MocktestQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MocktestQuestionRepository
        extends JpaRepository<MocktestQuestion, Long> { }
