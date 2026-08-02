package com.stockreviewer.model;


import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;
import java.util.List;


@Document(indexName = "signals")
public record StockSignal(
    @Id
    String id,

    @Field(type = FieldType.Keyword)
    String ticker,

    @Field(type = FieldType.Keyword)
    String signalType, // BUY, SELL, HOLD

    @Field(type = FieldType.Double)
    Double price,

    @Field(type = FieldType.Date)
    Instant timestamp,

    @Field(type = FieldType.Text)
    List<String> reasons,

    @Field(type = FieldType.Double)
    Double rsi,

    @Field(type = FieldType.Double)
    Double sma50,

    @Field(type = FieldType.Double)
    Double sma200,

    @Field(type = FieldType.Double)
    Double peRatio,

    @Field(type = FieldType.Double)
    Double revenueGrowthYoY,

    @Field(type = FieldType.Double)
    Double priceChangePercent
) {
    // Standard getters for backward compatibility
    public String getId() {
        return id;
    }

    public String getTicker() {
        return ticker;
    }

    public String getSignalType() {
        return signalType;
    }

    public Double getPrice() {
        return price;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public Double getRsi() {
        return rsi;
    }

    public Double getSma50() {
        return sma50;
    }

    public Double getSma200() {
        return sma200;
    }

    public Double getPeRatio() {
        return peRatio;
    }

    public Double getRevenueGrowthYoY() {
        return revenueGrowthYoY;
    }

    public Double getPriceChangePercent() {
        return priceChangePercent;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String ticker;
        private String signalType;
        private Double price;
        private Instant timestamp;
        private List<String> reasons;
        private Double rsi;
        private Double sma50;
        private Double sma200;
        private Double peRatio;
        private Double revenueGrowthYoY;
        private Double priceChangePercent;

        public Builder id(String id) { this.id = id; return this; }
        public Builder ticker(String ticker) { this.ticker = ticker; return this; }
        public Builder signalType(String signalType) { this.signalType = signalType; return this; }
        public Builder price(Double price) { this.price = price; return this; }
        public Builder timestamp(Instant timestamp) { this.timestamp = timestamp; return this; }
        public Builder reasons(List<String> reasons) { this.reasons = reasons; return this; }
        public Builder rsi(Double rsi) { this.rsi = rsi; return this; }
        public Builder sma50(Double sma50) { this.sma50 = sma50; return this; }
        public Builder sma200(Double sma200) { this.sma200 = sma200; return this; }
        public Builder peRatio(Double peRatio) { this.peRatio = peRatio; return this; }
        public Builder revenueGrowthYoY(Double revenueGrowthYoY) { this.revenueGrowthYoY = revenueGrowthYoY; return this; }
        public Builder priceChangePercent(Double priceChangePercent) { this.priceChangePercent = priceChangePercent; return this; }

        public StockSignal build() {
            return new StockSignal(id, ticker, signalType, price, timestamp, reasons, rsi, sma50, sma200, peRatio, revenueGrowthYoY, priceChangePercent);
        }
    }
}

