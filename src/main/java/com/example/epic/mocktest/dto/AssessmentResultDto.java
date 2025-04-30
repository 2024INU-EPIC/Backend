package com.example.epic.mocktest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AssessmentResultDto {
    private final int part;
    private final int qNo;
    private final String evaluationJson;
}