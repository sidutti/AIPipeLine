package com.siduuti.aipipeline.service;



import com.siduuti.aipipeline.dto.TargetDocument;
import com.siduuti.aipipeline.dto.repository.DocumentRepository;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Service
public class DocumentIngestionService {
    
    private final DocumentRepository documentRepository;
    private final Tika tika;
    
    @Value("${document.repository.base-path}")
    private String repositoryBasePath;
    
    public DocumentIngestionService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
        this.tika = new Tika();
    }
    
    public Flux<TargetDocument> ingestDocumentsFromRepository() {
        return Flux.fromStream(this::getDocumentFiles)
                .flatMap(this::processFile)
                .flatMap(documentRepository::save);
    }
    
    public Mono<TargetDocument> ingestSingleDocument(String filePath) {
        return processFile(Paths.get(filePath))
                .flatMap(documentRepository::save);
    }
    
    private Stream<Path> getDocumentFiles() {
        try {
            return Files.walk(Paths.get(repositoryBasePath))
                    .filter(Files::isRegularFile)
                    .filter(this::isSupportedFileType);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read document repository", e);
        }
    }
    
    private boolean isSupportedFileType(Path path) {
        String fileName = path.getFileName().toString().toLowerCase();
        return fileName.endsWith(".pdf") ||
               fileName.endsWith(".doc") ||
               fileName.endsWith(".docx") ||
               fileName.endsWith(".txt") ||
               fileName.endsWith(".html") ||
               fileName.endsWith(".xml");
    }
    
    private Mono<TargetDocument> processFile(Path filePath) {

            try {
                File file = filePath.toFile();
                String fileName = file.getName();
                
                return documentRepository.findByFileName(fileName)
                        .switchIfEmpty(Mono.defer(() -> {
                            try {
                                String content = tika.parseToString(file);
                                String mimeType = tika.detect(file);
                                
                                TargetDocument document = new TargetDocument();
                                document.setFileName(fileName);
                                document.setFilePath(filePath.toString());
                                document.setContent(content);
                                document.setFileSize(file.length());
                                document.setMimeType(mimeType);
                                document.setProcessingStatus(TargetDocument.ProcessingStatus.PENDING);
                                
                                return Mono.just(document);
                            } catch (IOException | TikaException e) {
                                throw new RuntimeException("Failed to process file: " + fileName, e);
                            }
                        }));
                        
            } catch (Exception e) {
                throw new RuntimeException("Failed to process file: " + filePath, e);
            }

    }
    
    public Mono<Long> getDocumentCount() {
        return documentRepository.count();
    }
    
    public Mono<Long> getPendingDocumentCount() {
        return documentRepository.countByProcessingStatus(TargetDocument.ProcessingStatus.PENDING);
    }
    
    public Flux<TargetDocument> getPendingDocuments() {
        return documentRepository.findByProcessingStatus(TargetDocument.ProcessingStatus.PENDING);
    }
}