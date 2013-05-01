package com.rally.integration.teamcity;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import jetbrains.buildServer.configuration.ChangeListener;
import jetbrains.buildServer.configuration.FileWatcher;
import jetbrains.buildServer.serverSide.crypt.EncryptUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class FileConfig extends com.rally.integration.rally.RallyConfig implements ChangeListener {

    private static final Logger LOG = Logger.getInstance(FileConfig.class.getName());

    public static final String CONFIG_FILENAME = "RallyTeamCityIntegrator.properties";

    public static final int FILE_MONITOR_INTERVAL = 10;
    private static final String URL = "url";
    private static final String USER_NAME           = "userName";
    private static final String PASSWORD            = "password";
    private static final String USE_PROXY           = "useProxy";
    private static final String PROXY_URI           = "proxyUri";
    private static final String PROXY_USER_NAME     = "proxyUsername";
    private static final String PROXY_PASSWORD      = "proxyPassword";
    private static final String TEST_ONLY           = "testOnly";
    private static final String CREATE_NOT_EXIST    = "createNotExist";

    private File myConfigFile;
    private FileWatcher myChangeObserver;

    public FileConfig(String configDir) {
        myConfigFile = new File(configDir, CONFIG_FILENAME);
        myChangeObserver = new FileWatcher(myConfigFile);
        myChangeObserver.setSleepingPeriod(FILE_MONITOR_INTERVAL * 1000L);
        myChangeObserver.registerListener(this);
        myChangeObserver.start();
        if (!myConfigFile.exists()) {
            setDefaults();
            save();
            LOG.warn("Default Rally config file created.");
        } else {
            load();
        }
        LOG.info("Rally configuration file " + myConfigFile.getAbsolutePath() +
                 " will be monitored with interval " + FILE_MONITOR_INTERVAL + " seconds.");
    }

    public FileConfig(SettingsBean bean) {
        url = bean.getUrl();
        userName = bean.getUserName();
        password = bean.getPassword();
        isProxyUsed = bean.getProxyUsed();
        proxyUri = bean.getProxyUri();
        proxyUser = bean.getProxyUsername();
        proxyPassword = bean.getProxyPassword();
        isTestOnly = bean.getTestOnly();
        isCreateNotExist = bean.getCreateNotExist();
    }

    private synchronized void load() {
        FileInputStream stream = null;
        try {
            LOG.info("Loading Rally configuration file: " + myConfigFile.getAbsolutePath());
            final Properties prop = new Properties();
            stream = new FileInputStream(myConfigFile);
            prop.load(stream);
            url = prop.getProperty(URL);
            userName = prop.getProperty(USER_NAME);
            final String pass = prop.getProperty(PASSWORD);
            password = StringUtil.isEmptyOrSpaces(pass) ? null : EncryptUtil.unscramble(pass);
            isProxyUsed = Boolean.parseBoolean(prop.getProperty(USE_PROXY));
            isTestOnly = Boolean.parseBoolean(prop.getProperty(TEST_ONLY));
            isCreateNotExist = Boolean.parseBoolean(prop.getProperty(CREATE_NOT_EXIST));
            proxyUri = prop.getProperty(PROXY_URI);
            proxyUser = prop.getProperty(PROXY_USER_NAME);
            final String proxyPass = prop.getProperty(PROXY_PASSWORD);
            proxyPassword = StringUtil.isEmptyOrSpaces(proxyPass) ? null : EncryptUtil.unscramble(proxyPass);
            LOG.info("\t...loading completed successfully.");
        } catch (Exception e) {
            throw new RuntimeException("Cannot load Rally config file: " + myConfigFile, e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    //do nothing
                }
            }
        }
    }

    public synchronized void save() {
        LOG.info("Saving configuration file: " + myConfigFile.getAbsolutePath());
        myChangeObserver.runActionWithDisabledObserver(new Runnable() {

            public void run() {
                final Properties p = new Properties();
                p.setProperty(URL, url);
                p.setProperty(USER_NAME, userName);
                if (!StringUtil.isEmptyOrSpaces(password)) {
                    p.setProperty(PASSWORD, EncryptUtil.scramble(password));
                }
                p.setProperty(USE_PROXY, Boolean.toString(isProxyUsed));
                p.setProperty(TEST_ONLY, Boolean.toString(isTestOnly));
                p.setProperty(CREATE_NOT_EXIST, Boolean.toString(isCreateNotExist));
                p.setProperty(PROXY_URI, proxyUri);
                p.setProperty(PROXY_USER_NAME, proxyUser);
                if (!StringUtil.isEmptyOrSpaces(proxyPassword)) {
                    p.setProperty(PROXY_PASSWORD, EncryptUtil.scramble(proxyPassword));
                }

                FileOutputStream stream = null;
                try {
                    stream = new FileOutputStream(myConfigFile);
                    p.store(stream, null);
                } catch (IOException e) {
                    throw new RuntimeException("Cannot save configuration file: " + myConfigFile, e);
                } finally {
                    if (stream != null)
                        try {
                            stream.close();
                        } catch (IOException e) {
                            //do nothing
                        }
                }
            }
        });
    }

    public void changeOccured(String requestor) {
        load();
    }
}
