package com.sidutti.charlie.pipelineagent;

import com.sidutti.charlie.pipelineagent.service.ConfigurationAgentService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

@SpringBootTest
public class FullPipelineConfigurationTest {

    @Test
    public void testCompleteConfluenceToTargetFlow() {
        ChatClient.Builder builder = ChatClient.builder();
        ConfigurationAgentService service = new ConfigurationAgentService(builder);

        String sessionId = "test-complete-flow";

        // Test the complete flow from Confluence source to target
        StepVerifier.create(
            service.startConfiguration(sessionId, "confluence")
                // Answer all Confluence questions
                .then(service.processAnswer(sessionId, "https://mycompany.atlassian.net"))
                .then(service.processAnswer(sessionId, "user@company.com"))
                .then(service.processAnswer(sessionId, "ATATT3xFfGF0token"))
                .then(service.processAnswer(sessionId, "SPACE1,SPACE2"))
                .then(service.processAnswer(sessionId, "yes"))
                .then(service.processAnswer(sessionId, "1000"))
                .then(service.processAnswer(sessionId, "2024-01-01"))
                // This should transition to target configuration
                .then(service.processAnswer(sessionId, "sk-1234567890abcdef"))
                .then(service.processAnswer(sessionId, "KnowledgeBase Analytics"))
        )
        .expectNextMatches(response ->
            response.contains("Pipeline configuration completed successfully!") &&
            response.contains("TargetConfig") &&
            response.contains("KnowledgeBase Analytics")
        )
        .verifyComplete();
    }

    @Test
    public void testSourceToTargetTransition() {
        ChatClient.Builder builder = ChatClient.builder();
        ConfigurationAgentService service = new ConfigurationAgentService(builder);

        String sessionId = "test-transition";

        // Test that we properly transition from source to target
        StepVerifier.create(
            service.startConfiguration(sessionId, "csv")
                .then(service.processAnswer(sessionId, "/path/to/data.csv"))
                .then(service.processAnswer(sessionId, ","))
                .then(service.processAnswer(sessionId, "\""))
                .then(service.processAnswer(sessionId, "\\"))
                .then(service.processAnswer(sessionId, "yes"))
                .then(service.processAnswer(sessionId, "auto-detect"))
                .then(service.processAnswer(sessionId, "UTF-8"))
                .then(service.processAnswer(sessionId, "0"))
                .then(service.processAnswer(sessionId, "all"))
                .then(service.processAnswer(sessionId, "yyyy-MM-dd"))
                .then(service.processAnswer(sessionId, "all"))
        )
        .expectNextMatches(response ->
            response.contains("Now let's configure the target") &&
            response.contains("API key")
        )
        .verifyComplete();
    }

    @Test
    public void testConfigurationStatus() {
        ChatClient.Builder builder = ChatClient.builder();
        ConfigurationAgentService service = new ConfigurationAgentService(builder);

        String sessionId = "test-status";

        // Test status reporting during different phases
        StepVerifier.create(
            service.startConfiguration(sessionId, "kafka")
                .then(service.processAnswer(sessionId, "localhost:9092"))
                .then(service.getConfigurationStatus(sessionId))
        )
        .expectNextMatches(status ->
            status.contains("source questions completed") &&
            status.contains("kafka")
        )
        .verifyComplete();
    }

    @Test
    public void testWebhookCompleteFlow() {
        ChatClient.Builder builder = ChatClient.builder();
        ConfigurationAgentService service = new ConfigurationAgentService(builder);

        String sessionId = "test-webhook-complete";

        // Test webhook configuration with all answers
        StepVerifier.create(
            service.startConfiguration(sessionId, "webhook")
                .then(service.processAnswer(sessionId, "https://api.example.com/webhook"))
                .then(service.processAnswer(sessionId, "POST"))
                .then(service.processAnswer(sessionId, "Content-Type:application/json,Authorization:Bearer token"))
                .then(service.processAnswer(sessionId, "application/json"))
                .then(service.processAnswer(sessionId, "bearer"))
                .then(service.processAnswer(sessionId, "sk-bearer-token"))
                .then(service.processAnswer(sessionId, "username"))
                .then(service.processAnswer(sessionId, "password"))
                .then(service.processAnswer(sessionId, "api-key-value"))
                .then(service.processAnswer(sessionId, "X-API-Key"))
                .then(service.processAnswer(sessionId, "yes"))
                .then(service.processAnswer(sessionId, "30"))
                .then(service.processAnswer(sessionId, "3"))
                // JSON Path configuration
                .then(service.processAnswer(sessionId, "$.content"))
                .then(service.processAnswer(sessionId, "$.id"))
                .then(service.processAnswer(sessionId, "$.timestamp"))
                .then(service.processAnswer(sessionId, "ISO8601"))
                .then(service.processAnswer(sessionId, "author:$.author,category:$.category"))
                .then(service.processAnswer(sessionId, "no"))
                // Target phase
                .then(service.processAnswer(sessionId, "sk-target-api-key"))
                .then(service.processAnswer(sessionId, "Webhook Data Pipeline"))
        )
        .expectNextMatches(response ->
            response.contains("Pipeline configuration completed successfully!") &&
            response.contains("Webhook Data Pipeline")
        )
        .verifyComplete();
    }
}