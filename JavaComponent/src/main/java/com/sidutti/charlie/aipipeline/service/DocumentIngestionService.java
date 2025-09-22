package com.sidutti.charlie.aipipeline.service;


import com.sidutti.charlie.aipipeline.dto.TargetDocument;
import com.sidutti.charlie.aipipeline.dto.TextAndDoc;
import com.sidutti.charlie.aipipeline.dto.repository.DocumentRepository;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Service
public class DocumentIngestionService {

    private final DocumentRepository documentRepository;
    private final TokenTextSplitter tokenTextSplitter;

    @Value("${document.repository.base-path}")
    private String repositoryBasePath;

    public DocumentIngestionService(DocumentRepository documentRepository, TokenTextSplitter tokenTextSplitter) {
        this.documentRepository = documentRepository;

        this.tokenTextSplitter = tokenTextSplitter;
    }

    public Flux<TextAndDoc> ingestDocumentsFromRepository() {
        return Flux.fromStream(this::getDocumentFiles)
                .flatMap(this::processFile);
    }


    private Stream<Path> getDocumentFiles() {
        try {
            Set<String> fileNames = new HashSet<>();
            return Files.walk(Paths.get(repositoryBasePath))
                    .filter(Files::isRegularFile)
                    .filter(this::isSupportedFileType)
                    .filter(p -> fileNames.add(p.getFileName().toString()));
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

    private Mono<TextAndDoc> processFile(Path filePath) {

        try {
            File file = filePath.toFile();
            String fileName = file.getName();
            FileSystemResource resource = new FileSystemResource(filePath);
            TikaDocumentReader documentReader = new TikaDocumentReader(resource);
            List<Document> sourceDocs = documentReader.get();

            // 2. Apply the TokenTextSplitter to chunk the documents.
            List<Document> chunkedDocs = tokenTextSplitter.apply(sourceDocs);
            TargetDocument targetDocument = createTargetDocuments(filePath, fileName, file);
            return documentRepository.save(targetDocument)
                    .thenReturn(new TextAndDoc(targetDocument,chunkedDocs));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Mono.empty();
    }

    private TargetDocument createTargetDocuments(Path filePath, String fileName, File file) {
        TargetDocument document = new TargetDocument();
        document.setFileName(fileName);
        document.setFilePath(filePath.toString());
        document.setFileSize(file.length());
        document.setProcessingStatus(TargetDocument.ProcessingStatus.PENDING);
        return document;
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