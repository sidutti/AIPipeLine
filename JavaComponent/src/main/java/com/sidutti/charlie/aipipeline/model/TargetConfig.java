package com.sidutti.charlie.aipipeline.model;

public class TargetConfig {
    private String apiKey;
    private String useCaseName;

    public TargetConfig() {}

    public TargetConfig(String apiKey, String useCaseName) {
        this.apiKey = apiKey;
        this.useCaseName = useCaseName;
    }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getUseCaseName() { return useCaseName; }
    public void setUseCaseName(String useCaseName) { this.useCaseName = useCaseName; }

    @Override
    public String toString() {
        return "TargetConfig{" +
                "apiKey='[REDACTED]'" +
                ", useCaseName='" + useCaseName + '\'' +
                '}';
    }
}