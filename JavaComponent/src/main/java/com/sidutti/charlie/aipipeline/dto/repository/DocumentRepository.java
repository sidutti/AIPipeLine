package com.sidutti.charlie.aipipeline.dto.repository;

import com.sidutti.charlie.aipipeline.dto.TargetDocument;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DocumentRepository extends ReactiveElasticsearchRepository<TargetDocument, String> {

    Flux<TargetDocument> findByProcessingStatus(TargetDocument.ProcessingStatus status);

    Mono<TargetDocument> findByFileName(String fileName);

    Flux<TargetDocument> findByClusterId(String clusterId);

    Mono<Long> countByProcessingStatus(TargetDocument.ProcessingStatus status);

    Flux<TargetDocument> findTop500ByProcessingStatus(TargetDocument.ProcessingStatus processingStatus);
}