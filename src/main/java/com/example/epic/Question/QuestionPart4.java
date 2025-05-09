package com.example.epic.Question;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "question_part4", schema = "epic")
public class QuestionPart4 {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Setter
    @Column(name = "question_part4_id")
    private Long questionPart4Id;

    @Getter
    @Setter
    @Column(name = "question_situation_image")
    private String situationImage;

    @Getter
    @Setter
    @Column(name = "question_situation_text")
    private String situationText;

    @Getter
    @Setter
    @Column(name = "question_8")
    private String question8;

    @Getter
    @Setter
    @Column(name = "question_9")
    private String question9;

    @Getter
    @Setter
    @Column(name = "question_10")
    private String question10;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // 기본 생성자
    public QuestionPart4() {
    }
}
