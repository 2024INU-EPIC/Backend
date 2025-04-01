package com.example.epic.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EpicUserRepository extends JpaRepository<EpicUser, Long> {
    Optional<EpicUser> findByUserEmail(String email);
}

