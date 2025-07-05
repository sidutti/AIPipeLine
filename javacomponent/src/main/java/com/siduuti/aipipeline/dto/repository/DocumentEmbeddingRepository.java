package com.siduuti.aipipeline.dto.repository;


import com.siduuti.aipipeline.dto.DocumentEmbedding;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface DocumentEmbeddingRepository extends ReactiveElasticsearchRepository<DocumentEmbedding, String> {
    
    Mono<DocumentEmbedding> findByDocumentId(String documentId);
    
    Flux<DocumentEmbedding> findByClusterId(String clusterId);
    
    Mono<Void> deleteByDocumentId(String documentId);
}