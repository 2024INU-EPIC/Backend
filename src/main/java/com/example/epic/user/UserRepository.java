package com.example.epic.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<SiteUser, Long> {
    Optional<SiteUser> findByEmail(String email);
    // JWT 인증 시 사용하는 레퍼지터리 함수
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
