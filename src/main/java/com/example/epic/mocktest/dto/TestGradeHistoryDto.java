package com.example.epic.mocktest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter @AllArgsConstructor
public class TestGradeHistoryDto {
    private final Long gradeId;
    private final LocalDateTime testDate;
    private final float part1Grade;
    private final float part2Grade;
    private final float part3Grade;
    private final float part4Grade;
    private final float part5Grade;
    private final String testGrade;
}