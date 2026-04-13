package com.sidutti.charlie.aipipeline.dto.playground;

public class UiPipelineConfig {
    private String usecase;
    private String apikey;
    private UiSourceConfig source;
    private UiProcessingConfig processing;
    private UiChunkingConfig chunking;
    private UiChunkHydrationConfig chunkHydration;

    public UiPipelineConfig() {
    }

    public String getUsecase() {
        return usecase;
    }

    public void setUsecase(String usecase) {
        this.usecase = usecase;
    }

    public String getApikey() {
        return apikey;
    }

    public void setApikey(String apikey) {
        this.apikey = apikey;
    }

    public UiSourceConfig getSource() {
        return source;
    }

    public void setSource(UiSourceConfig source) {
        this.source = source;
    }

    public UiProcessingConfig getProcessing() {
        return processing;
    }

    public void setProcessing(UiProcessingConfig processing) {
        this.processing = processing;
    }

    public UiChunkingConfig getChunking() {
        return chunking;
    }

    public void setChunking(UiChunkingConfig chunking) {
        this.chunking = chunking;
    }

    public UiChunkHydrationConfig getChunkHydration() {
        return chunkHydration;
    }

    public void setChunkHydration(UiChunkHydrationConfig chunkHydration) {
        this.chunkHydration = chunkHydration;
    }
}