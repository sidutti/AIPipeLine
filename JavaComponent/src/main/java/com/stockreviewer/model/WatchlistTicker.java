package com.stockreviewer.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;

@Document(indexName = "watchlist")
public record WatchlistTicker(
    @Id
    String ticker,

    @Field(type = FieldType.Date)
    Instant addedAt
) {
    public String getTicker() {
        return ticker;
    }

    public Instant getAddedAt() {
        return addedAt;
    }
}
