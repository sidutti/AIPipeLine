package com.sidutti.charlie.aipipeline.service.agents;

import com.sidutti.charlie.aipipeline.dto.StoredPrompt;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class EvaluatorAgent {


    private final ChatClient openAiChatClient;

    public EvaluatorAgent(@Qualifier("ollamaChatClient") ChatClient openAiChatClient) {
        this.openAiChatClient = openAiChatClient;
    }

    private EvaluationResponse evaluate(String content, StoredPrompt storedPrompt) {

        EvaluationResponse evaluationResponse = openAiChatClient.prompt()
                .user(u -> u.text("{prompt}\nOriginal task: {task}\nContent to evaluate: {content}")
                        .param("prompt", storedPrompt.userPrompt())
                        .param("task",storedPrompt.systemPrompt())
                        .param("content", content))
                .call()
                .entity(EvaluationResponse.class);
        Mono.just("as").subscribeOn(Schedulers.boundedElastic()).block();
        System.out.printf("\n=== EVALUATOR OUTPUT ===\nEVALUATION: %s\n\nFEEDBACK: %s\n%n",
                evaluationResponse.getScore(), evaluationResponse.getFeedback());
        return evaluationResponse;
    }
}
