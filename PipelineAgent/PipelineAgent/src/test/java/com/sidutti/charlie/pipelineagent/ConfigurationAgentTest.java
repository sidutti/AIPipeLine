package com.sidutti.charlie.pipelineagent;

import com.sidutti.charlie.pipelineagent.service.ConfigurationAgentService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

@SpringBootTest
public class ConfigurationAgentTest {

    @Test
    public void testConfluenceConfiguration() {
        ChatClient.Builder builder = ChatClient.builder();
        ConfigurationAgentService service = new ConfigurationAgentService(builder);

        String sessionId = "test-session";

        StepVerifier.create(service.startConfiguration(sessionId, "confluence"))
                .expectNext("What is the base URL of your Confluence instance? (e.g., https://yourcompany.atlassian.net)")
                .verifyComplete();
    }

    @Test
    public void testGetAvailableDataSources() {
        ChatClient.Builder builder = ChatClient.builder();
        ConfigurationAgentService service = new ConfigurationAgentService(builder);

        StepVerifier.create(service.getAvailableDataSources())
                .expectNextMatches(list -> list.contains("confluence") &&
                                          list.contains("kafka") &&
                                          list.contains("csv") &&
                                          list.size() == 8)
                .verifyComplete();
    }

    @Test
    public void testProcessAnswerFlow() {
        ChatClient.Builder builder = ChatClient.builder();
        ConfigurationAgentService service = new ConfigurationAgentService(builder);

        String sessionId = "test-session-2";

        StepVerifier.create(
                service.startConfiguration(sessionId, "csv")
                        .then(service.processAnswer(sessionId, "/path/to/file.csv"))
                        .then(service.processAnswer(sessionId, ","))
                        .then(service.processAnswer(sessionId, "\""))
                        .then(service.processAnswer(sessionId, "\\"))
                        .then(service.processAnswer(sessionId, "yes"))
        )
        .expectNextMatches(response -> response.contains("comma-separated or 'auto-detect'"))
        .verifyComplete();
    }
}