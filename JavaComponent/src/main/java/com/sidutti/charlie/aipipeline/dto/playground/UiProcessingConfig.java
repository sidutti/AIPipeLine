package com.sidutti.charlie.aipipeline.dto.playground;

import java.util.List;
import java.util.Map;

public class UiProcessingConfig {
    private List<String> processors;
    private Map<String, Map<String, Object>> parameters;

    public UiProcessingConfig() {
    }

    public List<String> getProcessors() {
        return processors;
    }

    public void setProcessors(List<String> processors) {
        this.processors = processors;
    }

    public Map<String, Map<String, Object>> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Map<String, Object>> parameters) {
        this.parameters = parameters;
    }
}