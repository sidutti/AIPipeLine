package com.stockreviewer.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.stockreviewer.client.PolygonClient;
import com.stockreviewer.client.dto.PolygonAggsResponse;
import com.stockreviewer.client.dto.PolygonFinancialsResponse;
import com.stockreviewer.model.StockSignal;
import com.stockreviewer.repository.SignalRepository;
import com.stockreviewer.repository.WatchlistRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class StockScannerService {

    private static final Logger log = LoggerFactory.getLogger(StockScannerService.class);

    private final WatchlistRepository watchlistRepository;
    private final SignalRepository signalRepository;

    private final PolygonClient polygonClient;
    private final SignalEvaluator signalEvaluator;
    private final NotificationService notificationService;

    public StockScannerService(WatchlistRepository watchlistRepository, SignalRepository signalRepository,
            PolygonClient polygonClient, SignalEvaluator signalEvaluator, NotificationService notificationService) {
        this.watchlistRepository = watchlistRepository;
        this.signalRepository = signalRepository;
        this.polygonClient = polygonClient;
        this.signalEvaluator = signalEvaluator;
        this.notificationService = notificationService;
    }

    public Flux<StockSignal> scanAll() {
        log.info("Starting stock market scan for all watchlist tickers...");

        LocalDate today = LocalDate.now();
        // Fetch 550 days (~1.5 years) of history to ensure we can calculate 200 SMA
        LocalDate from = today.minusDays(550);

        return watchlistRepository.findAll()
                .flatMap(watchlistTicker -> {
                    String ticker = watchlistTicker.ticker();
                    log.info("Scanning ticker: {}", ticker);

                    Mono<PolygonAggsResponse> priceHistoryMono = polygonClient.getDailyBars(ticker, from, today);
                    Mono<PolygonFinancialsResponse> financialsMono = polygonClient.getFinancials(ticker);

                    return Mono.zip(priceHistoryMono, financialsMono)
                            .flatMap(tuple -> {
                                PolygonAggsResponse priceHistory = tuple.getT1();
                                PolygonFinancialsResponse financials = tuple.getT2();

                                if (priceHistory == null || priceHistory.results() == null || priceHistory.results().isEmpty()) {
                                    log.warn("No price history returned for ticker {}. Skipping.", ticker);
                                    return Mono.empty();
                                }

                                List<Double> closingPrices = priceHistory.results().stream()
                                        .map(PolygonAggsResponse.Bar::close)
                                        .collect(Collectors.toList());

                                // Evaluate Signal
                                StockSignal signal = signalEvaluator.evaluate(ticker, closingPrices, financials);

                                // Save to DB and Notify
                                return signalRepository.save(signal)
                                        .flatMap(savedSignal -> 
                                            notificationService.notifySignal(savedSignal)
                                                    .thenReturn(savedSignal)
                                        );
                            })
                            .onErrorResume(e -> {
                                log.error("Failed to scan ticker: {} due to error", ticker, e);
                                return Mono.empty();
                            });
                })
                .doOnComplete(() -> log.info("Stock scan complete."));
    }
}
