package com.example.epic.mocktest.session;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MocktestSessionRepository
        extends JpaRepository<MocktestSession, UUID> {
    Optional<MocktestSession> findByIdAndUser_Id(UUID id, Long userId);
}