package com.sidutti.charlie.aipipeline.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.scheduler.Scheduler;
import reactor.netty.http.client.HttpClient;
import reactor.scheduler.forkjoin.ForkJoinPoolScheduler;

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
    public ChatClient openAiChatClient(ChatModel chatModel) {
        return ChatClient.create(chatModel);
    }


}
