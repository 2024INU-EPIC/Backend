package com.example.epic.Question;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "question_part2", schema = "epic")
public class QuestionPart2 {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_part2_id")
    private Long questionPart2Id;

    @Getter
    @Setter
    @Column(name = "question_3")
    private String question3;

    @Getter
    @Setter
    @Column(name = "question_4")
    private String question4;

    @Getter
    @Setter
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Getter
    @Setter
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 기본 생성자
    public QuestionPart2() {
    }
}
