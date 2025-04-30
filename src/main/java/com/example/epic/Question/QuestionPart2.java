package com.example.epic.Question;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "question_part2", schema = "epic")
public class QuestionPart2 {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Setter
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

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // 기본 생성자
    public QuestionPart2() {
    }
}
