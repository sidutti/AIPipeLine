package com.sidutti.charlie.aipipeline.dto.playground;

import java.util.Map;

public class UiChunkingConfig {
    private String strategy;
    private Map<String, Object> parameters;

    public UiChunkingConfig() {
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
}