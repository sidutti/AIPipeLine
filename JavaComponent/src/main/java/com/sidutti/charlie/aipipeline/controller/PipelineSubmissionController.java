package com.sidutti.charlie.aipipeline.controller;

import com.sidutti.charlie.aipipeline.dto.playground.UiPipelineConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/pipeline")
@CrossOrigin(origins = "*")
public class PipelineSubmissionController {

    @PostMapping("/submit")
    public Mono<ResponseEntity<Map<String, Object>>> submitPipeline(@RequestBody UiPipelineConfig config) {
        // In a real implementation, you would validate and persist the config,
        // and trigger pipeline setup/execution here.
        String pipelineId = "pipeline_" + UUID.randomUUID();

        return Mono.just(ResponseEntity.ok(Map.of(
                "success", true,
                "pipelineId", pipelineId,
                "message", "Pipeline configuration received and queued for processing"
        )));
    }
}
