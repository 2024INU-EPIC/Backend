package com.example.epic.mocktest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TestGradeDto {
    private final double part1Score;
    private final double part2Score;
    private final double part3Score;
    private final double part4Score;
    private final double part5Score;
    private final String finalGrade;
}
