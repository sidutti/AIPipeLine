package com.sidutti.charlie.aipipeline.model;

import java.util.List;

public class ObjectStorageConfig extends DataSourceConfig {
    private String storageType; // S3, Azure Blob, GCS, HPOS
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucketName;
    private String region;
    private List<String> prefixes;
    private List<String> fileExtensions;
    private boolean includeMetadata;
    private String lastModifiedFilter;
    private long maxFileSize;

    public ObjectStorageConfig() {
        super();
    }

    @Override
    public String getType() {
        return "objectstorage";
    }

    public String getStorageType() {
        return storageType;
    }

    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public List<String> getPrefixes() {
        return prefixes;
    }

    public void setPrefixes(List<String> prefixes) {
        this.prefixes = prefixes;
    }

    public List<String> getFileExtensions() {
        return fileExtensions;
    }

    public void setFileExtensions(List<String> fileExtensions) {
        this.fileExtensions = fileExtensions;
    }

    public boolean isIncludeMetadata() {
        return includeMetadata;
    }

    public void setIncludeMetadata(boolean includeMetadata) {
        this.includeMetadata = includeMetadata;
    }

    public String getLastModifiedFilter() {
        return lastModifiedFilter;
    }

    public void setLastModifiedFilter(String lastModifiedFilter) {
        this.lastModifiedFilter = lastModifiedFilter;
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }
}