package com.sidutti.charlie.aipipeline.controller;

import com.sidutti.charlie.aipipeline.service.ConfigurationAgentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/configuration")
@CrossOrigin(origins = "*")
public class ConfigurationController {

    private final ConfigurationAgentService configurationService;

    public ConfigurationController(ConfigurationAgentService configurationService) {
        this.configurationService = configurationService;
    }

    @GetMapping("/datasources")
    public Mono<ResponseEntity<List<String>>> getAvailableDataSources() {
        return configurationService.getAvailableDataSources()
                .map(ResponseEntity::ok);
    }

    @PostMapping("/start")
    public Mono<ResponseEntity<Map<String, Object>>> startConfiguration(@RequestBody StartConfigurationRequest request) {
        String sessionId = UUID.randomUUID().toString();
        return configurationService.startConfiguration(sessionId, request.getDataSourceType())
                .map(question -> ResponseEntity.ok(Map.of(
                        "sessionId", sessionId,
                        "question", question,
                        "dataSourceType", request.getDataSourceType()
                )));
    }

    @PostMapping("/answer")
    public Mono<ResponseEntity<Map<String, Object>>> processAnswer(@RequestBody AnswerRequest request) {
        return configurationService.processAnswer(request.getSessionId(), request.getAnswer())
                .flatMap(response -> {
                    if (response.contains("Pipeline configuration completed successfully!")) {
                        return Mono.just(ResponseEntity.ok(Map.of(
                                "completed", true,
                                "message", response,
                                "phase", "completed"
                        )));
                    } else if (response.contains("Now let's configure the target")) {
                        return configurationService.getConfigurationStatus(request.getSessionId())
                                .map(status -> ResponseEntity.ok(Map.of(
                                        "completed", false,
                                        "question", response,
                                        "status", status,
                                        "phase", "target"
                                )));
                    } else {
                        return configurationService.getConfigurationStatus(request.getSessionId())
                                .map(status -> ResponseEntity.ok(Map.of(
                                        "completed", false,
                                        "question", response,
                                        "status", status,
                                        "phase", "source"
                                )));
                    }
                });
    }

    @GetMapping("/status/{sessionId}")
    public Mono<ResponseEntity<Map<String, String>>> getConfigurationStatus(@PathVariable String sessionId) {
        return configurationService.getConfigurationStatus(sessionId)
                .map(status -> ResponseEntity.ok(Map.of("status", status)));
    }

    @GetMapping("/help/{dataSourceType}")
    public Mono<ResponseEntity<Map<String, Object>>> getDataSourceHelp(@PathVariable String dataSourceType) {
        return Mono.just(ResponseEntity.ok(Map.of(
                "dataSourceType", dataSourceType,
                "description", getDataSourceDescription(dataSourceType),
                "requiredFields", getRequiredFields(dataSourceType)
        )));
    }

    private String getDataSourceDescription(String dataSourceType) {
        return switch (dataSourceType.toLowerCase()) {
            case "confluence" -> "Connect to Atlassian Confluence to index wiki pages, attachments, and documentation";
            case "ecm" -> "Connect to Enterprise Content Management repositories like Git, SVN, or Mercurial";
            case "sharepoint" -> "Connect to Microsoft SharePoint to index documents, lists, and libraries";
            case "objectstorage" -> "Connect to object storage services like AWS S3, Azure Blob, Google Cloud Storage, or HPOS";
            case "urllist" -> "Crawl and index content from a list of web URLs with customizable depth and filters";
            case "csv" -> "Process and index data from CSV files with configurable parsing options";
            case "kafka" -> "Stream and index real-time data from Apache Kafka topics";
            case "webhook" -> "Receive and index data through HTTP webhooks with configurable authentication and JSON path extraction for flexible data parsing";
            default -> "Unknown data source type";
        };
    }

    private List<String> getRequiredFields(String dataSourceType) {
        return switch (dataSourceType.toLowerCase()) {
            case "confluence" -> List.of("baseUrl", "username", "apiToken");
            case "ecm" -> List.of("repoType", "repoUrl");
            case "sharepoint" -> List.of("siteUrl", "tenantId", "clientId", "clientSecret");
            case "objectstorage" -> List.of("storageType", "endpoint", "accessKey", "secretKey", "bucketName");
            case "urllist" -> List.of("urls");
            case "csv" -> List.of("filePath", "delimiter");
            case "kafka" -> List.of("bootstrapServers", "topicName", "consumerGroupId");
            case "webhook" -> List.of("endpoint", "httpMethod", "corpusTextPath");
            default -> List.of();
        };
    }

    public static class StartConfigurationRequest {
        private String dataSourceType;

        public String getDataSourceType() { return dataSourceType; }
        public void setDataSourceType(String dataSourceType) { this.dataSourceType = dataSourceType; }
    }

    public static class AnswerRequest {
        private String sessionId;
        private String answer;

        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }

        public String getAnswer() { return answer; }
        public void setAnswer(String answer) { this.answer = answer; }
    }
}