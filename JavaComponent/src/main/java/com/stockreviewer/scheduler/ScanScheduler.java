package com.stockreviewer.scheduler;

import com.stockreviewer.service.StockScannerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScanScheduler {

    private static final Logger log = LoggerFactory.getLogger(ScanScheduler.class);

    private final StockScannerService stockScannerService;

    public ScanScheduler(StockScannerService stockScannerService) {
        this.stockScannerService = stockScannerService;
    }

    @Scheduled(cron = "${app.scan.cron}")
    public void scheduleStockScan() {
        log.info("Triggering scheduled stock market scan...");
        stockScannerService.scanAll()
                .subscribe(
                        signal -> log.debug("Processed signal for ticker: {}", signal.getTicker()),
                        error -> log.error("Error occurred during scheduled stock market scan", error)
                );
    }
}
