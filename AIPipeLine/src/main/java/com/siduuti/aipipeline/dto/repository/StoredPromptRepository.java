package com.siduuti.aipipeline.dto.repository;

import com.siduuti.aipipeline.dto.StoredPrompt;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;

public interface StoredPromptRepository extends ReactiveElasticsearchRepository<StoredPrompt, String> {
}
