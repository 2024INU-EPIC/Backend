package com.example.epic.Assessment;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(schema = "epic", name = "test_grade")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class TestGrade {

    @Id
    @Column(name = "assessment_test_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "assessment_test_id", referencedColumnName = "assessment_mocktest_id")
    private AssessmentMocktest assessment;

    @Column(nullable = true)
    private LocalDateTime testDate;

    /* 파트별 성적 */
    @Column(name = "part1_grade", precision = 5, scale = 2)
    private Float part1Grade;

    @Column(name = "part2_grade", precision = 5, scale = 2)
    private Float part2Grade;

    @Column(name = "part3_grade", precision = 5, scale = 2)
    private Float part3Grade;

    @Column(name = "part4_grade", precision = 5, scale = 2)
    private Float part4Grade;

    @Column(name = "part5_grade", precision = 5, scale = 2)
    private Float part5Grade;

    @Column(length = 50)
    private String testGrade;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}