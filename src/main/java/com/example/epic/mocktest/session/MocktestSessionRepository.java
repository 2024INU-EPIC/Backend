package com.example.epic.mocktest.session;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MocktestSessionRepository
        extends JpaRepository<MocktestSession, UUID> {
}