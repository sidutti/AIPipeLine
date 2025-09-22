package com.siduuti.aipipeline.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.Map;

@Document(indexName = "entity")
public record EntityNode(@Id
                         String id,
                         String documentId,
                         String type,
                         String name,
                         String domain,
                         Map<String, Object> attributes) {

}
