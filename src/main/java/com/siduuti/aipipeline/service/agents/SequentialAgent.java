package com.siduuti.aipipeline.service.agents;

import com.siduuti.aipipeline.dto.StoredPrompt;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.ResponseFormat;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SequentialAgent {

    private final ChatClient openAiChatClient;

    public SequentialAgent(ChatClient openAiChatClient) {
        this.openAiChatClient = openAiChatClient;
    }

    private ResponseFormat responseFormat(String schema) {
        ResponseFormat.JsonSchema jsonSchema = ResponseFormat.JsonSchema.builder()
                .name("JSON_SCHEMA")
                .strict(true)
                .schema(schema)
                .build();
        return ResponseFormat.builder()
                .type(ResponseFormat.Type.JSON_SCHEMA)
                .jsonSchema(jsonSchema)
                .build();
    }

    public String call(String userInput, List<StoredPrompt> prompts) {

        String response = userInput;

        for (StoredPrompt prompt : prompts) {
            response = callLLM(response, prompt);

        }

        return response;
    }

    private String callLLM(String response, StoredPrompt prompt) {
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .responseFormat(responseFormat(prompt.promptOutput()))
                .build();
        // 2. Call the chat client with the new input and get the new response.
        return openAiChatClient.prompt()
                .user(response)
                .system(prompt.systemPrompt())
                .options(options)
                .call().content();
    }
}
