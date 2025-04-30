package com.example.epic.Assessment;

import com.example.epic.mocktest.MocktestQuestion;
import com.example.epic.user.SiteUser;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity @Table(schema = "epic", name = "assessment_mocktest")
@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
public class AssessmentMocktest {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assessment_mocktest_id")
    private Long id;

    /* ========= FK ========= */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private SiteUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mocktest_id", referencedColumnName = "mocktest_id")
    private MocktestQuestion mocktest;

    /* ========= 채점 결과 ========= */
    @Column(name = "assessment_question_1", columnDefinition = "nvarchar(max)")
    private String q1;
    @Column(name = "assessment_question_2", columnDefinition = "nvarchar(max)")
    private String q2;
    @Column(name = "assessment_question_3", columnDefinition = "nvarchar(max)")
    private String q3;
    @Column(name = "assessment_question_4", columnDefinition = "nvarchar(max)")
    private String q4;
    @Column(name = "assessment_question_5", columnDefinition = "nvarchar(max)")
    private String q5;
    @Column(name = "assessment_question_6", columnDefinition = "nvarchar(max)")
    private String q6;
    @Column(name = "assessment_question_7", columnDefinition = "nvarchar(max)")
    private String q7;
    @Column(name = "assessment_question_8", columnDefinition = "nvarchar(max)")
    private String q8;
    @Column(name = "assessment_question_9", columnDefinition = "nvarchar(max)")
    private String q9;
    @Column(name = "assessment_question_10", columnDefinition = "nvarchar(max)")
    private String q10;
    @Column(name = "assessment_question_11", columnDefinition = "nvarchar(max)")
    private String q11;

    /* ========= 공통 ========= */
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
