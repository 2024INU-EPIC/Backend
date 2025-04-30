package com.example.epic.Question;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "question_part5", schema = "epic")
public class QuestionPart5 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Setter
    @Column(name = "question_part5_id")
    private Long questionPart5Id;

    @Getter
    @Setter
    @Column(name = "question_11", nullable = false)
    private String question11;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // 기본 생성자
    public QuestionPart5() {
    }
}