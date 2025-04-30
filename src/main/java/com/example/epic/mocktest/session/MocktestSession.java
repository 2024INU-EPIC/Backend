package com.example.epic.mocktest.session;

import com.example.epic.mocktest.MocktestQuestion;
import com.example.epic.user.SiteUser;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(schema = "epic", name = "mocktest_session")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class MocktestSession {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "session_id", columnDefinition = "uniqueidentifier")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private SiteUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mocktest_id", referencedColumnName = "mocktest_id")
    private MocktestQuestion mocktest;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private SessionStatus status = SessionStatus.IN_PROGRESS;

    @CreationTimestamp
    private LocalDateTime createdAt;

    /* 문제별 평가 결과 (임시 저장) */
    @Column(name = "assessment_question1", columnDefinition = "nvarchar(max)")
    private String assessmentQuestion1;

    @Column(name = "assessment_question2", columnDefinition = "nvarchar(max)")
    private String assessmentQuestion2;

    @Column(name = "assessment_question3", columnDefinition = "nvarchar(max)")
    private String assessmentQuestion3;

    @Column(name = "assessment_question4", columnDefinition = "nvarchar(max)")
    private String assessmentQuestion4;

    @Column(name = "assessment_question5", columnDefinition = "nvarchar(max)")
    private String assessmentQuestion5;

    @Column(name = "assessment_question6", columnDefinition = "nvarchar(max)")
    private String assessmentQuestion6;

    @Column(name = "assessment_question7", columnDefinition = "nvarchar(max)")
    private String assessmentQuestion7;

    @Column(name = "assessment_question8", columnDefinition = "nvarchar(max)")
    private String assessmentQuestion8;

    @Column(name = "assessment_question9", columnDefinition = "nvarchar(max)")
    private String assessmentQuestion9;

    @Column(name = "assessment_question10", columnDefinition = "nvarchar(max)")
    private String assessmentQuestion10;

    @Column(name = "assessment_question11", columnDefinition = "nvarchar(max)")
    private String assessmentQuestion11;

    @Column(nullable = false)
    private Instant lastActivityAt;

    @PrePersist @PreUpdate
    public void touch() {
        this.lastActivityAt = Instant.now();
    }
}