package com.sidutti.charlie.aipipeline.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.Map;

@Document(indexName = "relationship")
public record Relationship(@Id String id,
                           String type,
                           String from,
                           String to,
                           String documentId,
                           Map<String, Object> attributes) {
}
