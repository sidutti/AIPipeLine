package com.siduuti.aipipeline.model;

import java.util.List;
import java.util.Map;

public class KafkaConfig extends DataSourceConfig {
    private List<String> bootstrapServers;
    private String topicName;
    private String consumerGroupId;
    private String offsetReset; // earliest, latest, none
    private String keyDeserializer;
    private String valueDeserializer;
    private Map<String, String> additionalProperties;
    private boolean enableAutoCommit;
    private int autoCommitInterval;
    private int sessionTimeout;
    private String securityProtocol;
    private String saslMechanism;
    private String saslUsername;
    private String saslPassword;

    public KafkaConfig() {
        super();
    }

    @Override
    public String getType() {
        return "kafka";
    }

    public List<String> getBootstrapServers() { return bootstrapServers; }
    public void setBootstrapServers(List<String> bootstrapServers) { this.bootstrapServers = bootstrapServers; }

    public String getTopicName() { return topicName; }
    public void setTopicName(String topicName) { this.topicName = topicName; }

    public String getConsumerGroupId() { return consumerGroupId; }
    public void setConsumerGroupId(String consumerGroupId) { this.consumerGroupId = consumerGroupId; }

    public String getOffsetReset() { return offsetReset; }
    public void setOffsetReset(String offsetReset) { this.offsetReset = offsetReset; }

    public String getKeyDeserializer() { return keyDeserializer; }
    public void setKeyDeserializer(String keyDeserializer) { this.keyDeserializer = keyDeserializer; }

    public String getValueDeserializer() { return valueDeserializer; }
    public void setValueDeserializer(String valueDeserializer) { this.valueDeserializer = valueDeserializer; }

    public Map<String, String> getAdditionalProperties() { return additionalProperties; }
    public void setAdditionalProperties(Map<String, String> additionalProperties) { this.additionalProperties = additionalProperties; }

    public boolean isEnableAutoCommit() { return enableAutoCommit; }
    public void setEnableAutoCommit(boolean enableAutoCommit) { this.enableAutoCommit = enableAutoCommit; }

    public int getAutoCommitInterval() { return autoCommitInterval; }
    public void setAutoCommitInterval(int autoCommitInterval) { this.autoCommitInterval = autoCommitInterval; }

    public int getSessionTimeout() { return sessionTimeout; }
    public void setSessionTimeout(int sessionTimeout) { this.sessionTimeout = sessionTimeout; }

    public String getSecurityProtocol() { return securityProtocol; }
    public void setSecurityProtocol(String securityProtocol) { this.securityProtocol = securityProtocol; }

    public String getSaslMechanism() { return saslMechanism; }
    public void setSaslMechanism(String saslMechanism) { this.saslMechanism = saslMechanism; }

    public String getSaslUsername() { return saslUsername; }
    public void setSaslUsername(String saslUsername) { this.saslUsername = saslUsername; }

    public String getSaslPassword() { return saslPassword; }
    public void setSaslPassword(String saslPassword) { this.saslPassword = saslPassword; }
}