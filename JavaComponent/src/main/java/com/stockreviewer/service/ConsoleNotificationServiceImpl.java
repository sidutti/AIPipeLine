package com.stockreviewer.service;

import com.stockreviewer.model.StockSignal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ConsoleNotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(ConsoleNotificationServiceImpl.class);

    @Override
    public Mono<Void> notifySignal(StockSignal signal) {
        return Mono.fromRunnable(() -> {
            if ("BUY".equalsIgnoreCase(signal.getSignalType()) || "SELL".equalsIgnoreCase(signal.getSignalType())) {
                log.info("📢 [{}] SIGNAL DETECTED FOR {} at ${}", 
                        signal.getSignalType().toUpperCase(), 
                        signal.getTicker(), 
                        signal.getPrice());
                for (String reason : signal.getReasons()) {
                    log.info("  👉 {}", reason);
                }
            } else {
                log.debug("Ticker {} is HOLD. Price: ${}", signal.getTicker(), signal.getPrice());
            }
        });
    }
}
