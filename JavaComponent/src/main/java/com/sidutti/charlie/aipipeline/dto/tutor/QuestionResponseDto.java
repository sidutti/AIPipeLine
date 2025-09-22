package com.sidutti.charlie.aipipeline.dto.tutor;

import java.util.List;

public record QuestionResponseDto(
        String question,
        String questionId,
        ChatMessage.QuestionType questionType,
        List<String> options,
        String correctAnswer,
        String explanation,
        int difficulty,
        String topic
) {}