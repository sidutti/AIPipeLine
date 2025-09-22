package com.siduuti.aipipeline.dto.confluence;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfluenceContent {
    private String id;
    private String type;
    private String status;
    private String title;
    private ConfluenceSpace space;
    private ConfluenceVersion version;
    private ConfluenceBody body;
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public ConfluenceSpace getSpace() {
        return space;
    }
    
    public void setSpace(ConfluenceSpace space) {
        this.space = space;
    }
    
    public ConfluenceVersion getVersion() {
        return version;
    }
    
    public void setVersion(ConfluenceVersion version) {
        this.version = version;
    }
    
    public ConfluenceBody getBody() {
        return body;
    }
    
    public void setBody(ConfluenceBody body) {
        this.body = body;
    }
}