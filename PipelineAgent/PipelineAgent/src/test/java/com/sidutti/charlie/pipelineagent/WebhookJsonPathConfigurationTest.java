package com.sidutti.charlie.pipelineagent;

import com.sidutti.charlie.pipelineagent.model.JsonPathConfig;
import com.sidutti.charlie.pipelineagent.model.WebhookConfig;
import com.sidutti.charlie.pipelineagent.service.ConfigurationAgentService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class WebhookJsonPathConfigurationTest {

    @Autowired
    private ChatModel chatModel;

    @Test
    public void testWebhookJsonPathConfiguration() {
        ChatClient.Builder builder = ChatClient.builder(chatModel);
        ConfigurationAgentService service = new ConfigurationAgentService(builder);

        String sessionId = "test-webhook-json-path";

        // Test complete webhook configuration with JSON paths
        StepVerifier.create(
            service.startConfiguration(sessionId, "webhook")
                // Basic webhook config
                .then(service.processAnswer(sessionId, "https://api.example.com/webhook"))
                .then(service.processAnswer(sessionId, "POST"))
                .then(service.processAnswer(sessionId, "Content-Type:application/json"))
                .then(service.processAnswer(sessionId, "application/json"))
                .then(service.processAnswer(sessionId, "bearer"))
                .then(service.processAnswer(sessionId, "sk-token-123"))
                .then(service.processAnswer(sessionId, "none"))
                .then(service.processAnswer(sessionId, "none"))
                .then(service.processAnswer(sessionId, "none"))
                .then(service.processAnswer(sessionId, "none"))
                .then(service.processAnswer(sessionId, "yes"))
                .then(service.processAnswer(sessionId, "30"))
                .then(service.processAnswer(sessionId, "3"))
                // JSON Path config
                .then(service.processAnswer(sessionId, "$.content"))
                .then(service.processAnswer(sessionId, "$.id"))
                .then(service.processAnswer(sessionId, "$.timestamp"))
                .then(service.processAnswer(sessionId, "ISO8601"))
                .then(service.processAnswer(sessionId, "author:$.author,category:$.category"))
                .then(service.processAnswer(sessionId, "no"))
                // Target config
                .then(service.processAnswer(sessionId, "sk-api-key"))
                .then(service.processAnswer(sessionId, "Blog Content Pipeline"))
        )
        .expectNextMatches(response -> {
            System.out.println("Final response: " + response);
            return response.contains("Pipeline configuration completed successfully!") &&
                   response.contains("Blog Content Pipeline");
        })
        .verifyComplete();
    }

    @Test
    public void testWebhookJsonPathWithArrayFlattening() {
        ChatClient.Builder builder = ChatClient.builder(chatModel);
        ConfigurationAgentService service = new ConfigurationAgentService(builder);

        String sessionId = "test-webhook-array-flatten";

        // Test webhook with array flattening
        StepVerifier.create(
            service.startConfiguration(sessionId, "webhook")
                .then(service.processAnswer(sessionId, "https://api.messages.com/webhook"))
                .then(service.processAnswer(sessionId, "POST"))
                .then(service.processAnswer(sessionId, "none"))
                .then(service.processAnswer(sessionId, "application/json"))
                .then(service.processAnswer(sessionId, "none"))
                .then(service.processAnswer(sessionId, "none"))
                .then(service.processAnswer(sessionId, "none"))
                .then(service.processAnswer(sessionId, "none"))
                .then(service.processAnswer(sessionId, "none"))
                .then(service.processAnswer(sessionId, "none"))
                .then(service.processAnswer(sessionId, "yes"))
                .then(service.processAnswer(sessionId, "60"))
                .then(service.processAnswer(sessionId, "5"))
                // JSON Path for message arrays
                .then(service.processAnswer(sessionId, "$.messages[*].content"))
                .then(service.processAnswer(sessionId, "$.messages[*].id"))
                .then(service.processAnswer(sessionId, "$.messages[*].timestamp"))
                .then(service.processAnswer(sessionId, "epoch"))
                .then(service.processAnswer(sessionId, "sender:$.messages[*].sender,type:$.messages[*].type"))
                .then(service.processAnswer(sessionId, "yes")) // Flatten arrays
        )
        .expectNextMatches(response ->
            response.contains("Now let's configure the target") &&
            response.contains("API key")
        )
        .verifyComplete();
    }

    @Test
    public void testWebhookJsonPathMinimalConfig() {
        ChatClient.Builder builder = ChatClient.builder(chatModel);
        ConfigurationAgentService service = new ConfigurationAgentService(builder);

        String sessionId = "test-webhook-minimal";

        // Test minimal webhook configuration with only required JSON path
        StepVerifier.create(
            service.startConfiguration(sessionId, "webhook")
                .then(service.processAnswer(sessionId, "https://simple.api.com/data"))
                .then(service.processAnswer(sessionId, "POST"))
                .then(service.processAnswer(sessionId, "none"))
                .then(service.processAnswer(sessionId, "application/json"))
                .then(service.processAnswer(sessionId, "none"))
                .then(service.processAnswer(sessionId, "none"))
                .then(service.processAnswer(sessionId, "none"))
                .then(service.processAnswer(sessionId, "none"))
                .then(service.processAnswer(sessionId, "none"))
                .then(service.processAnswer(sessionId, "none"))
                .then(service.processAnswer(sessionId, "no"))
                .then(service.processAnswer(sessionId, "10"))
                .then(service.processAnswer(sessionId, "1"))
                // Minimal JSON path - only corpus text required
                .then(service.processAnswer(sessionId, "$.text"))
                .then(service.processAnswer(sessionId, "none"))
                .then(service.processAnswer(sessionId, "none"))
                .then(service.processAnswer(sessionId, "none"))
                .then(service.processAnswer(sessionId, "none"))
                .then(service.processAnswer(sessionId, "no"))
        )
        .expectNextMatches(response ->
            response.contains("Now let's configure the target")
        )
        .verifyComplete();
    }

    @Test
    public void testJsonPathConfigCreation() {
        // Test that JsonPathConfig is properly created with expected values
        JsonPathConfig config = new JsonPathConfig();
        config.setCorpusTextPath("$.document.content");
        config.setIdPath("$.document.id");
        config.setTimestampPath("$.document.created_at");
        config.setTimestampFormat("ISO8601");
        config.setFlattenArrays(true);

        assertEquals("$.document.content", config.getCorpusTextPath());
        assertEquals("$.document.id", config.getIdPath());
        assertEquals("$.document.created_at", config.getTimestampPath());
        assertEquals("ISO8601", config.getTimestampFormat());
        assertTrue(config.isFlattenArrays());

        assertNotNull(config.toString());
        assertTrue(config.toString().contains("corpusTextPath"));
    }

    @Test
    public void testWebhookConfigWithJsonPaths() {
        // Test WebhookConfig integration with JsonPathConfig
        WebhookConfig webhookConfig = new WebhookConfig();
        webhookConfig.setEndpoint("https://test.com/webhook");

        JsonPathConfig jsonConfig = new JsonPathConfig();
        jsonConfig.setCorpusTextPath("$.content");
        jsonConfig.setIdPath("$.id");

        webhookConfig.setJsonPathConfig(jsonConfig);

        assertNotNull(webhookConfig.getJsonPathConfig());
        assertEquals("$.content", webhookConfig.getJsonPathConfig().getCorpusTextPath());
        assertEquals("$.id", webhookConfig.getJsonPathConfig().getIdPath());
        assertEquals("webhook", webhookConfig.getType());
    }

    @Test
    public void testComplexNestedJsonPathConfig() {
        ChatClient.Builder builder = ChatClient.builder(chatModel);
        ConfigurationAgentService service = new ConfigurationAgentService(builder);

        String sessionId = "test-complex-nested";

        // Test complex nested JSON structure
        StepVerifier.create(
            service.startConfiguration(sessionId, "webhook")
                .then(service.processAnswer(sessionId, "https://complex.api.com/docs"))
                .then(service.processAnswer(sessionId, "POST"))
                .then(service.processAnswer(sessionId, "Authorization:Bearer token123"))
                .then(service.processAnswer(sessionId, "application/json"))
                .then(service.processAnswer(sessionId, "bearer"))
                .then(service.processAnswer(sessionId, "bearer-token-456"))
                .then(service.processAnswer(sessionId, "none"))
                .then(service.processAnswer(sessionId, "none"))
                .then(service.processAnswer(sessionId, "none"))
                .then(service.processAnswer(sessionId, "none"))
                .then(service.processAnswer(sessionId, "yes"))
                .then(service.processAnswer(sessionId, "45"))
                .then(service.processAnswer(sessionId, "2"))
                // Complex nested paths
                .then(service.processAnswer(sessionId, "$.document.body.full_text"))
                .then(service.processAnswer(sessionId, "$.document.uuid"))
                .then(service.processAnswer(sessionId, "$.document.metadata.created"))
                .then(service.processAnswer(sessionId, "yyyy-MM-dd HH:mm:ss"))
                .then(service.processAnswer(sessionId, "title:$.document.metadata.title,authors:$.document.metadata.authors,department:$.document.metadata.department"))
                .then(service.processAnswer(sessionId, "no"))
        )
        .expectNextMatches(response ->
            response.contains("Now let's configure the target")
        )
        .verifyComplete();
    }
}