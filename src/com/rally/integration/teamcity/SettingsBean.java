package com.rally.integration.teamcity;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.controllers.RememberState;
import jetbrains.buildServer.serverSide.crypt.RSACipher;
import jetbrains.buildServer.util.StringUtil;

public class SettingsBean extends RememberState {
    private static final Logger LOG = Logger.getInstance(SettingsBean.class.getName());

    private String url;
    private String userName;
    private String password;
    private String proxyUri;
    private String proxyUsername;
    private String proxyPassword;
    private Boolean isProxyUsed;
    private Boolean isTestOnly;
    private Boolean isCreateNotExist;

    public SettingsBean() { }

    public SettingsBean(com.rally.integration.rally.RallyConfig cfg) {
        url = cfg.getUrl();
        userName = cfg.getUserName();
        password = cfg.getPassword();
        isProxyUsed = cfg.getProxyUsed();
        proxyUri = cfg.getProxyUri();
        proxyUsername = cfg.getProxyUser();
        proxyPassword = cfg.getProxyPassword();
        isTestOnly = cfg.getTestOnly();
        isCreateNotExist = cfg.getCreateNotExist();
    }

    public String getPassword() {
        return password;
    }

    public String getHexEncodedPublicKey() {
        return RSACipher.getHexEncodedPublicKey();
    }

    public String getEncryptedPassword() {
        if (!StringUtil.isEmptyOrSpaces(password)) return RSACipher.encryptDataForWeb(password);
        return "";
    }

    public void setEncryptedPassword(String encrypted) {
        password = RSACipher.decryptWebRequestData(encrypted);
    }

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

    public Boolean getProxyUsed() {
        return isProxyUsed;
    }

    public void setProxyUsed(Boolean proxyUsed) {
        isProxyUsed = proxyUsed;
    }

    public Boolean getTestOnly() {
        return isTestOnly;
    }

    public void setTestOnly(Boolean testOnly) {
        isTestOnly = testOnly;
    }

    public Boolean getCreateNotExist() {
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

    public String getProxyUsername() {
        return proxyUsername;
    }

    public void setProxyUsername(String proxyUsername) {
        this.proxyUsername = proxyUsername;
    }

    public String getEncryptedProxyPassword() {
        if (!StringUtil.isEmptyOrSpaces(proxyPassword))
            return RSACipher.encryptDataForWeb(proxyPassword);
        return "";
    }

    public void setEncryptedProxyPassword(String encrypted) {
        proxyPassword = RSACipher.decryptWebRequestData(encrypted);
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    public String getPLUGIN_NAME() {
        return RallyServerListener.PLUGIN_NAME;
    }

    public String getPAGE_URL() {
        return RallySettingsController.PAGE_URL;
    }
}
