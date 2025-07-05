package com.siduuti.aipipeline.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "prompts")
public record StoredPrompt(@Id String id,
                           String userPrompt,
                           String systemPrompt,
                           String promptOutput,
                           String intent,
                           String useCase,
                           int orderInChain) {


}
