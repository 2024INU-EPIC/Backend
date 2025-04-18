package com.example.epic.Assessment;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "test_grade")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class TestGrade {

    @Id
    @Column(name = "assessment_test_id")    // PK = FK 1:1
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "assessment_test_id",
            referencedColumnName = "assessment_mocktest_id")
    private AssessmentMocktest assessment;      // 1:1

    @Column(nullable = false)  // 시험 종료 즉시 삽입
    private LocalDateTime testDate;

    /* 파트별 성적 */
    private Float part1Grade;
    private Float part2Grade;
    private Float part3Grade;
    private Float part4Grade;
    private Float part5Grade;

    private String testGrade;  // “A/B/C …” 전체 등급
}

