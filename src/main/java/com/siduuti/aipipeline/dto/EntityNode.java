package com.siduuti.aipipeline.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Document(collection = "entities")
public record EntityNode(@Id
                         String id,
                         String documentId,
                         String type,
                         String name,
                         String domain,
                         Map<String, Object> attributes) {

}
