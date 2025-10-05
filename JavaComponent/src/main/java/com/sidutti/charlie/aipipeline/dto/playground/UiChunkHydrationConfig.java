package com.sidutti.charlie.aipipeline.dto.playground;

import java.util.List;
import java.util.Map;

public class UiChunkHydrationConfig {
    private List<String> hydrators;
    private Map<String, Map<String, Object>> parameters;

    public UiChunkHydrationConfig() {}

    public List<String> getHydrators() { return hydrators; }
    public void setHydrators(List<String> hydrators) { this.hydrators = hydrators; }

    public Map<String, Map<String, Object>> getParameters() { return parameters; }
    public void setParameters(Map<String, Map<String, Object>> parameters) { this.parameters = parameters; }
}