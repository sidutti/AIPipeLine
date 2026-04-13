package com.sidutti.charlie.aipipeline.dto.tutor;

import java.util.List;

public record SessionSummaryDto(
        String sessionId,
        int totalQuestions,
        int correctAnswers,
        int score,
        int timeSpent,
        List<String> strengths,
        List<String> weaknesses,
        List<String> recommendations
) {
}