package com.siduuti.aipipeline.dto.repository;

import com.siduuti.aipipeline.dto.TargetDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface DocumentRepository extends ReactiveMongoRepository<TargetDocument, String> {
    
    Flux<TargetDocument> findByProcessingStatus(TargetDocument.ProcessingStatus status);
    
    Mono<TargetDocument> findByFileName(String fileName);
    
    Flux<TargetDocument> findByClusterId(String clusterId);
    
    Mono<Long> countByProcessingStatus(TargetDocument.ProcessingStatus status);
}