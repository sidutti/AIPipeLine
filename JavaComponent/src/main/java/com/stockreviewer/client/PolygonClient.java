package com.stockreviewer.client;

import com.stockreviewer.client.dto.PolygonAggsResponse;
import com.stockreviewer.client.dto.PolygonFinancialsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class PolygonClient {

    private static final Logger log = LoggerFactory.getLogger(PolygonClient.class);

    private final String apiKey;
    private final WebClient webClient;

    public PolygonClient(
            @Value("${app.polygon.api-key}") String apiKey,
            @Value("${app.polygon.base-url}") String baseUrl) {
        this.apiKey = apiKey;
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
    }

    public Mono<PolygonAggsResponse> getDailyBars(String ticker, LocalDate from, LocalDate to) {
        String fromStr = from.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String toStr = to.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String path = String.format("/v2/aggs/ticker/%s/range/1/day/%s/%s?adjusted=true&sort=asc&limit=5000&apiKey=%s",
                ticker, fromStr, toStr, apiKey);

        return executeWithRetry(path, PolygonAggsResponse.class);
    }

    public Mono<PolygonFinancialsResponse> getFinancials(String ticker) {
        String path = String.format("/vx/reference/financials?ticker=%s&limit=10&apiKey=%s",
                ticker, apiKey);

        return executeWithRetry(path, PolygonFinancialsResponse.class);
    }

    private <T> Mono<T> executeWithRetry(String path, Class<T> responseType) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path(path).build())
                .retrieve()
                .onStatus(status -> status.value() == 429, clientResponse -> {
                    log.warn("Rate limit exceeded (429) for API call to {}.", path);
                    return Mono.error(new RuntimeException("Rate limit exceeded"));
                })
                .onStatus(status -> !status.is2xxSuccessful() && status.value() != 429, clientResponse -> 
                    clientResponse.bodyToMono(String.class)
                            .flatMap(body -> {
                                log.error("API request failed with status code {}: {}", clientResponse.statusCode().value(), body);
                                return Mono.error(new RuntimeException("API error: Status " + clientResponse.statusCode().value()));
                            })
                )
                .bodyToMono(responseType)
                .retryWhen(reactor.util.retry.Retry.backoff(3, Duration.ofSeconds(2))
                        .filter(throwable -> true)
                        .doBeforeRetry(retrySignal -> 
                            log.info("Retrying API request to: {} (Attempt {}/3)", path, retrySignal.totalRetries() + 1)
                        )
                )
                .onErrorResume(e -> {
                    log.error("Failed to execute request after 3 attempts on path: {}", path, e);
                    return Mono.error(new RuntimeException("Failed to retrieve data from Polygon.io", e));
                });
    }
}
