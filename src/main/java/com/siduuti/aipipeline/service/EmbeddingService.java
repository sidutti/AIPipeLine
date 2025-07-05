package com.siduuti.aipipeline.service;

import com.siduuti.aipipeline.dto.DocumentEmbedding;
import com.siduuti.aipipeline.dto.TargetDocument;
import com.siduuti.aipipeline.dto.repository.DocumentEmbeddingRepository;
import com.siduuti.aipipeline.dto.repository.DocumentRepository;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class EmbeddingService {

    private final DocumentRepository documentRepository;
    private final DocumentEmbeddingRepository embeddingRepository;
    private final EmbeddingModel embeddingModel;


    public EmbeddingService(DocumentRepository documentRepository,
                            DocumentEmbeddingRepository embeddingRepository,
                            @Qualifier("ollamaEmbeddingModel") EmbeddingModel embeddingModel) {
        this.documentRepository = documentRepository;
        this.embeddingRepository = embeddingRepository;
        this.embeddingModel = embeddingModel;
    }

    public Mono<DocumentEmbedding> generateEmbedding(TargetDocument document) {

        String textContent = preprocessText(document.getContent());
        float[] embedding = embeddingModel.embed(textContent);

        DocumentEmbedding docEmbedding = new DocumentEmbedding();
        docEmbedding.setDocumentId(document.getId());
        docEmbedding.setEmbedding(embedding);
        docEmbedding.setTextContent(textContent);
        docEmbedding.setFileName(document.getFileName());
        return embeddingRepository.save(docEmbedding);
    }


    public Mono<List<DocumentEmbedding>> getAllEmbeddings() {
        return embeddingRepository.findAll().collectList();
    }

    public Flux<DocumentEmbedding> getEmbeddingsByCluster(String clusterId) {
        return embeddingRepository.findByClusterId(clusterId);
    }

    private String preprocessText(String text) {
        if (text == null) return "";

        text = text.replaceAll("\\s+", " ");
        text = text.replaceAll("[^\\w\\s]", "");

        if (text.length() > 8000) {
            text = text.substring(0, 8000);
        }

        return text.trim();
    }

    private void updateDocumentStatus(String documentId, TargetDocument.ProcessingStatus status) {
        documentRepository.findById(documentId)
                .map(doc -> {
                    doc.setProcessingStatus(status);
                    return doc;
                })
                .flatMap(documentRepository::save)
                .subscribe();
    }
}