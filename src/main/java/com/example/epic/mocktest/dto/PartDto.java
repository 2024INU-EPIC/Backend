package com.example.epic.mocktest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter @AllArgsConstructor
public class PartDto {
    private final String situationImage;
    private final String situationText;
    private final List<String> questions;
}