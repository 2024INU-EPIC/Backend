package com.example.epic.Question;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "question_part1", schema = "epic")
public class QuestionPart1 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Setter
    @Column(name = "question_part1_id")
    private Long questionPart1Id;

    @Getter
    @Setter
    @Column(name = "question_1", nullable = false)
    private String question1;

    @Getter
    @Setter
    @Column(name = "question_2", nullable = false)
    private String question2;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
    // 기본 생성자
    public QuestionPart1() {
    }
}