package com.sidutti.charlie.pipelineagent.model;

public class PipelineConfig {
    private DataSourceConfig source;
    private TargetConfig target;
    private String configurationId;
    private long createdTimestamp;

    public PipelineConfig() {
        this.createdTimestamp = System.currentTimeMillis();
    }

    public PipelineConfig(DataSourceConfig source, TargetConfig target, String configurationId) {
        this();
        this.source = source;
        this.target = target;
        this.configurationId = configurationId;
    }

    public DataSourceConfig getSource() { return source; }
    public void setSource(DataSourceConfig source) { this.source = source; }

    public TargetConfig getTarget() { return target; }
    public void setTarget(TargetConfig target) { this.target = target; }

    public String getConfigurationId() { return configurationId; }
    public void setConfigurationId(String configurationId) { this.configurationId = configurationId; }

    public long getCreatedTimestamp() { return createdTimestamp; }
    public void setCreatedTimestamp(long createdTimestamp) { this.createdTimestamp = createdTimestamp; }

    @Override
    public String toString() {
        return "PipelineConfig{" +
                "source=" + source +
                ", target=" + target +
                ", configurationId='" + configurationId + '\'' +
                ", createdTimestamp=" + createdTimestamp +
                '}';
    }
}