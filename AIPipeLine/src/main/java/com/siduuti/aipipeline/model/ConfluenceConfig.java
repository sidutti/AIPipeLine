package com.siduuti.aipipeline.model;

import java.util.List;

public class ConfluenceConfig extends DataSourceConfig {
    private String baseUrl;
    private String username;
    private String apiToken;
    private List<String> spaceKeys;
    private boolean includeAttachments;
    private int maxPages;
    private String lastModifiedFilter;

    public ConfluenceConfig() {
        super();
    }

    @Override
    public String getType() {
        return "confluence";
    }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getApiToken() { return apiToken; }
    public void setApiToken(String apiToken) { this.apiToken = apiToken; }

    public List<String> getSpaceKeys() { return spaceKeys; }
    public void setSpaceKeys(List<String> spaceKeys) { this.spaceKeys = spaceKeys; }

    public boolean isIncludeAttachments() { return includeAttachments; }
    public void setIncludeAttachments(boolean includeAttachments) { this.includeAttachments = includeAttachments; }

    public int getMaxPages() { return maxPages; }
    public void setMaxPages(int maxPages) { this.maxPages = maxPages; }

    public String getLastModifiedFilter() { return lastModifiedFilter; }
    public void setLastModifiedFilter(String lastModifiedFilter) { this.lastModifiedFilter = lastModifiedFilter; }
}