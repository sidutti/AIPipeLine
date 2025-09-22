package com.siduuti.aipipeline.service;

import com.sidutti.charlie.pipelineagent.model.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConfigurationAgentService {

    private final ChatClient chatClient;
    private final Map<String, ConfigurationSession> sessions = new HashMap<>();

    public ConfigurationAgentService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public Mono<String> startConfiguration(String sessionId, String dataSourceType) {
        ConfigurationSession session = new ConfigurationSession();
        session.setDataSourceType(dataSourceType);
        session.setCurrentStep(0);
        sessions.put(sessionId, session);

        return getNextQuestion(sessionId);
    }

    public Mono<String> processAnswer(String sessionId, String answer) {
        ConfigurationSession session = sessions.get(sessionId);
        if (session == null) {
            return Mono.just("Session not found. Please start a new configuration.");
        }

        if (session.isTargetConfigStarted()) {
            session.getTargetAnswers().put(session.getCurrentStep(), answer);
        } else {
            session.getAnswers().put(session.getCurrentStep(), answer);
        }

        session.setCurrentStep(session.getCurrentStep() + 1);

        if (isSourceConfigurationComplete(session) && !session.isTargetConfigStarted()) {
            session.setSourceConfig(buildSourceConfiguration(session));
            session.setTargetConfigStarted(true);
            session.setCurrentStep(0);
            return Mono.just("Great! Source configuration is complete. Now let's configure the target for your vectoring pipeline.\n\nWhat is your API key for the custom vectoring service?");
        }

        if (session.isTargetConfigStarted() && isTargetConfigurationComplete(session)) {
            TargetConfig targetConfig = buildTargetConfiguration(session);
            PipelineConfig finalConfig = new PipelineConfig(session.getSourceConfig(), targetConfig, sessionId);
            return Mono.just("✅ Pipeline configuration completed successfully!\n\nFinal Configuration:\n" + finalConfig.toString());
        }

        return getNextQuestion(sessionId);
    }

    private Mono<String> getNextQuestion(String sessionId) {
        ConfigurationSession session = sessions.get(sessionId);

        if (session.isTargetConfigStarted()) {
            List<String> targetQuestions = getTargetQuestions();
            if (session.getCurrentStep() >= targetQuestions.size()) {
                return Mono.just("All target questions completed!");
            }
            return Mono.just(targetQuestions.get(session.getCurrentStep()));
        } else {
            List<String> sourceQuestions = getQuestionsForDataSource(session.getDataSourceType());
            if (session.getCurrentStep() >= sourceQuestions.size()) {
                return Mono.just("All source questions completed!");
            }
            return Mono.just(sourceQuestions.get(session.getCurrentStep()));
        }
    }

    private List<String> getTargetQuestions() {
        return List.of(
            "What is your API key for the custom vectoring service?",
            "What is the use case name for this vectoring pipeline?"
        );
    }

    private List<String> getQuestionsForDataSource(String dataSourceType) {
        return switch (dataSourceType.toLowerCase()) {
            case "confluence" -> List.of(
                "What is the base URL of your Confluence instance? (e.g., https://yourcompany.atlassian.net)",
                "What is your Confluence username/email?",
                "Please provide your Confluence API token:",
                "Which space keys would you like to index? (comma-separated, or 'ALL' for all spaces)",
                "Should we include attachments? (yes/no)",
                "What is the maximum number of pages to process? (or 'unlimited')",
                "Any date filter for last modified content? (e.g., '2024-01-01' or 'none')"
            );
            case "ecm" -> List.of(
                "What type of repository? (git, svn, mercurial)",
                "What is the repository URL?",
                "Which branch/tag should we use? (default: main/master)",
                "Username for authentication (if required):",
                "Password or token for authentication (if required):",
                "SSH key path (if using SSH authentication):",
                "Which paths should we include? (comma-separated, or 'ALL')",
                "Which paths should we exclude? (comma-separated, or 'none')",
                "Which file extensions to process? (e.g., .java,.py,.md or 'ALL')",
                "Should we include version history? (yes/no)",
                "Maximum number of commits to analyze? (or 'unlimited')"
            );
            case "sharepoint" -> List.of(
                "What is your SharePoint site URL?",
                "What is your tenant ID?",
                "What is your client ID (app registration)?",
                "What is your client secret?",
                "Which document libraries should we access? (comma-separated or 'ALL')",
                "Which folder paths to include? (comma-separated or 'ALL')",
                "Which file types to process? (e.g., docx,pdf,txt or 'ALL')",
                "Should we include file metadata? (yes/no)",
                "Should we include version history? (yes/no)",
                "Any date filter for last modified files? (e.g., '2024-01-01' or 'none')"
            );
            case "objectstorage" -> List.of(
                "What type of object storage? (S3, Azure Blob, GCS, HPOS)",
                "What is the storage endpoint URL?",
                "What is your access key?",
                "What is your secret key?",
                "What is the bucket/container name?",
                "What region is the storage in?",
                "Which prefixes/folders to include? (comma-separated or 'ALL')",
                "Which file extensions to process? (e.g., .pdf,.docx,.txt or 'ALL')",
                "Should we include object metadata? (yes/no)",
                "Any date filter for last modified files? (e.g., '2024-01-01' or 'none')",
                "Maximum file size to process in MB? (or 'unlimited')"
            );
            case "urllist" -> List.of(
                "Please provide the list of URLs (comma-separated):",
                "How deep should we crawl each site? (0 = just the URL, 1 = one level deep, etc.)",
                "Should we follow external links? (yes/no)",
                "Which domains are allowed for crawling? (comma-separated or 'same-domain')",
                "Any URL patterns to exclude? (regex patterns, comma-separated or 'none')",
                "Any custom headers needed? (format: 'Header1:Value1,Header2:Value2' or 'none')",
                "Custom User-Agent string? (or 'default')",
                "Request delay between pages in seconds? (recommended: 1-5)",
                "Maximum pages to crawl per site? (or 'unlimited')",
                "Should we respect robots.txt? (yes/no)"
            );
            case "csv" -> List.of(
                "What is the file path or upload location?",
                "What is the delimiter character? (comma, semicolon, tab, etc.)",
                "What is the quote character? (usually double-quote)",
                "What is the escape character? (usually backslash)",
                "Does the file have a header row? (yes/no)",
                "How should columns be mapped? (provide column names comma-separated or 'auto-detect')",
                "What is the file encoding? (UTF-8, ISO-8859-1, etc.)",
                "How many lines to skip at the beginning? (usually 0)",
                "Maximum number of records to process? (or 'all')",
                "What date format is used? (e.g., 'yyyy-MM-dd' or 'none')",
                "Which columns are required? (comma-separated or 'all')"
            );
            case "kafka" -> List.of(
                "What are the Kafka bootstrap servers? (comma-separated)",
                "What is the topic name to consume from?",
                "What is the consumer group ID?",
                "Offset reset strategy? (earliest, latest, none)",
                "Key deserializer class? (or 'default')",
                "Value deserializer class? (or 'default')",
                "Any additional Kafka properties? (format: 'key1:value1,key2:value2' or 'none')",
                "Should we enable auto-commit? (yes/no)",
                "Auto-commit interval in milliseconds? (default: 5000)",
                "Session timeout in milliseconds? (default: 10000)",
                "Security protocol? (PLAINTEXT, SSL, SASL_PLAINTEXT, SASL_SSL)",
                "SASL mechanism? (if using SASL)",
                "SASL username? (if required)",
                "SASL password? (if required)"
            );
            case "webhook" -> List.of(
                "What is the webhook endpoint URL?",
                "What HTTP method should we use? (POST, PUT, PATCH)",
                "Any custom headers needed? (format: 'Header1:Value1,Header2:Value2' or 'none')",
                "What content type? (application/json, application/xml, etc.)",
                "What authentication type? (none, bearer, basic, apikey)",
                "Authentication token/bearer token? (if using bearer auth)",
                "Username? (if using basic auth)",
                "Password? (if using basic auth)",
                "API key? (if using API key auth)",
                "API key header name? (if using API key auth)",
                "Should we validate SSL certificates? (yes/no)",
                "Timeout in seconds? (default: 30)",
                "Maximum retry attempts? (default: 3)",
                "What JSON path should we use to extract the main corpus text? (e.g., '$.content' or '$.data.message')",
                "What is the JSON path for the document/record ID? (e.g., '$.id' or '$.document.uuid' or 'none')",
                "What JSON path should we use for timestamps? (e.g., '$.timestamp' or '$.created_at' or 'none')",
                "What format is the timestamp in? (e.g., 'ISO8601', 'epoch', 'yyyy-MM-dd HH:mm:ss' or 'none')",
                "What metadata fields do you want to extract? (format: 'fieldName:$.json.path,author:$.user.name' or 'none')",
                "Should we flatten JSON arrays into separate records? (yes/no)"
            );
            default -> List.of("Unknown data source type. Please specify: confluence, ecm, sharepoint, objectstorage, urllist, csv, kafka, or webhook");
        };
    }

    private boolean isSourceConfigurationComplete(ConfigurationSession session) {
        List<String> questions = getQuestionsForDataSource(session.getDataSourceType());
        return session.getCurrentStep() >= questions.size();
    }

    private boolean isTargetConfigurationComplete(ConfigurationSession session) {
        List<String> targetQuestions = getTargetQuestions();
        return session.getCurrentStep() >= targetQuestions.size();
    }

    private DataSourceConfig buildSourceConfiguration(ConfigurationSession session) {
        Map<Integer, String> answers = session.getAnswers();
        String type = session.getDataSourceType();

        return switch (type.toLowerCase()) {
            case "confluence" -> buildConfluenceConfig(answers);
            case "ecm" -> buildEcmRepoConfig(answers);
            case "sharepoint" -> buildSharepointConfig(answers);
            case "objectstorage" -> buildObjectStorageConfig(answers);
            case "urllist" -> buildUrlListConfig(answers);
            case "csv" -> buildCsvConfig(answers);
            case "kafka" -> buildKafkaConfig(answers);
            case "webhook" -> buildWebhookConfig(answers);
            default -> null;
        };
    }

    private ConfluenceConfig buildConfluenceConfig(Map<Integer, String> answers) {
        ConfluenceConfig config = new ConfluenceConfig();
        config.setBaseUrl(answers.get(0));
        config.setUsername(answers.get(1));
        config.setApiToken(answers.get(2));

        String spaceKeys = answers.get(3);
        if (!"ALL".equalsIgnoreCase(spaceKeys)) {
            config.setSpaceKeys(List.of(spaceKeys.split(",")));
        }

        config.setIncludeAttachments("yes".equalsIgnoreCase(answers.get(4)));

        String maxPages = answers.get(5);
        if (!"unlimited".equalsIgnoreCase(maxPages)) {
            config.setMaxPages(Integer.parseInt(maxPages));
        }

        String dateFilter = answers.get(6);
        if (!"none".equalsIgnoreCase(dateFilter)) {
            config.setLastModifiedFilter(dateFilter);
        }

        return config;
    }

    private EcmRepoConfig buildEcmRepoConfig(Map<Integer, String> answers) {
        EcmRepoConfig config = new EcmRepoConfig();
        config.setRepoType(answers.get(0));
        config.setRepoUrl(answers.get(1));
        config.setBranch(answers.get(2));
        config.setUsername(answers.get(3));
        config.setPassword(answers.get(4));
        config.setSshKey(answers.get(5));

        String includePaths = answers.get(6);
        if (!"ALL".equalsIgnoreCase(includePaths)) {
            config.setIncludePaths(List.of(includePaths.split(",")));
        }

        String excludePaths = answers.get(7);
        if (!"none".equalsIgnoreCase(excludePaths)) {
            config.setExcludePaths(List.of(excludePaths.split(",")));
        }

        String extensions = answers.get(8);
        if (!"ALL".equalsIgnoreCase(extensions)) {
            config.setFileExtensions(List.of(extensions.split(",")));
        }

        config.setIncludeHistory("yes".equalsIgnoreCase(answers.get(9)));

        String maxCommits = answers.get(10);
        if (!"unlimited".equalsIgnoreCase(maxCommits)) {
            config.setMaxCommits(Integer.parseInt(maxCommits));
        }

        return config;
    }

    private SharepointConfig buildSharepointConfig(Map<Integer, String> answers) {
        SharepointConfig config = new SharepointConfig();
        config.setSiteUrl(answers.get(0));
        config.setTenantId(answers.get(1));
        config.setClientId(answers.get(2));
        config.setClientSecret(answers.get(3));

        String libraries = answers.get(4);
        if (!"ALL".equalsIgnoreCase(libraries)) {
            config.setLibraryNames(List.of(libraries.split(",")));
        }

        String folders = answers.get(5);
        if (!"ALL".equalsIgnoreCase(folders)) {
            config.setFolderPaths(List.of(folders.split(",")));
        }

        String fileTypes = answers.get(6);
        if (!"ALL".equalsIgnoreCase(fileTypes)) {
            config.setFileTypes(List.of(fileTypes.split(",")));
        }

        config.setIncludeMetadata("yes".equalsIgnoreCase(answers.get(7)));
        config.setIncludeVersionHistory("yes".equalsIgnoreCase(answers.get(8)));

        String dateFilter = answers.get(9);
        if (!"none".equalsIgnoreCase(dateFilter)) {
            config.setLastModifiedFilter(dateFilter);
        }

        return config;
    }

    private ObjectStorageConfig buildObjectStorageConfig(Map<Integer, String> answers) {
        ObjectStorageConfig config = new ObjectStorageConfig();
        config.setStorageType(answers.get(0));
        config.setEndpoint(answers.get(1));
        config.setAccessKey(answers.get(2));
        config.setSecretKey(answers.get(3));
        config.setBucketName(answers.get(4));
        config.setRegion(answers.get(5));

        String prefixes = answers.get(6);
        if (!"ALL".equalsIgnoreCase(prefixes)) {
            config.setPrefixes(List.of(prefixes.split(",")));
        }

        String extensions = answers.get(7);
        if (!"ALL".equalsIgnoreCase(extensions)) {
            config.setFileExtensions(List.of(extensions.split(",")));
        }

        config.setIncludeMetadata("yes".equalsIgnoreCase(answers.get(8)));

        String dateFilter = answers.get(9);
        if (!"none".equalsIgnoreCase(dateFilter)) {
            config.setLastModifiedFilter(dateFilter);
        }

        String maxSize = answers.get(10);
        if (!"unlimited".equalsIgnoreCase(maxSize)) {
            config.setMaxFileSize(Long.parseLong(maxSize) * 1024 * 1024); // Convert MB to bytes
        }

        return config;
    }

    private UrlListConfig buildUrlListConfig(Map<Integer, String> answers) {
        UrlListConfig config = new UrlListConfig();
        config.setUrls(List.of(answers.get(0).split(",")));
        config.setCrawlDepth(Integer.parseInt(answers.get(1)));
        config.setFollowExternalLinks("yes".equalsIgnoreCase(answers.get(2)));

        String domains = answers.get(3);
        if (!"same-domain".equalsIgnoreCase(domains)) {
            config.setAllowedDomains(List.of(domains.split(",")));
        }

        String excludePatterns = answers.get(4);
        if (!"none".equalsIgnoreCase(excludePatterns)) {
            config.setExcludePatterns(List.of(excludePatterns.split(",")));
        }

        String headers = answers.get(5);
        if (!"none".equalsIgnoreCase(headers)) {
            Map<String, String> headerMap = new HashMap<>();
            for (String header : headers.split(",")) {
                String[] parts = header.split(":");
                if (parts.length == 2) {
                    headerMap.put(parts[0].trim(), parts[1].trim());
                }
            }
            config.setHeaders(headerMap);
        }

        String userAgent = answers.get(6);
        if (!"default".equalsIgnoreCase(userAgent)) {
            config.setUserAgent(userAgent);
        }

        config.setRequestDelay(Integer.parseInt(answers.get(7)));

        String maxPages = answers.get(8);
        if (!"unlimited".equalsIgnoreCase(maxPages)) {
            config.setMaxPages(Integer.parseInt(maxPages));
        }

        config.setRespectRobotsTxt("yes".equalsIgnoreCase(answers.get(9)));

        return config;
    }

    private CsvConfig buildCsvConfig(Map<Integer, String> answers) {
        CsvConfig config = new CsvConfig();
        config.setFilePath(answers.get(0));
        config.setDelimiter(answers.get(1));
        config.setQuoteCharacter(answers.get(2));
        config.setEscapeCharacter(answers.get(3));
        config.setHasHeader("yes".equalsIgnoreCase(answers.get(4)));

        String columnMappings = answers.get(5);
        if (!"auto-detect".equalsIgnoreCase(columnMappings)) {
            config.setColumnMappings(List.of(columnMappings.split(",")));
        }

        config.setEncoding(answers.get(6));
        config.setSkipLines(Integer.parseInt(answers.get(7)));

        String maxRecords = answers.get(8);
        if (!"all".equalsIgnoreCase(maxRecords)) {
            config.setMaxRecords(Integer.parseInt(maxRecords));
        }

        String dateFormat = answers.get(9);
        if (!"none".equalsIgnoreCase(dateFormat)) {
            config.setDateFormat(dateFormat);
        }

        String requiredColumns = answers.get(10);
        if (!"all".equalsIgnoreCase(requiredColumns)) {
            config.setRequiredColumns(List.of(requiredColumns.split(",")));
        }

        return config;
    }

    private KafkaConfig buildKafkaConfig(Map<Integer, String> answers) {
        KafkaConfig config = new KafkaConfig();
        config.setBootstrapServers(List.of(answers.get(0).split(",")));
        config.setTopicName(answers.get(1));
        config.setConsumerGroupId(answers.get(2));
        config.setOffsetReset(answers.get(3));

        String keyDeserializer = answers.get(4);
        if (!"default".equalsIgnoreCase(keyDeserializer)) {
            config.setKeyDeserializer(keyDeserializer);
        }

        String valueDeserializer = answers.get(5);
        if (!"default".equalsIgnoreCase(valueDeserializer)) {
            config.setValueDeserializer(valueDeserializer);
        }

        String additionalProps = answers.get(6);
        if (!"none".equalsIgnoreCase(additionalProps)) {
            Map<String, String> props = new HashMap<>();
            for (String prop : additionalProps.split(",")) {
                String[] parts = prop.split(":");
                if (parts.length == 2) {
                    props.put(parts[0].trim(), parts[1].trim());
                }
            }
            config.setAdditionalProperties(props);
        }

        config.setEnableAutoCommit("yes".equalsIgnoreCase(answers.get(7)));
        config.setAutoCommitInterval(Integer.parseInt(answers.get(8)));
        config.setSessionTimeout(Integer.parseInt(answers.get(9)));
        config.setSecurityProtocol(answers.get(10));
        config.setSaslMechanism(answers.get(11));
        config.setSaslUsername(answers.get(12));
        config.setSaslPassword(answers.get(13));

        return config;
    }

    private WebhookConfig buildWebhookConfig(Map<Integer, String> answers) {
        WebhookConfig config = new WebhookConfig();
        config.setEndpoint(answers.get(0));
        config.setHttpMethod(answers.get(1));

        String headers = answers.get(2);
        if (!"none".equalsIgnoreCase(headers)) {
            Map<String, String> headerMap = new HashMap<>();
            for (String header : headers.split(",")) {
                String[] parts = header.split(":");
                if (parts.length == 2) {
                    headerMap.put(parts[0].trim(), parts[1].trim());
                }
            }
            config.setHeaders(headerMap);
        }

        config.setContentType(answers.get(3));
        config.setAuthType(answers.get(4));
        config.setAuthToken(answers.get(5));
        config.setUsername(answers.get(6));
        config.setPassword(answers.get(7));
        config.setApiKey(answers.get(8));
        config.setApiKeyHeader(answers.get(9));
        config.setValidateSsl("yes".equalsIgnoreCase(answers.get(10)));
        config.setTimeoutSeconds(Integer.parseInt(answers.get(11)));
        config.setMaxRetries(Integer.parseInt(answers.get(12)));

        // Build JSON Path Configuration
        JsonPathConfig jsonPathConfig = new JsonPathConfig();
        jsonPathConfig.setCorpusTextPath(answers.get(13));

        String idPath = answers.get(14);
        if (!"none".equalsIgnoreCase(idPath)) {
            jsonPathConfig.setIdPath(idPath);
        }

        String timestampPath = answers.get(15);
        if (!"none".equalsIgnoreCase(timestampPath)) {
            jsonPathConfig.setTimestampPath(timestampPath);
        }

        String timestampFormat = answers.get(16);
        if (!"none".equalsIgnoreCase(timestampFormat)) {
            jsonPathConfig.setTimestampFormat(timestampFormat);
        }

        String metadataFields = answers.get(17);
        if (!"none".equalsIgnoreCase(metadataFields)) {
            Map<String, String> metadataPaths = new HashMap<>();
            for (String field : metadataFields.split(",")) {
                String[] parts = field.split(":");
                if (parts.length == 2) {
                    metadataPaths.put(parts[0].trim(), parts[1].trim());
                }
            }
            jsonPathConfig.setMetadataPaths(metadataPaths);
        }

        jsonPathConfig.setFlattenArrays("yes".equalsIgnoreCase(answers.get(18)));

        config.setJsonPathConfig(jsonPathConfig);
        return config;
    }

    private TargetConfig buildTargetConfiguration(ConfigurationSession session) {
        Map<Integer, String> targetAnswers = session.getTargetAnswers();
        TargetConfig config = new TargetConfig();
        config.setApiKey(targetAnswers.get(0));
        config.setUseCaseName(targetAnswers.get(1));
        return config;
    }

    public Mono<List<String>> getAvailableDataSources() {
        return Mono.just(List.of("confluence", "ecm", "sharepoint", "objectstorage", "urllist", "csv", "kafka", "webhook"));
    }

    public Mono<String> getConfigurationStatus(String sessionId) {
        ConfigurationSession session = sessions.get(sessionId);
        if (session == null) {
            return Mono.just("Session not found");
        }

        if (session.isTargetConfigStarted()) {
            List<String> targetQuestions = getTargetQuestions();
            return Mono.just(String.format("Progress: %d/%d target questions completed",
                    session.getCurrentStep(), targetQuestions.size()));
        } else {
            List<String> questions = getQuestionsForDataSource(session.getDataSourceType());
            return Mono.just(String.format("Progress: %d/%d source questions completed for %s",
                    session.getCurrentStep(), questions.size(), session.getDataSourceType()));
        }
    }

    private static class ConfigurationSession {
        private String dataSourceType;
        private int currentStep;
        private final Map<Integer, String> answers = new HashMap<>();
        private boolean targetConfigStarted = false;
        private final Map<Integer, String> targetAnswers = new HashMap<>();
        private DataSourceConfig sourceConfig;

        public String getDataSourceType() { return dataSourceType; }
        public void setDataSourceType(String dataSourceType) { this.dataSourceType = dataSourceType; }

        public int getCurrentStep() { return currentStep; }
        public void setCurrentStep(int currentStep) { this.currentStep = currentStep; }

        public Map<Integer, String> getAnswers() { return answers; }

        public boolean isTargetConfigStarted() { return targetConfigStarted; }
        public void setTargetConfigStarted(boolean targetConfigStarted) { this.targetConfigStarted = targetConfigStarted; }

        public Map<Integer, String> getTargetAnswers() { return targetAnswers; }

        public DataSourceConfig getSourceConfig() { return sourceConfig; }
        public void setSourceConfig(DataSourceConfig sourceConfig) { this.sourceConfig = sourceConfig; }
    }
}