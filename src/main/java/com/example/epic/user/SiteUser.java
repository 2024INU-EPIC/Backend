package com.example.epic.user;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

// Getter, Setter 추가 시 자동적으로 getXxx(), setXxx() 메소드 생성해서 편리.
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(schema = "epic", name = "site_user")
public class SiteUser {
    // Column annotation 내의 unique 프로퍼티는 중복되지 않아야 하는 경우 추가.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 값이 자동적으로 증가되면서 기본 키를 생성하도록 설정.
    private Long id;

    @Column(length = 25, nullable = false, unique = true)
    private String username;

    @Column(length = 100, nullable = false)
    private String password;

    @Column(length = 30, nullable = false, unique = true)
    private String email;

    @Column(length = 20, nullable = true)
    private String user_level;

    @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm-:s")
    @Column(nullable = true)
    private LocalDateTime last_tested_at;

    // 사용자 권한 부여
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.USER;
}
