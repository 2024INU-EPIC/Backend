package com.example.epic.Assessment;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(schema = "epic", name = "test_grade")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class TestGrade {

    @Id
    @Column(name = "assessment_test_id") // PK = FK 1:1
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "assessment_test_id", referencedColumnName = "assessment_mocktest_id")
    private AssessmentMocktest assessment; // 1:1 매핑

    @Column(nullable = true)
    private LocalDateTime testDate;

    /* 파트별 성적 */
    @Column(precision = 5, scale = 2)
    private Float part1Grade;

    @Column(precision = 5, scale = 2)
    private Float part2Grade;

    @Column(precision = 5, scale = 2)
    private Float part3Grade;

    @Column(precision = 5, scale = 2)
    private Float part4Grade;

    @Column(precision = 5, scale = 2)
    private Float part5Grade;

    @Column(length = 50)
    private String testGrade;  // 전체 등급 문자열 ("140 IH" 등)
}