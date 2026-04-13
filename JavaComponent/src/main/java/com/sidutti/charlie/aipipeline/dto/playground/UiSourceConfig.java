package com.sidutti.charlie.aipipeline.dto.playground;

import java.util.Map;

public class UiSourceConfig {
    private String type;
    private Map<String, Object> parameters;

    public UiSourceConfig() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
}