package com.example.epic.stats;

import com.example.epic.user.SiteUser;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "learning_statistics")
@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
public class LearningStatistics {

    @Id
    @Column(name = "user_id")    // PK = FK 패턴
    private Long userId;

    /** 1:1 연결 (SiteUser ←→ LearningStatistics) */
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId                                 // PK를 FK로 재사용
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private SiteUser user;

    /* 파트별 누적 점수(%) */
    @Column(precision = 5, scale = 2) private Double statisticsPart1;
    @Column(precision = 5, scale = 2) private Double statisticsPart2;
    @Column(precision = 5, scale = 2) private Double statisticsPart3;
    @Column(precision = 5, scale = 2) private Double statisticsPart4;
    @Column(precision = 5, scale = 2) private Double statisticsPart5;
}
