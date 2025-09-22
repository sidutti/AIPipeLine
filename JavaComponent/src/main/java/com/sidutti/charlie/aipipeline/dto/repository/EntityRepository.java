package com.sidutti.charlie.aipipeline.dto.repository;

import com.sidutti.charlie.aipipeline.dto.EntityNode;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import reactor.core.publisher.Flux;

public interface EntityRepository extends ReactiveElasticsearchRepository<EntityNode, String> {
    Flux<EntityNode> findByDocumentId(String documentId);

    Flux<EntityNode> findByDomain(String domain);




}
