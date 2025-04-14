package com.siduuti.aipipeline.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Document(collection = "relationships")
public record Relationship(@Id String id,
                           String type,
                           String from,
                           String to,
                           String documentId,
                           Map<String, Object> attributes) {
}
