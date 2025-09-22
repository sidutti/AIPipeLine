package com.sidutti.charlie.aipipeline.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

/**
 * Represents an entity document stored in Elasticsearch.
 */
@Document(indexName = "entities")
public record Entities(@Id String id,

                       @Field(type = FieldType.Keyword)
                       String documentId,

                       @Field(type = FieldType.Keyword)
                       String chunkId,

                       @Field(type = FieldType.Keyword)
                       String sku,

                       // Mapped as a Nested object to allow querying on k and v pairs independently.
                       // This is the Elasticsearch equivalent of the MongoDB compound index.
                       @Field(type = FieldType.Nested, includeInParent = true)
                       List<PropertyEntry> dynamicProperties) {

    // The default record toString() is often sufficient, but this is customized for clarity.
    @Override
    public String toString() {
        return "Entities{" +
                "id='" + id + '\'' +
                ", documentId='" + documentId + '\'' +
                ", chunkId='" + chunkId + '\'' +
                ", sku='" + sku + '\'' +
                ", dynamicProperties=" + dynamicProperties +
                '}';
    }
}

/**
 * Represents a single key-value pair within the nested dynamicProperties field.
 * The fields are mapped here to define how they are indexed.
 */
record PropertyEntry(
        // 'k' (key) is a keyword for exact matching, filtering, and aggregations.
        @Field(type = FieldType.Keyword)
        String k,

        // 'v' (value) is text to allow for full-text search.
        @Field(type = FieldType.Text)
        String v
) {
}