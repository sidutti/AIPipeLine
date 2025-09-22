package com.sidutti.charlie.aipipeline.dto.tutor;

import java.time.LocalDateTime;
import java.util.List;

public record ChatSessionDto(
        String id,
        String studentName,
        ChatSession.Subject subject,
        int grade,
        LocalDateTime startTime,
        LocalDateTime endTime,
        List<ChatMessage> messages,
        Double score,
        Integer totalQuestions,
        Integer correctAnswers,
        ChatSession.SessionStatus status
) {}