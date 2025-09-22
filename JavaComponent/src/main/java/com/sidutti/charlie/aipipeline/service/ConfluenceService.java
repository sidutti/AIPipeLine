package com.sidutti.charlie.aipipeline.service;

import com.sidutti.charlie.aipipeline.config.ConfluenceConfig;
import com.sidutti.charlie.aipipeline.dto.confluence.ConfluenceContent;
import com.sidutti.charlie.aipipeline.dto.confluence.ConfluenceSearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.List;

@Service
public class ConfluenceService {
    
    private final WebClient webClient;
    private final ConfluenceConfig confluenceConfig;
    
    @Autowired
    public ConfluenceService(ConfluenceConfig confluenceConfig) {
        this.confluenceConfig = confluenceConfig;
        this.webClient = WebClient.builder()
                .baseUrl(confluenceConfig.getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
    
    public Mono<ConfluenceSearchResult> searchContent(String query, int start, int limit) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/rest/api/content/search")
                        .queryParam("cql", query)
                        .queryParam("start", start)
                        .queryParam("limit", limit)
                        .queryParam("expand", "body.storage,space,version")
                        .build())
                .headers(this::addAuthHeaders)
                .retrieve()
                .bodyToMono(ConfluenceSearchResult.class);
    }
    
    public Mono<List<ConfluenceContent>> listPagesInSpace(String spaceKey, int start, int limit) {
        String cql = String.format("space = %s AND type = page", spaceKey);
        return searchContent(cql, start, limit)
                .map(ConfluenceSearchResult::getResults);
    }
    
    public Mono<ConfluenceContent> getPageById(String pageId) {
        return webClient.get()
                .uri("/rest/api/content/{id}", pageId)
                .headers(this::addAuthHeaders)
                .retrieve()
                .bodyToMono(ConfluenceContent.class);
    }
    
    public Mono<ConfluenceContent> getPageByIdWithBody(String pageId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/rest/api/content/{id}")
                        .queryParam("expand", "body.storage,body.view,space,version")
                        .build(pageId))
                .headers(this::addAuthHeaders)
                .retrieve()
                .bodyToMono(ConfluenceContent.class);
    }
    
    public Mono<String> downloadPageContent(String pageId) {
        return getPageByIdWithBody(pageId)
                .map(content -> {
                    if (content.getBody() != null && content.getBody().getStorage() != null) {
                        return content.getBody().getStorage().getValue();
                    }
                    return "";
                });
    }
    
    public Mono<List<ConfluenceContent>> getAllPagesInSpace(String spaceKey) {
        return listPagesInSpace(spaceKey, 0, 25)
                .expand(result -> {
                    if (result.size() == 25) {
                        return searchContent(String.format("space = %s AND type = page", spaceKey), 
                                result.size(), 25)
                                .map(ConfluenceSearchResult::getResults);
                    }
                    return Mono.empty();
                })
                .collectList()
                .map(lists -> lists.stream()
                        .flatMap(List::stream)
                        .toList());
    }
    
    private void addAuthHeaders(HttpHeaders headers) {
        if (confluenceConfig.getUsername() != null && confluenceConfig.getApiToken() != null) {
            String credentials = confluenceConfig.getUsername() + ":" + confluenceConfig.getApiToken();
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
            headers.set(HttpHeaders.AUTHORIZATION, "Basic " + encodedCredentials);
        }
    }
    
    public Mono<Boolean> testConnection() {
        return webClient.get()
                .uri("/rest/api/user/current")
                .headers(this::addAuthHeaders)
                .retrieve()
                .toBodilessEntity()
                .map(response -> response.getStatusCode().is2xxSuccessful())
                .onErrorReturn(false);
    }
}