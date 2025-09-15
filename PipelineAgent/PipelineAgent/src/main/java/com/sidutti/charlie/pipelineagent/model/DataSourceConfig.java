package com.sidutti.charlie.pipelineagent.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = ConfluenceConfig.class, name = "confluence"),
    @JsonSubTypes.Type(value = EcmRepoConfig.class, name = "ecm"),
    @JsonSubTypes.Type(value = SharepointConfig.class, name = "sharepoint"),
    @JsonSubTypes.Type(value = ObjectStorageConfig.class, name = "objectstorage"),
    @JsonSubTypes.Type(value = UrlListConfig.class, name = "urllist"),
    @JsonSubTypes.Type(value = CsvConfig.class, name = "csv"),
    @JsonSubTypes.Type(value = KafkaConfig.class, name = "kafka"),
    @JsonSubTypes.Type(value = WebhookConfig.class, name = "webhook")
})
public abstract class DataSourceConfig {
    private String name;
    private String description;
    private boolean enabled;

    public DataSourceConfig() {}

    public DataSourceConfig(String name, String description, boolean enabled) {
        this.name = name;
        this.description = description;
        this.enabled = enabled;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public abstract String getType();
}