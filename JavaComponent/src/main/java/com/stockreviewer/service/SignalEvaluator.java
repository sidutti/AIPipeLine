package com.stockreviewer.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.stockreviewer.client.dto.PolygonFinancialsResponse;
import com.stockreviewer.config.AppProperties;
import com.stockreviewer.model.StockSignal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SignalEvaluator {

    private static final Logger log = LoggerFactory.getLogger(SignalEvaluator.class);

    private final IndicatorCalculator indicatorCalculator;
    private final AppProperties appProperties;

    public SignalEvaluator(IndicatorCalculator indicatorCalculator, AppProperties appProperties) {
        this.indicatorCalculator = indicatorCalculator;
        this.appProperties = appProperties;
    }

    public StockSignal evaluate(String ticker, List<Double> closingPrices, PolygonFinancialsResponse financials) {
        AppProperties.Rules rules = appProperties.getRules();

        if (closingPrices == null || closingPrices.size() < 2) {
            log.warn("Insufficient price data for ticker {} to calculate price changes.", ticker);
            return createHoldSignal(ticker, "Insufficient price data");
        }

        Double latestPrice = closingPrices.get(closingPrices.size() - 1);
        Double previousPrice = closingPrices.get(closingPrices.size() - 2);
        double priceChangePercent = ((latestPrice - previousPrice) / previousPrice) * 100.0;

        // 1. Calculate Technical Indicators
        Double rsi = indicatorCalculator.calculateRSI(closingPrices, rules.getRsiPeriod());
        Double sma50 = indicatorCalculator.calculateSMA(closingPrices, rules.getSmaShortPeriod());
        Double sma200 = indicatorCalculator.calculateSMA(closingPrices, rules.getSmaLongPeriod());

        // 2. Parse Fundamentals
        Double ttmEps = calculateTtmEps(financials);
        Double peRatio = null;
        if (ttmEps != null && ttmEps > 0) {
            peRatio = latestPrice / ttmEps;
        }

        Double revenueGrowthYoY = calculateYoYRevenueGrowth(financials);

        // 3. Evaluate Rules
        List<String> buyReasons = new ArrayList<>();
        List<String> sellReasons = new ArrayList<>();

        // --- Fundamental Filters for Buying ---
        boolean fundamentalsOk = true;
        if (peRatio == null) {
            fundamentalsOk = false;
            log.debug("Ticker {}: Fundamental check failed: PE ratio is unavailable or negative.", ticker);
        } else if (peRatio < rules.getMinPeRatio() || peRatio > rules.getMaxPeRatio()) {
            fundamentalsOk = false;
            log.debug("Ticker {}: Fundamental check failed: PE ratio ({}) is outside [{}, {}].", 
                    ticker, peRatio, rules.getMinPeRatio(), rules.getMaxPeRatio());
        }

        if (revenueGrowthYoY == null) {
            fundamentalsOk = false;
            log.debug("Ticker {}: Fundamental check failed: YoY Revenue growth is unavailable.", ticker);
        } else if (revenueGrowthYoY < rules.getMinRevenueGrowthYoY()) {
            fundamentalsOk = false;
            log.debug("Ticker {}: Fundamental check failed: YoY Revenue growth ({}) is below minimum ({}).", 
                    ticker, revenueGrowthYoY, rules.getMinRevenueGrowthYoY());
        }

        // --- Technical Triggers for Buying (only if fundamentals are healthy) ---
        if (fundamentalsOk) {
            if (rsi != null && rsi <= rules.getRsiLow()) {
                buyReasons.add(String.format("RSI is oversold at %.2f (threshold <= %.2f)", rsi, rules.getRsiLow()));
            }
            if (priceChangePercent <= -rules.getPriceDropPercentage()) {
                buyReasons.add(String.format("Daily price drop of %.2f%% meets dip-buying threshold (>= %.2f%% drop)", 
                        Math.abs(priceChangePercent), rules.getPriceDropPercentage()));
            }
            if (sma50 != null && sma200 != null && sma50 > sma200) {
                // Contributing factor for buy momentum
                buyReasons.add(String.format("Bullish trend: 50 SMA (%.2f) is above 200 SMA (%.2f)", sma50, sma200));
            }
        }

        // --- Technical Triggers for Selling (no fundamental check needed to exit/cut loss) ---
        if (rsi != null && rsi >= rules.getRsiHigh()) {
            sellReasons.add(String.format("RSI is overbought at %.2f (threshold >= %.2f)", rsi, rules.getRsiHigh()));
        }
        if (priceChangePercent >= rules.getPriceGainPercentage()) {
            sellReasons.add(String.format("Daily price gain of %.2f%% meets profit-taking threshold (>= %.2f%%)", 
                    priceChangePercent, rules.getPriceGainPercentage()));
        }
        if (sma50 != null && sma200 != null && sma50 < sma200) {
            sellReasons.add(String.format("Bearish trend: 50 SMA (%.2f) is below 200 SMA (%.2f)", sma50, sma200));
        }

        // 4. Build Resulting Signal
        String signalType = "HOLD";
        List<String> reasons = new ArrayList<>();

        if (!sellReasons.isEmpty()) {
            signalType = "SELL";
            reasons = sellReasons;
        } else if (!buyReasons.isEmpty() && fundamentalsOk) {
            signalType = "BUY";
            reasons = buyReasons;
        } else {
            reasons.add(String.format("No buy or sell triggers met. Fundamentals healthy: %s", fundamentalsOk));
        }

        return StockSignal.builder()
                .id(UUID.randomUUID().toString())
                .ticker(ticker)
                .signalType(signalType)
                .price(latestPrice)
                .timestamp(Instant.now())
                .reasons(reasons)
                .rsi(rsi)
                .sma50(sma50)
                .sma200(sma200)
                .peRatio(peRatio)
                .revenueGrowthYoY(revenueGrowthYoY)
                .priceChangePercent(priceChangePercent)
                .build();
    }

    private StockSignal createHoldSignal(String ticker, String reason) {
        return StockSignal.builder()
                .id(UUID.randomUUID().toString())
                .ticker(ticker)
                .signalType("HOLD")
                .price(0.0)
                .timestamp(Instant.now())
                .reasons(List.of(reason))
                .build();
    }

    /**
     * Calculates Trailing Twelve Months (TTM) EPS.
     * Looks for quarterly reports first, summing the last 4 quarters' EPS.
     * Fallbacks to the latest annual EPS if quarterly reports are unavailable.
     */
    public Double calculateTtmEps(PolygonFinancialsResponse response) {
        if (response == null || response.results() == null || response.results().isEmpty()) {
            return null;
        }

        // Filter for quarterly reports
        List<PolygonFinancialsResponse.FinancialResult> quarters = response.results().stream()
                .filter(r -> "quarterly".equalsIgnoreCase(r.timeframe()))
                .sorted((r1, r2) -> r2.endDate().compareTo(r1.endDate())) // Newest first
                .limit(4)
                .collect(Collectors.toList());

        if (quarters.size() == 4) {
            double sumEps = 0.0;
            boolean valid = true;
            for (PolygonFinancialsResponse.FinancialResult quarter : quarters) {
                Double eps = getEpsFromReport(quarter);
                if (eps != null) {
                    sumEps += eps;
                } else {
                    valid = false;
                    break;
                }
            }
            if (valid) {
                return sumEps;
            }
        }

        // Fallback to the latest annual report
        return response.results().stream()
                .filter(r -> "annual".equalsIgnoreCase(r.timeframe()))
                .max((r1, r2) -> r1.endDate().compareTo(r2.endDate())) // Latest
                .map(this::getEpsFromReport)
                .orElse(null);
    }

    private Double getEpsFromReport(PolygonFinancialsResponse.FinancialResult report) {
        if (report.financials() != null && report.financials().incomeStatement() != null) {
            PolygonFinancialsResponse.IncomeStatement income = report.financials().incomeStatement();
            if (income.basicEarningsPerShare() != null) {
                return income.basicEarningsPerShare().value();
            } else if (income.dilutedEarningsPerShare() != null) {
                return income.dilutedEarningsPerShare().value();
            }
        }
        return null;
    }

    /**
     * Calculates YoY revenue growth.
     * Compares the latest quarterly report to the same quarter of the previous year.
     * Fallbacks to comparing the latest annual report to the previous year's annual report.
     */
    public Double calculateYoYRevenueGrowth(PolygonFinancialsResponse response) {
        if (response == null || response.results() == null || response.results().isEmpty()) {
            return null;
        }

        // Get the latest report
        PolygonFinancialsResponse.FinancialResult latest = response.results().stream()
                .max((r1, r2) -> r1.endDate().compareTo(r2.endDate()))
                .orElse(null);

        if (latest == null) {
            return null;
        }

        Double latestRevenue = getRevenueFromReport(latest);
        if (latestRevenue == null || latestRevenue <= 0) {
            return null;
        }

        if ("quarterly".equalsIgnoreCase(latest.timeframe())) {
            // Find Q report from same fiscal period in previous year
            String targetPeriod = latest.fiscalPeriod();
            int targetYear = Integer.parseInt(latest.fiscalYear()) - 1;

            PolygonFinancialsResponse.FinancialResult prevQuarter = response.results().stream()
                    .filter(r -> "quarterly".equalsIgnoreCase(r.timeframe())
                            && targetPeriod.equalsIgnoreCase(r.fiscalPeriod())
                            && String.valueOf(targetYear).equals(r.fiscalYear()))
                    .findFirst()
                    .orElse(null);

            if (prevQuarter != null) {
                Double prevRevenue = getRevenueFromReport(prevQuarter);
                if (prevRevenue != null && prevRevenue > 0) {
                    return (latestRevenue - prevRevenue) / prevRevenue;
                }
            }
        }

        // Fallback or if annual: compare annual reports
        int targetYear = Integer.parseInt(latest.fiscalYear()) - 1;
        PolygonFinancialsResponse.FinancialResult prevAnnual = response.results().stream()
                .filter(r -> "annual".equalsIgnoreCase(r.timeframe())
                        && String.valueOf(targetYear).equals(r.fiscalYear()))
                .findFirst()
                .orElse(null);

        if (prevAnnual != null) {
            Double prevRevenue = getRevenueFromReport(prevAnnual);
            if (prevRevenue != null && prevRevenue > 0) {
                return (latestRevenue - prevRevenue) / prevRevenue;
            }
        }

        return null;
    }

    private Double getRevenueFromReport(PolygonFinancialsResponse.FinancialResult report) {
        if (report.financials() != null && report.financials().incomeStatement() != null) {
            PolygonFinancialsResponse.IncomeStatement income = report.financials().incomeStatement();
            if (income.revenues() != null) {
                return income.revenues().value();
            }
        }
        return null;
    }
}
