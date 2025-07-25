package com.siduuti.aipipeline.dto.confluence;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfluenceSearchResult {
    private List<ConfluenceContent> results;
    private int start;
    private int limit;
    private int size;
    
    public List<ConfluenceContent> getResults() {
        return results;
    }
    
    public void setResults(List<ConfluenceContent> results) {
        this.results = results;
    }
    
    public int getStart() {
        return start;
    }
    
    public void setStart(int start) {
        this.start = start;
    }
    
    public int getLimit() {
        return limit;
    }
    
    public void setLimit(int limit) {
        this.limit = limit;
    }
    
    public int getSize() {
        return size;
    }
    
    public void setSize(int size) {
        this.size = size;
    }
}