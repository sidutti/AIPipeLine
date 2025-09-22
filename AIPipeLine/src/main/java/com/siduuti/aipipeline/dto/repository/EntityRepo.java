package com.siduuti.aipipeline.dto.repository;

import com.siduuti.aipipeline.dto.Entities;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import reactor.core.publisher.Flux;

public interface EntityRepo extends ReactiveElasticsearchRepository<Entities, String> {


    Flux<Entities> findByDocumentId(String documentId);
    Flux<Entities> findBySku(String sku);
    Flux<Entities> findByChunkId(String chunkId);

}
