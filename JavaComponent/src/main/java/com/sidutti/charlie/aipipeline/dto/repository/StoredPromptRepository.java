package com.sidutti.charlie.aipipeline.dto.repository;

import com.sidutti.charlie.aipipeline.dto.StoredPrompt;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;

public interface StoredPromptRepository extends ReactiveElasticsearchRepository<StoredPrompt, String> {
}
