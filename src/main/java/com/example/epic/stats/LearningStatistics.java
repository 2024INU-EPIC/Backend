package com.example.epic.stats;

import com.example.epic.user.SiteUser;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(schema = "epic", name = "learning_statistics")
@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
public class LearningStatistics {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private SiteUser user;

    /* 파트별 누적 점수(%) */
    @Column(precision = 5, scale = 2)
    private Double statisticsPart1;

    @Column(precision = 5, scale = 2)
    private Double statisticsPart2;

    @Column(precision = 5, scale = 2)
    private Double statisticsPart3;

    @Column(precision = 5, scale = 2)
    private Double statisticsPart4;

    @Column(precision = 5, scale = 2)
    private Double statisticsPart5;

    /* 추가된 누적 통계 정보 */
    @Column(columnDefinition = "int default 0")
    private Integer totalTests;

    @Column(length = 16)
    private String lastGrade;

    private LocalDateTime lastTestedAt;
}