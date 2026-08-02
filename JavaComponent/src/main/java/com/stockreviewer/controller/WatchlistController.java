package com.stockreviewer.controller;

import com.stockreviewer.model.WatchlistTicker;
import com.stockreviewer.repository.WatchlistRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Set;

@RestController
@RequestMapping("/api/watchlist")
public class WatchlistController {

    private static final Logger log = LoggerFactory.getLogger(WatchlistController.class);

    private final WatchlistRepository watchlistRepository;

    private static final Set<String> DEFAULT_TICKERS = Set.of("AAPL", "MSFT", "GOOGL", "AMZN", "TSLA", "NVDA", "META");

    public WatchlistController(WatchlistRepository watchlistRepository) {
        this.watchlistRepository = watchlistRepository;
    }

    @PostMapping("/{ticker}")
    public Mono<ResponseEntity<WatchlistTicker>> addTicker(@PathVariable String ticker) {
        String cleanedTicker = ticker.trim().toUpperCase();
        log.info("Adding ticker to watchlist: {}", cleanedTicker);
        
        WatchlistTicker watchlistTicker = new WatchlistTicker(cleanedTicker, Instant.now());
        return watchlistRepository.save(watchlistTicker)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{ticker}")
    public Mono<ResponseEntity<Void>> removeTicker(@PathVariable String ticker) {
        String cleanedTicker = ticker.trim().toUpperCase();
        log.info("Removing ticker from watchlist: {}", cleanedTicker);
        
        return watchlistRepository.existsById(cleanedTicker)
                .flatMap(exists -> {
                    if (exists) {
                        return watchlistRepository.deleteById(cleanedTicker)
                                .thenReturn(ResponseEntity.noContent().<Void>build());
                    } else {
                        return Mono.just(ResponseEntity.notFound().<Void>build());
                    }
                });
    }

    @GetMapping
    public Flux<WatchlistTicker> getWatchlist() {
        log.info("Fetching entire stock watchlist...");
        return watchlistRepository.findAll();
    }

    @PostMapping("/initialize")
    public Flux<WatchlistTicker> initializeDefaultWatchlist() {
        log.info("Initializing watchlist with default tickers: {}", DEFAULT_TICKERS);
        
        return Flux.fromIterable(DEFAULT_TICKERS)
                .flatMap(ticker -> watchlistRepository.existsById(ticker)
                        .flatMap(exists -> {
                            if (!exists) {
                                WatchlistTicker wt = new WatchlistTicker(ticker, Instant.now());
                                return watchlistRepository.save(wt);
                            }
                            return Mono.empty();
                        })
                );
    }
}
