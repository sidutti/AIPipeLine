package com.stockreviewer.repository;

import com.stockreviewer.model.WatchlistTicker;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;

public interface WatchlistRepository extends ReactiveElasticsearchRepository<WatchlistTicker, String> {
}
