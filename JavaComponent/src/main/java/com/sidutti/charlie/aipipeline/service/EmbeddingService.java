package com.sidutti.charlie.aipipeline.service;

import com.sidutti.charlie.aipipeline.dto.DocumentEmbedding;
import com.sidutti.charlie.aipipeline.dto.TargetDocument;
import com.sidutti.charlie.aipipeline.dto.TextAndDoc;
import com.sidutti.charlie.aipipeline.dto.repository.DocumentEmbeddingRepository;
import com.sidutti.charlie.aipipeline.dto.repository.DocumentRepository;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@Service
public class EmbeddingService {

    private final DocumentRepository documentRepository;
    private final DocumentEmbeddingRepository embeddingRepository;
    private final EmbeddingModel embeddingModel;
    private final Scheduler forkJoinScheduler;


    public EmbeddingService(DocumentRepository documentRepository,
                            DocumentEmbeddingRepository embeddingRepository,
                            @Qualifier("ollamaEmbeddingModel") EmbeddingModel embeddingModel,
                            Scheduler forkJoinScheduler) {
        this.documentRepository = documentRepository;
        this.embeddingRepository = embeddingRepository;
        this.embeddingModel = embeddingModel;
        this.forkJoinScheduler = forkJoinScheduler;
    }

    public Flux<DocumentEmbedding> generateEmbedding(TextAndDoc input) {
        return  Flux.fromIterable(input.docs())
                .publishOn(forkJoinScheduler)
                .map(d->{
                    String textContent = preprocessText(d.getText());
                    float[] embedding = embeddingModel.embed(textContent);
                    DocumentEmbedding docEmbedding = new DocumentEmbedding();
                    docEmbedding.setDocumentId(input.doc().getId());
                    docEmbedding.setEmbedding(embedding);
                    docEmbedding.setTextContent(textContent);
                    docEmbedding.setFileName(input.doc().getFileName());
                    return docEmbedding;
                })
                .flatMap(embeddingRepository::save);
    }


    public Mono<Flux<DocumentEmbedding>> getAllEmbeddings() {

        return documentRepository.findTop500ByProcessingStatus(TargetDocument.ProcessingStatus.PROCESSING)
                .map(TargetDocument::getId)
                .collectList()
                .map(embeddingRepository::findAllByDocumentIdIn);
    }

    public Flux<DocumentEmbedding> getEmbeddingsByCluster(String clusterId) {
        return embeddingRepository.findByClusterId(clusterId);
    }

    private String preprocessText(String text) {
        if (text == null) return "";

        text = text.replaceAll("\\s+", " ");
        text = text.replaceAll("[^\\w\\s]", "");


        return text.trim();
    }


}