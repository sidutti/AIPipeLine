package com.sidutti.charlie.aipipeline.dto.repository;

import com.sidutti.charlie.aipipeline.dto.Relationship;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import reactor.core.publisher.Flux;

public interface RelationShipRepository extends ReactiveElasticsearchRepository<Relationship, String> {
    Flux<Relationship> findByFrom(String fromEntityId);

    Flux<Relationship> findByTo(String toEntityId);

}
