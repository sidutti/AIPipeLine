package com.sidutti.charlie.aipipeline.model;

import java.util.Map;

public class WebhookConfig extends DataSourceConfig {
    private String endpoint;
    private String httpMethod; // POST, PUT, PATCH
    private Map<String, String> headers;
    private String contentType;
    private String authType; // none, bearer, basic, apikey
    private String authToken;
    private String username;
    private String password;
    private String apiKey;
    private String apiKeyHeader;
    private boolean validateSsl;
    private int timeoutSeconds;
    private int maxRetries;
    private JsonPathConfig jsonPathConfig;

    public WebhookConfig() {
        super();
    }

    @Override
    public String getType() {
        return "webhook";
    }

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public String getHttpMethod() { return httpMethod; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }

    public Map<String, String> getHeaders() { return headers; }
    public void setHeaders(Map<String, String> headers) { this.headers = headers; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public String getAuthType() { return authType; }
    public void setAuthType(String authType) { this.authType = authType; }

    public String getAuthToken() { return authToken; }
    public void setAuthToken(String authToken) { this.authToken = authToken; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getApiKeyHeader() { return apiKeyHeader; }
    public void setApiKeyHeader(String apiKeyHeader) { this.apiKeyHeader = apiKeyHeader; }

    public boolean isValidateSsl() { return validateSsl; }
    public void setValidateSsl(boolean validateSsl) { this.validateSsl = validateSsl; }

    public int getTimeoutSeconds() { return timeoutSeconds; }
    public void setTimeoutSeconds(int timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }

    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }

    public JsonPathConfig getJsonPathConfig() { return jsonPathConfig; }
    public void setJsonPathConfig(JsonPathConfig jsonPathConfig) { this.jsonPathConfig = jsonPathConfig; }
}