package com.siduuti.aipipeline.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "confluence")
public class ConfluenceConfig {
    
    private String baseUrl;
    private String username;
    private String apiToken;
    private String spaceKey;
    private OAuth oauth = new OAuth();
    
    public String getBaseUrl() {
        return baseUrl;
    }
    
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getApiToken() {
        return apiToken;
    }
    
    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }
    
    public String getSpaceKey() {
        return spaceKey;
    }
    
    public void setSpaceKey(String spaceKey) {
        this.spaceKey = spaceKey;
    }
    
    public OAuth getOauth() {
        return oauth;
    }
    
    public void setOauth(OAuth oauth) {
        this.oauth = oauth;
    }
    
    public static class OAuth {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
        private String scope = "read:content:confluence";
        
        public String getClientId() {
            return clientId;
        }
        
        public void setClientId(String clientId) {
            this.clientId = clientId;
        }
        
        public String getClientSecret() {
            return clientSecret;
        }
        
        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }
        
        public String getRedirectUri() {
            return redirectUri;
        }
        
        public void setRedirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
        }
        
        public String getScope() {
            return scope;
        }
        
        public void setScope(String scope) {
            this.scope = scope;
        }
    }
}