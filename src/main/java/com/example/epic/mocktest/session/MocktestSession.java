package com.example.epic.mocktest.session;

import com.example.epic.mocktest.MocktestQuestion;
import com.example.epic.user.SiteUser;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(schema = "epic", name = "mocktest_session")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class MocktestSession {

    /** 클라이언트-측에서 그대로 쓰는 식별자 */
    @Id
    @Column(name = "session_id", columnDefinition = "binary(16)")
    private UUID id;

    /** 로그인 유저 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private SiteUser user;

    /** 이번 시험에 고정된 문제 세트 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mocktest_id", referencedColumnName = "mocktest_id")
    private MocktestQuestion mocktest;

    /** 세션 완료 여부 */
    private boolean completed;

    @CreationTimestamp
    private LocalDateTime createdAt;
}