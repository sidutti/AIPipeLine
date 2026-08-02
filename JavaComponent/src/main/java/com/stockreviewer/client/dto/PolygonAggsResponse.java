package com.stockreviewer.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PolygonAggsResponse(
    String status,
    String ticker,
    int queryCount,
    int resultsCount,
    boolean adjusted,
    List<Bar> results
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Bar(
        @JsonProperty("c") Double close,
        @JsonProperty("o") Double open,
        @JsonProperty("h") Double high,
        @JsonProperty("l") Double low,
        @JsonProperty("v") Double volume,
        @JsonProperty("t") Long timestamp,
        @JsonProperty("n") Integer transactionCount
    ) {}
}
