package com.siduuti.aipipeline.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Document("entities")
@CompoundIndex(name = "dynamic_props_idx", def = "{'dynamic_properties.k': 1, 'dynamic_properties.v': 1}")
public record Entities(@Id String id,
                       @Indexed String documentId,
                       @Indexed String chunkId,
                       String sku,
                       @Field("dynamic_properties")
                                   List<PropertyEntry> dynamicProperties) {

    @Override
    public String toString() {
        return "ProductWithProperties{" +
                "id='" + id + '\'' +
                ", sku='" + sku + '\'' +
                ", dynamicProperties=" + dynamicProperties +
                '}';
    }
}

// Represents a single key-value pair
record PropertyEntry(String k, String v) {

}

