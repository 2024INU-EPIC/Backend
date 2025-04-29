package com.example.epic.mocktest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.UUID;

@Getter @AllArgsConstructor
public class MocktestPayloadDto {
    private final UUID sessionId;
    private final Long mocktestId;
    private final PartDto part1;
    private final PartDto part2;
    private final PartDto part3;
    private final PartDto part4;
    private final PartDto part5;
}