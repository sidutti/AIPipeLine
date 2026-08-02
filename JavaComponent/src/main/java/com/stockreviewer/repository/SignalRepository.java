package com.stockreviewer.repository;

import com.stockreviewer.model.StockSignal;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import reactor.core.publisher.Flux;


public interface SignalRepository extends ReactiveElasticsearchRepository<StockSignal, String> {
    Flux<StockSignal> findByTickerOrderByTimestampDesc(String ticker);
    Flux<StockSignal> findAllByOrderByTimestampDesc();
}
