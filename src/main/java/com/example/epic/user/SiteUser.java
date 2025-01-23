package com.example.epic.user;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;

import lombok.Getter;
import lombok.Setter;

// Getter, Setter 추가 시 자동적으로 getXxx(), setXxx() 메소드 생성해서 편리.
@Getter
@Setter
@Entity
public class SiteUser {
    // Column annotation 내의 unique 프로퍼티는 중복되지 않아야 하는 경우 추가.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 값이 자동적으로 증가되면서 기본 키를 생성하도록 설정.
    private Long id;

    @Column(length = 25, nullable = false)
    private String username;

    @Column(length = 100, nullable = false)
    private String password;

    @Column(length = 30, unique = true, nullable = false)
    private String email;

    @Column(length = 20, nullable = true)
    private String currentlevel;
}
