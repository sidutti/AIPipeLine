package com.siduuti.aipipeline.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "documents")
public class TargetDocument {
    
    @Id
    private String id;
    
    @Field("file_name")
    private String fileName;
    
    @Field("file_path")
    private String filePath;
    
    @Field("content")
    private String content;
    
    @Field("file_size")
    private Long fileSize;
    
    @Field("mime_type")
    private String mimeType;
    
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @Field("updated_at")
    private LocalDateTime updatedAt;
    
    @Field("processing_status")
    private ProcessingStatus processingStatus;
    
    @Field("cluster_id")
    private String clusterId;
    
    @Field("classification")
    private String classification;
    
    @Field("confidence_score")
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