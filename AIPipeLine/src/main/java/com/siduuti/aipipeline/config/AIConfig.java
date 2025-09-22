package com.siduuti.aipipeline.config;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vertexai.Transport;
import com.google.cloud.vertexai.VertexAI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.vertexai.autoconfigure.gemini.VertexAiGeminiConnectionProperties;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.scheduler.Scheduler;
import reactor.netty.http.client.HttpClient;
import reactor.scheduler.forkjoin.ForkJoinPoolScheduler;

import java.io.IOException;
import java.util.List;

@Configuration
public class AIConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(AIConfig.class);

    @Bean("forkJoinScheduler")
    public Scheduler forkJoinScheduler() {
        return ForkJoinPoolScheduler.create("distributor");
    }

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create().followRedirect(true)
                ))
                .codecs(codecs -> codecs
                        .defaultCodecs()
                        .maxInMemorySize(2048 * 1024))
                .build();
    }

    @Bean
    public TokenTextSplitter tokenTextSplitter() {
        return
                TokenTextSplitter.builder()
                        .withChunkSize(512) // The target token size for each chunk
                        .withKeepSeparator(false)
                        .build();

    }

    @Bean
    public ObjectMapper objectMapper() {
        LOGGER.info("Creating ObjectMapper");
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.FAIL_ON_SELF_REFERENCES, false)
                .registerModule(new JavaTimeModule());
    }

    @Bean
    public ElasticsearchAsyncClient elasticsearchAsyncClient(org.elasticsearch.client.RestClient restClient, ObjectMapper objectMapper) {
        LOGGER.info("Creating ElasticsearchAsyncClient");
        return new ElasticsearchAsyncClient(new RestClientTransport(restClient, new JacksonJsonpMapper(objectMapper)));
    }

    @Bean
    public VertexAI vertexAi(VertexAiGeminiConnectionProperties connectionProperties) throws IOException {
        LOGGER.info("Creating vertexAi");
        Assert.hasText(connectionProperties.getProjectId(), "Vertex AI project-id must be set!");
        Assert.hasText(connectionProperties.getLocation(), "Vertex AI location must be set!");
        Assert.notNull(connectionProperties.getTransport(), "Vertex AI transport must be set!");

        var vertexAIBuilder = new VertexAI.Builder().setProjectId(connectionProperties.getProjectId())
                .setLocation(connectionProperties.getLocation())
                .setTransport(Transport.valueOf(connectionProperties.getTransport().name()));

        if (StringUtils.hasText(connectionProperties.getApiEndpoint())) {
            vertexAIBuilder.setApiEndpoint(connectionProperties.getApiEndpoint());
        }
        if (!CollectionUtils.isEmpty(connectionProperties.getScopes())) {
            vertexAIBuilder.setScopes(connectionProperties.getScopes());
        }

        if (connectionProperties.getCredentialsUri() != null) {
            GoogleCredentials credentials = GoogleCredentials
                    .fromStream(connectionProperties.getCredentialsUri().getInputStream())
                    .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));

            vertexAIBuilder.setCredentials(credentials);
        }
        return vertexAIBuilder.build();
    }

//    @Bean
//    public VertexAiEmbeddingConnectionDetails connectionDetails(CredentialsProvider credentialsProvider,
//                                                                VertexAiEmbeddingConnectionProperties connectionProperties) throws IOException {
//        PredictionServiceSettings.Builder predictionServiceSettingsBuilder =
//                PredictionServiceSettings.newBuilder();
//        predictionServiceSettingsBuilder
//                .setCredentialsProvider(credentialsProvider)
//                .predictSettings()
//                .setRetrySettings(
//                        predictionServiceSettingsBuilder
//                                .predictSettings()
//                                .getRetrySettings()
//                                .toBuilder()
//                                .setInitialRetryDelayDuration(Duration.ofSeconds(1))
//                                .setInitialRpcTimeoutDuration(Duration.ofSeconds(5))
//                                .setMaxAttempts(5)
//                                .setMaxRetryDelayDuration(Duration.ofSeconds(30))
//                                .setMaxRpcTimeoutDuration(Duration.ofSeconds(60))
//                                .setRetryDelayMultiplier(1.3)
//                                .setRpcTimeoutMultiplier(1.5)
//                                .setTotalTimeoutDuration(Duration.ofSeconds(300))
//                                .build());
//        PredictionServiceSettings predictionServiceSettings = predictionServiceSettingsBuilder.build();
//       return  new VertexAiEmbeddingConnectionDetails(connectionProperties.getProjectId(),
//                connectionProperties.getLocation(),
//                "google",
//                predictionServiceSettings);
//    }
//
//    @Bean
//    public CredentialsProvider googleCredentials(VertexAiEmbeddingConnectionProperties connectionProperties) throws IOException {
//        GoogleCredentials credentials =
//                GoogleCredentials
//                .fromStream(connectionProperties.getCredentialsUri().getInputStream())
//                .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));
//        return FixedCredentialsProvider.create(credentials);
//    }

    @Bean
    public ChatClient vertexChatClient(@Qualifier("vertexAiGeminiChat") ChatModel chatModel) {
        return ChatClient.create(chatModel);
    }

    @Bean
    public ChatClient ollamaChatClient(@Qualifier("ollamaChatModel") ChatModel chatModel) {
        return ChatClient.create(chatModel);
    }


}
