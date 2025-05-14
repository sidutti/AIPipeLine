package com.siduuti.aipipeline.dto.repository;

import com.siduuti.aipipeline.dto.StoredPrompt;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface StoredPromptRepository extends ReactiveMongoRepository<StoredPrompt, String> {
}
