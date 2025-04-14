package com.siduuti.aipipeline.dto.repository;

import com.siduuti.aipipeline.dto.Relationship;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface RelationShipRepository extends ReactiveMongoRepository<Relationship, String> {
    Flux<Relationship> findByFrom(String fromEntityId);

    Flux<Relationship> findByTo(String toEntityId);

}
