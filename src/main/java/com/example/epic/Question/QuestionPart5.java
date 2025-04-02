package com.example.epic.Question;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "question_part5", schema = "epic")
public class QuestionPart5 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_part5_id")
    private Long questionPart5Id;

    @Getter
    @Setter
    @Column(name = "question_11", nullable = false)
    private String question11;

    @Getter
    @Setter
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Getter
    @Setter
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 기본 생성자
    public QuestionPart5() {
    }
}