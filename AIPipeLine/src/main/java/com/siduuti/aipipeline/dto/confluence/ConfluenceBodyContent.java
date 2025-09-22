package com.siduuti.aipipeline.dto.confluence;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfluenceBodyContent {
    private String value;
    private String representation;
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
    public String getRepresentation() {
        return representation;
    }
    
    public void setRepresentation(String representation) {
        this.representation = representation;
    }
}