package com.sidutti.charlie.aipipeline.dto.repository;


import com.sidutti.charlie.aipipeline.dto.DocumentEmbedding;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface DocumentEmbeddingRepository extends ReactiveElasticsearchRepository<DocumentEmbedding, String> {

    Mono<DocumentEmbedding> findByDocumentId(String documentId);

    Flux<DocumentEmbedding> findByClusterId(String clusterId);

    Mono<Void> deleteByDocumentId(String documentId);

    Flux<DocumentEmbedding> findAllByDocumentIdIn(List<String> documentIds);
}