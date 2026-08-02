package com.stockreviewer.service;

import com.stockreviewer.model.StockSignal;

import reactor.core.publisher.Mono;

public interface NotificationService {
    Mono<Void> notifySignal(StockSignal signal);
}
