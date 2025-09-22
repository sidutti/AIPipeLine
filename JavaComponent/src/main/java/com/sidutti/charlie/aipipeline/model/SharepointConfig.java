package com.sidutti.charlie.aipipeline.model;

import java.util.List;

public class SharepointConfig extends DataSourceConfig {
    private String siteUrl;
    private String tenantId;
    private String clientId;
    private String clientSecret;
    private List<String> libraryNames;
    private List<String> folderPaths;
    private List<String> fileTypes;
    private boolean includeMetadata;
    private boolean includeVersionHistory;
    private String lastModifiedFilter;

    public SharepointConfig() {
        super();
    }

    @Override
    public String getType() {
        return "sharepoint";
    }

    public String getSiteUrl() { return siteUrl; }
    public void setSiteUrl(String siteUrl) { this.siteUrl = siteUrl; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }

    public List<String> getLibraryNames() { return libraryNames; }
    public void setLibraryNames(List<String> libraryNames) { this.libraryNames = libraryNames; }

    public List<String> getFolderPaths() { return folderPaths; }
    public void setFolderPaths(List<String> folderPaths) { this.folderPaths = folderPaths; }

    public List<String> getFileTypes() { return fileTypes; }
    public void setFileTypes(List<String> fileTypes) { this.fileTypes = fileTypes; }

    public boolean isIncludeMetadata() { return includeMetadata; }
    public void setIncludeMetadata(boolean includeMetadata) { this.includeMetadata = includeMetadata; }

    public boolean isIncludeVersionHistory() { return includeVersionHistory; }
    public void setIncludeVersionHistory(boolean includeVersionHistory) { this.includeVersionHistory = includeVersionHistory; }

    public String getLastModifiedFilter() { return lastModifiedFilter; }
    public void setLastModifiedFilter(String lastModifiedFilter) { this.lastModifiedFilter = lastModifiedFilter; }
}