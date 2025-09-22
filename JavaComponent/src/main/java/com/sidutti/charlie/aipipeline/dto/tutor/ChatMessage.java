package com.sidutti.charlie.aipipeline.dto.tutor;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.time.LocalDateTime;

@Document(indexName = "chat_message")
public record ChatMessage(
        @Id String id,
        String sessionId,
        String message,
        boolean isUser,
        LocalDateTime timestamp,
        ChatSession.Subject subject,
        QuestionType questionType,
        Integer difficulty
) {
    public enum QuestionType {
        multiple_choice, open_ended, calculation
    }
}