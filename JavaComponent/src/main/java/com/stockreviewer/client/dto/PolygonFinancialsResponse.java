package com.stockreviewer.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PolygonFinancialsResponse(
    String status,
    List<FinancialResult> results
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FinancialResult(
        String id,
        @JsonProperty("ticker") String ticker,
        @JsonProperty("start_date") String startDate,
        @JsonProperty("end_date") String endDate,
        String timeframe,
        @JsonProperty("fiscal_period") String fiscalPeriod,
        @JsonProperty("fiscal_year") String fiscalYear,
        Financials financials
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Financials(
        @JsonProperty("income_statement") IncomeStatement incomeStatement
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record IncomeStatement(
        FinancialValue revenues,
        @JsonProperty("net_income_loss") FinancialValue netIncomeLoss,
        @JsonProperty("basic_earnings_per_share") FinancialValue basicEarningsPerShare,
        @JsonProperty("diluted_earnings_per_share") FinancialValue dilutedEarningsPerShare
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FinancialValue(
        Double value,
        String unit,
        String label
    ) {}
}
