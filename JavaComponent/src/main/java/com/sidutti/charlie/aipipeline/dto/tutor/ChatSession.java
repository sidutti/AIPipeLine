package com.sidutti.charlie.aipipeline.dto.tutor;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(indexName = "chat_session")
public record ChatSession(
        @Id String id,
        String studentName,
        Subject subject,
        int grade,
        LocalDateTime startTime,
        LocalDateTime endTime,
        List<ChatMessage> messages,
        Double score,
        Integer totalQuestions,
        Integer correctAnswers,
        SessionStatus status
) {
    public enum Subject {
        math, physics
    }

    public enum SessionStatus {
        active, completed, paused
    }
}