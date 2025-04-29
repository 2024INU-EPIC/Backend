package com.example.epic.mocktest;

import com.example.epic.mocktest.MocktestQuestion;
import com.example.epic.mocktest.MocktestQuestionRepository;
import com.example.epic.mocktest.dto.PartDto;
import com.example.epic.mocktest.dto.MocktestPayloadDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MocktestQuestionService {
    private final MocktestQuestionRepository repository;

    public MocktestPayloadDto getPayload(UUID sessionId, Long mocktestId) {
        MocktestQuestion mq = repository.findById(mocktestId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "MocktestQuestion not found: " + mocktestId));

        // Part1 (2문항)
        var p1 = mq.getPart1();
        PartDto part1 = new PartDto(
                null,
                null,
                List.of(p1.getQuestion1(), p1.getQuestion2())
        );

        // Part2 (2문항)
        var p2 = mq.getPart2();
        PartDto part2 = new PartDto(
                null,
                null,
                List.of(p2.getQuestion3(), p2.getQuestion4())
        );

        // Part3 (3문항)
        var p3 = mq.getPart3();
        PartDto part3 = new PartDto(
                null,
                p3.getSituationText(),
                List.of(p3.getQuestion5(), p3.getQuestion6(), p3.getQuestion7())
        );

        // Part4 (3문항)
        var p4 = mq.getPart4();
        PartDto part4 = new PartDto(
                p4.getSituationImage(),
                p4.getSituationText(),
                List.of(p4.getQuestion8(), p4.getQuestion9(), p4.getQuestion10())
        );

        // Part5 (1문항)
        var p5 = mq.getPart5();
        PartDto part5 = new PartDto(
                null,
                null,
                List.of(p5.getQuestion11())
        );

        return new MocktestPayloadDto(
                sessionId,
                mq.getId(),
                part1, part2, part3, part4, part5
        );
    }
}
