package com.sidutti.charlie.aipipeline.dto.tutor;

public record AnswerResponseDto(
        boolean isCorrect,
        String correctAnswer,
        String explanation,
        int score,
        String feedback,
        boolean nextQuestionAvailable
) {}