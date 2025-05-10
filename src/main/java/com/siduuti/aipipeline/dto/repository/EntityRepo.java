package com.siduuti.aipipeline.dto.repository;

import com.siduuti.aipipeline.dto.Entities;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface EntityRepo extends ReactiveMongoRepository<Entities, String> {

    @Query("{ 'dynamic_properties': { $elemMatch: { 'key': ?0, 'value': ?1 } } }")
    Flux<Entities> findByDynamicProperty(String key, String value);
    Flux<Entities> findByDocumentId(String documentId);
    Flux<Entities> findBySku(String sku);
    Flux<Entities> findByChunkId(String chunkId);

}
