package com.stockreviewer.controller;

import com.stockreviewer.model.StockSignal;
import com.stockreviewer.repository.SignalRepository;
import com.stockreviewer.service.StockScannerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/scan")
public class ScanController {

    private static final Logger log = LoggerFactory.getLogger(ScanController.class);

    private final StockScannerService stockScannerService;
    private final SignalRepository signalRepository;

    public ScanController(StockScannerService stockScannerService, SignalRepository signalRepository) {
        this.stockScannerService = stockScannerService;
        this.signalRepository = signalRepository;
    }

    @PostMapping("/trigger")
    public Flux<StockSignal> triggerScan() {
        log.info("REST request to trigger stock market scan manually.");
        return stockScannerService.scanAll();
    }

    @GetMapping("/signals")
    public Flux<StockSignal> getAllSignals() {
        log.info("REST request to fetch all stored stock signals.");
        return signalRepository.findAllByOrderByTimestampDesc();
    }

    @GetMapping("/signals/{ticker}")
    public Flux<StockSignal> getSignalsForTicker(@PathVariable String ticker) {
        String cleanedTicker = ticker.trim().toUpperCase();
        log.info("REST request to fetch stock signals for ticker: {}", cleanedTicker);
        return signalRepository.findByTickerOrderByTimestampDesc(cleanedTicker);
    }
}
