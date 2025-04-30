package com.example.epic.mocktest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class CompletedSessionDto {
    private final UUID sessionId;
    private final List<String> assessmentJsons;
    private final TestGradeDto grade;
}
