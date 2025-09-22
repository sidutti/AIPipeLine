package com.sidutti.charlie.aipipeline.dto.tutor.repository;

import com.sidutti.charlie.aipipeline.dto.tutor.ChatSession;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import reactor.core.publisher.Flux;

public interface ChatSessionRepository extends ReactiveElasticsearchRepository<ChatSession, String> {
    Flux<ChatSession> findByStudentNameOrderByStartTimeDesc(String studentName);
    Flux<ChatSession> findByStatus(ChatSession.SessionStatus status);
    Flux<ChatSession> findByStudentNameAndStatus(String studentName, ChatSession.SessionStatus status);
}