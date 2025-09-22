package com.sidutti.charlie.aipipeline.model;

import java.util.List;
import java.util.Map;

public class UrlListConfig extends DataSourceConfig {
    private List<String> urls;
    private int crawlDepth;
    private boolean followExternalLinks;
    private List<String> allowedDomains;
    private List<String> excludePatterns;
    private Map<String, String> headers;
    private String userAgent;
    private int requestDelay;
    private int maxPages;
    private boolean respectRobotsTxt;

    public UrlListConfig() {
        super();
    }

    @Override
    public String getType() {
        return "urllist";
    }

    public List<String> getUrls() { return urls; }
    public void setUrls(List<String> urls) { this.urls = urls; }

    public int getCrawlDepth() { return crawlDepth; }
    public void setCrawlDepth(int crawlDepth) { this.crawlDepth = crawlDepth; }

    public boolean isFollowExternalLinks() { return followExternalLinks; }
    public void setFollowExternalLinks(boolean followExternalLinks) { this.followExternalLinks = followExternalLinks; }

    public List<String> getAllowedDomains() { return allowedDomains; }
    public void setAllowedDomains(List<String> allowedDomains) { this.allowedDomains = allowedDomains; }

    public List<String> getExcludePatterns() { return excludePatterns; }
    public void setExcludePatterns(List<String> excludePatterns) { this.excludePatterns = excludePatterns; }

    public Map<String, String> getHeaders() { return headers; }
    public void setHeaders(Map<String, String> headers) { this.headers = headers; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public int getRequestDelay() { return requestDelay; }
    public void setRequestDelay(int requestDelay) { this.requestDelay = requestDelay; }

    public int getMaxPages() { return maxPages; }
    public void setMaxPages(int maxPages) { this.maxPages = maxPages; }

    public boolean isRespectRobotsTxt() { return respectRobotsTxt; }
    public void setRespectRobotsTxt(boolean respectRobotsTxt) { this.respectRobotsTxt = respectRobotsTxt; }
}