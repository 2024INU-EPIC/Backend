package com.example.epic.Question;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "question_part3", schema = "epic")
public class QuestionPart3 {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Setter
    @Column(name = "question_part3_id")
    private Long questionPart3Id;

    @Getter
    @Setter
    @Column(name = "question_situation_text")
    private String situationText;

    @Getter
    @Setter
    @Column(name = "question_5")
    private String question5;

    @Getter
    @Setter
    @Column(name = "question_6")
    private String question6;

    @Getter
    @Setter
    @Column(name = "question_7")
    private String question7;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 기본 생성자
    public QuestionPart3() {
    }
}
