package com.sidutti.charlie.pipelineagent.model;

import java.util.List;

public class EcmRepoConfig extends DataSourceConfig {
    private String repoType; // git, svn, mercurial
    private String repoUrl;
    private String branch;
    private String username;
    private String password;
    private String sshKey;
    private List<String> includePaths;
    private List<String> excludePaths;
    private List<String> fileExtensions;
    private boolean includeHistory;
    private int maxCommits;

    public EcmRepoConfig() {
        super();
    }

    @Override
    public String getType() {
        return "ecm";
    }

    public String getRepoType() { return repoType; }
    public void setRepoType(String repoType) { this.repoType = repoType; }

    public String getRepoUrl() { return repoUrl; }
    public void setRepoUrl(String repoUrl) { this.repoUrl = repoUrl; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getSshKey() { return sshKey; }
    public void setSshKey(String sshKey) { this.sshKey = sshKey; }

    public List<String> getIncludePaths() { return includePaths; }
    public void setIncludePaths(List<String> includePaths) { this.includePaths = includePaths; }

    public List<String> getExcludePaths() { return excludePaths; }
    public void setExcludePaths(List<String> excludePaths) { this.excludePaths = excludePaths; }

    public List<String> getFileExtensions() { return fileExtensions; }
    public void setFileExtensions(List<String> fileExtensions) { this.fileExtensions = fileExtensions; }

    public boolean isIncludeHistory() { return includeHistory; }
    public void setIncludeHistory(boolean includeHistory) { this.includeHistory = includeHistory; }

    public int getMaxCommits() { return maxCommits; }
    public void setMaxCommits(int maxCommits) { this.maxCommits = maxCommits; }
}