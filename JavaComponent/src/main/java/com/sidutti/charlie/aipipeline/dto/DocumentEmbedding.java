package com.sidutti.charlie.aipipeline.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "document_embeddings")
public class DocumentEmbedding {
    
    @Id
    private String id;
    
    @Field(type = FieldType.Keyword)
    private String documentId;
    
    @Field(type = FieldType.Dense_Vector, dims = 1024)
    private float[] embedding;
    
    @Field(type = FieldType.Text)
    private String textContent;
    
    @Field(type = FieldType.Keyword)
    private String fileName;
    

    
    @Field(type = FieldType.Keyword)
    private String clusterId;
    
    public DocumentEmbedding() {

    }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    
    public float[] getEmbedding() { return embedding; }
    public void setEmbedding(float[] embedding) { this.embedding = embedding; }
    
    public String getTextContent() { return textContent; }
    public void setTextContent(String textContent) { this.textContent = textContent; }
    
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    
    public String getClusterId() { return clusterId; }
    public void setClusterId(String clusterId) { this.clusterId = clusterId; }
}