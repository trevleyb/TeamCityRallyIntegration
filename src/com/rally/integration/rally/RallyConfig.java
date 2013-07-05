package com.rally.integration.rally;

public class RallyConfig {

    protected String url = "";
    protected String userName;
    protected String password;
    protected String proxyUri;
    protected String proxyUser;
    protected String proxyPassword;
    protected boolean isProxyUsed;
    protected boolean isTestOnly;
    protected boolean isCreateNotExist;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean getProxyUsed() {
        return isProxyUsed;
    }

    public void setProxyUsed(Boolean proxyUsed) {
        isProxyUsed = proxyUsed;
    }

    public boolean getTestOnly() {
        return isTestOnly;
    }

    public void setTestOnly(Boolean testOnly) {
        isTestOnly = testOnly;
    }

    public boolean getCreateNotExist() {
        return isCreateNotExist;
    }

    public void setCreateNotExist(Boolean createNotExist) {
        isCreateNotExist = createNotExist;
    }

    public String getProxyUri() {
        return proxyUri;
    }

    public void setProxyUri(String proxyUri) {
        this.proxyUri = proxyUri;
    }

    public String getProxyUser() {
        return proxyUser;
    }

    public void setProxyUsername(String proxyUser) {
        this.proxyUser = proxyUser;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    public void setDefaults() {
        url = "https://rally1.rallydev.com/";
        userName = "user";
        password = "password";
        isProxyUsed = false;
        proxyUri = "";
        proxyUser = "";
        proxyPassword = "";
        isTestOnly = true;
        isCreateNotExist = false;
    }

    @Override
    public String toString() {
        return "Config{" +
                " userName='" + userName + '\'' +
                ", url='" + url + '\'' +
                ", useProxy='" + isProxyUsed + '\'' +
                ", proxyUri='" + proxyUri + '\'' +
                ", proxyUser='" + proxyUser + '\'' +
                ", proxyPassword='" + proxyPassword + '\'' +
                ", testOnly='" + isTestOnly + '\'' +
                ", createNotExist='" + isCreateNotExist + '\'' +
                '}';
    }
}
