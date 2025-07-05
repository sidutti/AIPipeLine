package com.siduuti.aipipeline.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.time.LocalDateTime;

@Document(indexName = "id")
public class TargetDocument {
    
    @Id
    private String id;

    private String fileName;
    

    private String filePath;
    

    private String content;
    

    private Long fileSize;
    

    private String mimeType;
    

    private LocalDateTime createdAt;
    

    private LocalDateTime updatedAt;
    

    private ProcessingStatus processingStatus;
    

    private String clusterId;
    

    private String classification;
    

    private Double confidenceScore;
    
    public TargetDocument() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.processingStatus = ProcessingStatus.PENDING;
    }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public ProcessingStatus getProcessingStatus() { return processingStatus; }
    public void setProcessingStatus(ProcessingStatus processingStatus) { this.processingStatus = processingStatus; }
    
    public String getClusterId() { return clusterId; }
    public void setClusterId(String clusterId) { this.clusterId = clusterId; }
    
    public String getClassification() { return classification; }
    public void setClassification(String classification) { this.classification = classification; }
    
    public Double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(Double confidenceScore) { this.confidenceScore = confidenceScore; }
    
    public enum ProcessingStatus {
        PENDING,
        PROCESSING,
        EMBEDDING_GENERATED,
        CLUSTERED,
        CLASSIFIED,
        FAILED
    }
}