package com.siduuti.aipipeline.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class ClassificationService {

    private final ChatClient client;
    private static final String USER_PROMPT = """
            Analyze the following  documents and classify them into a Category:
            
            Sample document content:
            {sample_text}
            
            Based on the content and key terms, classify these documents and provide:
                    1. The most appropriate category
                    2. A confidence score from 0.0 to 1.0
                    3. A brief explanation (one sentence)
            
            Respond in this exact JSON format provided
            {
              "title": "DocumentClassificationResult",
              "description": "Describes the output of a document classification process.",
              "type": "object",
              "properties": {
                "Category": {
                  "description": "The assigned category for the document.",
                  "type": "string"
                },
                "confidence": {
                  "description": "The confidence score of the classification, ranging from 0.0 to 1.0.",
                  "type": "number",
                  "minimum": 0.0,
                  "maximum": 1.0
                },
                "explanation": {
                  "description": "A brief justification for the assigned category.",
                  "type": "string"
                }
              },
              "required": [
                "Category",
                "confidence",
                "explanation"
              ]
            }
            """;

    public ClassificationService(@Qualifier("ollamaChatClient") ChatClient client) {
        this.client = client;
    }

    public String Classify(String documentContents) {
       return client.prompt()
               .user(USER_PROMPT.replace("{sample_text}", documentContents))
               .system("You are a Document classifier expert")
               .call()
               .content();
    }
}
