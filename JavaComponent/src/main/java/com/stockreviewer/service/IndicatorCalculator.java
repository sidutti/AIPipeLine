package com.stockreviewer.service;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IndicatorCalculator {

    /**
     * Calculates the Simple Moving Average (SMA) for a given period.
     * The prices list is assumed to be ordered chronologically (oldest to newest),
     * so the last element is the most recent price.
     *
     * @param prices list of closing prices ordered chronologically
     * @param period the window size
     * @return the SMA of the last 'period' elements, or null if insufficient data
     */
    public Double calculateSMA(List<Double> prices, int period) {
        if (prices == null || prices.size() < period || period <= 0) {
            return null;
        }

        double sum = 0.0;
        int startIndex = prices.size() - period;
        for (int i = startIndex; i < prices.size(); i++) {
            sum += prices.get(i);
        }
        return sum / period;
    }

    /**
     * Calculates the Relative Strength Index (RSI) using Wilder's smoothing technique.
     * The prices list is assumed to be ordered chronologically (oldest to newest).
     *
     * @param prices list of closing prices ordered chronologically
     * @param period the RSI period (typically 14)
     * @return the latest RSI value, or null if insufficient data (requires at least period + 1 prices)
     */
    public Double calculateRSI(List<Double> prices, int period) {
        if (prices == null || prices.size() <= period || period <= 0) {
            return null;
        }

        // We need at least period + 1 prices to calculate the first average gain/loss
        double avgGain = 0.0;
        double avgLoss = 0.0;

        // 1. Calculate the first average gain and average loss (simple average)
        for (int i = 1; i <= period; i++) {
            double change = prices.get(i) - prices.get(i - 1);
            if (change > 0) {
                avgGain += change;
            } else {
                avgLoss += Math.abs(change);
            }
        }
        avgGain /= period;
        avgLoss /= period;

        // 2. Smooth subsequent values using Wilder's smoothing
        for (int i = period + 1; i < prices.size(); i++) {
            double change = prices.get(i) - prices.get(i - 1);
            double currentGain = change > 0 ? change : 0.0;
            double currentLoss = change < 0 ? Math.abs(change) : 0.0;

            avgGain = (avgGain * (period - 1) + currentGain) / period;
            avgLoss = (avgLoss * (period - 1) + currentLoss) / period;
        }

        if (avgLoss == 0.0) {
            return 100.0;
        }

        double rs = avgGain / avgLoss;
        return 100.0 - (100.0 / (1.0 + rs));
    }
}
