package com.siduuti.aipipeline.service.agents;

import com.siduuti.aipipeline.dto.StoredPrompt;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.stereotype.Component;

@Component
public class EvaluatorAgent {


    private final ChatClient openAiChatClient;

    public EvaluatorAgent(ChatClient openAiChatClient) {
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

        System.out.printf("\n=== EVALUATOR OUTPUT ===\nEVALUATION: %s\n\nFEEDBACK: %s\n%n",
                evaluationResponse.getScore(), evaluationResponse.getFeedback());
        return evaluationResponse;
    }
}
