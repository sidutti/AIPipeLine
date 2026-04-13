package com.sidutti.charlie.aipipeline.dto.tutor.repository;

import com.sidutti.charlie.aipipeline.dto.tutor.ChatMessage;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import reactor.core.publisher.Flux;

public interface ChatMessageRepository extends ReactiveElasticsearchRepository<ChatMessage, String> {
    Flux<ChatMessage> findBySessionIdOrderByTimestampAsc(String sessionId);

    Flux<ChatMessage> findBySessionIdAndIsUserOrderByTimestampAsc(String sessionId, boolean isUser);
}