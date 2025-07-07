package com.siduuti.aipipeline.controller;

import com.siduuti.aipipeline.dto.TargetDocument;
import com.siduuti.aipipeline.service.DocumentProcessingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/documents")
public class DocumentProcessingController {
    
    private final DocumentProcessingService processingService;
    
    public DocumentProcessingController(DocumentProcessingService processingService) {
        this.processingService = processingService;
    }
    
    @PostMapping("/process")
    public Mono<String> processDocuments() {
        return processingService.processAllDocuments();
    }

    @PostMapping("/cluster")
    public Flux<String> clusterDocuments() {
        return processingService.performClustering();
    }

    @GetMapping("/status")
    public Mono<DocumentProcessingService.ProcessingStatus> getProcessingStatus() {
        return processingService.getProcessingStatus();
    }
    
    @GetMapping("/classification/{classification}")
    public Flux<TargetDocument> getDocumentsByClassification(@PathVariable String classification) {
        return processingService.getDocumentsByClassification(classification);
    }
    
    @GetMapping("/cluster/{clusterId}")
    public Flux<TargetDocument> getDocumentsByCluster(@PathVariable String clusterId) {
        return processingService.getDocumentsByCluster(clusterId);
    }
    
    @GetMapping("/health")
    public Mono<String> healthCheck() {
        return Mono.just("TargetDocument Classifier Service is running");
    }
}