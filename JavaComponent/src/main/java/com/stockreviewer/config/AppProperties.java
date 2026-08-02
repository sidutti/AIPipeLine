package com.stockreviewer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.scan")
public record AppProperties(
    String cron,
    @DefaultValue Rules rules
) {
    public AppProperties {
        if (rules == null) {
            rules = new Rules(14, 30.0, 70.0, 50, 200, 0.0, 25.0, 0.10, 5.0, 10.0);
        }
    }

    public String getCron() {
        return cron;
    }

    public Rules getRules() {
        return rules;
    }

    public record Rules(
        @DefaultValue("14") int rsiPeriod,
        @DefaultValue("30.0") double rsiLow,
        @DefaultValue("70.0") double rsiHigh,
        @DefaultValue("50") int smaShortPeriod,
        @DefaultValue("200") int smaLongPeriod,
        @DefaultValue("0.0") double minPeRatio,
        @DefaultValue("25.0") double maxPeRatio,
        @DefaultValue("0.10") double minRevenueGrowthYoY,
        @DefaultValue("5.0") double priceDropPercentage,
        @DefaultValue("10.0") double priceGainPercentage
    ) {
        public int getRsiPeriod() { return rsiPeriod; }
        public double getRsiLow() { return rsiLow; }
        public double getRsiHigh() { return rsiHigh; }
        public int getSmaShortPeriod() { return smaShortPeriod; }
        public int getSmaLongPeriod() { return smaLongPeriod; }
        public double getMinPeRatio() { return minPeRatio; }
        public double getMaxPeRatio() { return maxPeRatio; }
        public double getMinRevenueGrowthYoY() { return minRevenueGrowthYoY; }
        public double getPriceDropPercentage() { return priceDropPercentage; }
        public double getPriceGainPercentage() { return priceGainPercentage; }
    }
}
