package com.siduuti.aipipeline.dto.confluence;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfluenceBody {
    private ConfluenceBodyContent storage;
    private ConfluenceBodyContent view;
    private ConfluenceBodyContent exportView;
    
    public ConfluenceBodyContent getStorage() {
        return storage;
    }
    
    public void setStorage(ConfluenceBodyContent storage) {
        this.storage = storage;
    }
    
    public ConfluenceBodyContent getView() {
        return view;
    }
    
    public void setView(ConfluenceBodyContent view) {
        this.view = view;
    }
    
    public ConfluenceBodyContent getExportView() {
        return exportView;
    }
    
    public void setExportView(ConfluenceBodyContent exportView) {
        this.exportView = exportView;
    }
}