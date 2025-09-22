package com.sidutti.charlie.aipipeline.dto.confluence;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfluenceVersion {
    private int number;
    private String when;
    private String message;
    private boolean minorEdit;
    
    public int getNumber() {
        return number;
    }
    
    public void setNumber(int number) {
        this.number = number;
    }
    
    public String getWhen() {
        return when;
    }
    
    public void setWhen(String when) {
        this.when = when;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public boolean isMinorEdit() {
        return minorEdit;
    }
    
    public void setMinorEdit(boolean minorEdit) {
        this.minorEdit = minorEdit;
    }
}