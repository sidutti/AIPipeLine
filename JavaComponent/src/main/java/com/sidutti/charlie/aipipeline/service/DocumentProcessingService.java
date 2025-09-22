package com.sidutti.charlie.aipipeline.service;

import com.sidutti.charlie.aipipeline.dto.DocumentEmbedding;
import com.sidutti.charlie.aipipeline.dto.TargetDocument;
import com.sidutti.charlie.aipipeline.dto.repository.DocumentEmbeddingRepository;
import com.sidutti.charlie.aipipeline.dto.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.List;
import java.util.Map;

@Service
public class DocumentProcessingService {

    private final DocumentIngestionService ingestionService;
    private final EmbeddingService embeddingService;
    private final PythonServiceClient pythonServiceClient;
    private final DocumentRepository documentRepository;
    private final DocumentEmbeddingRepository embeddingRepository;
    private final Scheduler forkJoinScheduler;

    public DocumentProcessingService(DocumentIngestionService ingestionService,
                                     EmbeddingService embeddingService,
                                     PythonServiceClient pythonServiceClient,
                                     DocumentRepository documentRepository,
                                     DocumentEmbeddingRepository embeddingRepository, Scheduler forkJoinScheduler) {
        this.ingestionService = ingestionService;
        this.embeddingService = embeddingService;
        this.pythonServiceClient = pythonServiceClient;
        this.documentRepository = documentRepository;
        this.embeddingRepository = embeddingRepository;
        this.forkJoinScheduler = forkJoinScheduler;
    }

    public Mono<String> processAllDocuments() {
        ingestionService.ingestDocumentsFromRepository()
                .flatMap(embeddingService::generateEmbedding)
                .flatMap(this::updateStatus)
                .subscribeOn(forkJoinScheduler)
                .subscribe();
        return Mono.just("All documents processed");
    }

    private Mono<TargetDocument> updateStatus(DocumentEmbedding documentEmbedding) {
        return documentRepository.findById(documentEmbedding.getDocumentId())
                .map(doc -> {
                    doc.setProcessingStatus(TargetDocument.ProcessingStatus.PROCESSING);
                    return doc;
                })
                .flatMap(documentRepository::save);
    }


    public Flux<TargetDocument> performClustering() {
        return embeddingService.getAllEmbeddings()
                .flatMap(embeddings -> pythonServiceClient.performClustering(embeddings, null))
                .map(PythonServiceClient.ClusteringResponse::clusters)
                .flatMapMany(Flux::fromIterable)
                .flatMap(this::processEntries);
    }


    private Flux<TargetDocument> processEntries(Map<String, Object> clusterEntry) {
        String clusterName = clusterEntry.get("cluster_id").toString();
        List<String> docIds = (List<String>) clusterEntry.get("documents");
        if(docIds.isEmpty()){
            return Flux.empty();
        }
        return documentRepository.findAllById(docIds)
                .map(td -> {
                    td.setClusterId(clusterName);
                    td.setProcessingStatus(TargetDocument.ProcessingStatus.CLUSTERED);
                    return td;
                })
                .flatMap(documentRepository::save);
    }

    private Mono<String> performClassification() {
        return embeddingRepository.findAll()
                .filter(embedding -> embedding.getClusterId() != null)
                .groupBy(DocumentEmbedding::getClusterId)
                .flatMap(groupedFlux -> {
                    String clusterId = groupedFlux.key();
                    return groupedFlux.collectList()
                            .flatMap(embeddings -> {
                                List<String> textContents = embeddings.stream()
                                        .map(DocumentEmbedding::getTextContent)
                                        .toList();

                                return pythonServiceClient.performClassification(clusterId, textContents)
                                        .flatMap(response -> updateDocumentClassifications(clusterId, response));
                            });
                })
                .collectList()
                .map(results -> "Classification completed for " + results.size() + " clusters");
    }

    private Mono<Void> updateDocumentClassifications(String clusterId,
                                                     PythonServiceClient.ClassificationResponse response) {
        return documentRepository.findByClusterId(clusterId)
                .flatMap(document -> {
                    document.setClassification(response.classification());
                    document.setConfidenceScore(response.confidence());
                    document.setProcessingStatus(TargetDocument.ProcessingStatus.CLASSIFIED);
                    return documentRepository.save(document);
                })
                .then();
    }

    public Mono<ProcessingStatus> getProcessingStatus() {
        return Mono.zip(
                documentRepository.count(),
                documentRepository.countByProcessingStatus(TargetDocument.ProcessingStatus.PENDING),
                documentRepository.countByProcessingStatus(TargetDocument.ProcessingStatus.EMBEDDING_GENERATED),
                documentRepository.countByProcessingStatus(TargetDocument.ProcessingStatus.CLUSTERED),
                documentRepository.countByProcessingStatus(TargetDocument.ProcessingStatus.CLASSIFIED)
        ).map(tuple -> {
            ProcessingStatus status = new ProcessingStatus();
            status.setTotalDocuments(tuple.getT1());
            status.setPendingDocuments(tuple.getT2());
            status.setEmbeddingGenerated(tuple.getT3());
            status.setClustered(tuple.getT4());
            status.setClassified(tuple.getT5());
            return status;
        });
    }

    public Flux<TargetDocument> getDocumentsByClassification(String classification) {
        return documentRepository.findAll()
                .filter(doc -> classification.equals(doc.getClassification()));
    }

    public Flux<TargetDocument> getDocumentsByCluster(String clusterId) {
        return documentRepository.findByClusterId(clusterId);
    }

    public static class ProcessingStatus {
        private Long totalDocuments;
        private Long pendingDocuments;
        private Long embeddingGenerated;
        private Long clustered;
        private Long classified;

        public Long getTotalDocuments() {
            return totalDocuments;
        }

        public void setTotalDocuments(Long totalDocuments) {
            this.totalDocuments = totalDocuments;
        }

        public Long getPendingDocuments() {
            return pendingDocuments;
        }

        public void setPendingDocuments(Long pendingDocuments) {
            this.pendingDocuments = pendingDocuments;
        }

        public Long getEmbeddingGenerated() {
            return embeddingGenerated;
        }

        public void setEmbeddingGenerated(Long embeddingGenerated) {
            this.embeddingGenerated = embeddingGenerated;
        }

        public Long getClustered() {
            return clustered;
        }

        public void setClustered(Long clustered) {
            this.clustered = clustered;
        }

        public Long getClassified() {
            return classified;
        }

        public void setClassified(Long classified) {
            this.classified = classified;
        }
    }
}