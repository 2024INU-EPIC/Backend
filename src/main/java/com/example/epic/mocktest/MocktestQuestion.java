package com.example.epic.mocktest;

import com.example.epic.Question.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "mocktest_question", schema = "epic")
@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
public class MocktestQuestion {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter
    @Column(name = "mocktest_id")
    private Long id;

    /* 각 Part 문제 FK */
    @ManyToOne(fetch = FetchType.LAZY)   // 모두 not‑null
    @JoinColumn(name = "question_part1_id",
            referencedColumnName = "question_part1_id")
    private QuestionPart1 part1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_part2_id",
            referencedColumnName = "question_part2_id")
    private QuestionPart2 part2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_part3_id",
            referencedColumnName = "question_part3_id")
    private QuestionPart3 part3;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_part4_id",
            referencedColumnName = "question_part4_id")
    private QuestionPart4 part4;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_part5_id",
            referencedColumnName = "question_part5_id")
    private QuestionPart5 part5;

    @CreationTimestamp private LocalDateTime createdAt;
    @UpdateTimestamp private LocalDateTime updatedAt;
}
