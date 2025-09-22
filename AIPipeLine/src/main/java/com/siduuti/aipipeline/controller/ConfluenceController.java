package com.siduuti.aipipeline.controller;

import com.siduuti.aipipeline.dto.confluence.ConfluenceContent;
import com.siduuti.aipipeline.dto.confluence.ConfluenceSearchResult;
import com.siduuti.aipipeline.service.ConfluenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/confluence")
public class ConfluenceController {
    
    private final ConfluenceService confluenceService;
    
    @Autowired
    public ConfluenceController(ConfluenceService confluenceService) {
        this.confluenceService = confluenceService;
    }
    
    @GetMapping("/test-connection")
    public Mono<ResponseEntity<String>> testConnection() {
        return confluenceService.testConnection()
                .map(success -> {
                    if (success) {
                        return ResponseEntity.ok("Connection successful");
                    } else {
                        return ResponseEntity.status(500).body("Connection failed");
                    }
                });
    }
    
    @GetMapping("/search")
    public Mono<ConfluenceSearchResult> searchContent(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "25") int limit) {
        return confluenceService.searchContent(query, start, limit);
    }
    
    @GetMapping("/spaces/{spaceKey}/pages")
    public Mono<List<ConfluenceContent>> listPagesInSpace(
            @PathVariable String spaceKey,
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "25") int limit) {
        return confluenceService.listPagesInSpace(spaceKey, start, limit);
    }
    
    @GetMapping("/spaces/{spaceKey}/pages/all")
    public Mono<List<ConfluenceContent>> getAllPagesInSpace(@PathVariable String spaceKey) {
        return confluenceService.getAllPagesInSpace(spaceKey);
    }
    
    @GetMapping("/pages/{pageId}")
    public Mono<ConfluenceContent> getPageById(@PathVariable String pageId) {
        return confluenceService.getPageById(pageId);
    }
    
    @GetMapping("/pages/{pageId}/content")
    public Mono<ConfluenceContent> getPageByIdWithBody(@PathVariable String pageId) {
        return confluenceService.getPageByIdWithBody(pageId);
    }
    
    @GetMapping("/pages/{pageId}/download")
    public Mono<ResponseEntity<String>> downloadPageContent(@PathVariable String pageId) {
        return confluenceService.downloadPageContent(pageId)
                .map(content -> ResponseEntity.ok()
                        .header("Content-Type", "text/html")
                        .body(content));
    }
}