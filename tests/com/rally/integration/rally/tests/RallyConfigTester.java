package com.rally.integration.rally.tests;

import com.rally.integration.rally.RallyConnector;
import com.rally.integration.teamcity.FileConfig;
import com.rally.integration.teamcity.SettingsBean;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class RallyConfigTester {
    //these need only for integration tests
    public static final String URL = "https://rally1.rallydev.com/";
    public static final String USER_NAME = "trevor.leybourne@myob.com";
    public static final String PASSWORD = "myob1234";

    @Test
    //@Ignore("Require Rally server")
    public void testDefaults() {
        final com.rally.integration.rally.RallyConfig cfg = new com.rally.integration.rally.RallyConfig();
        cfg.setDefaults();

        Assert.assertEquals("https://rally1.rallydev.com", cfg.getUrl());
        Assert.assertEquals("admin", cfg.getUserName());
        Assert.assertEquals("admin", cfg.getPassword());
        Assert.assertFalse(cfg.getProxyUsed());
        Assert.assertEquals("", cfg.getProxyPassword());
        Assert.assertEquals("", cfg.getProxyUri());
        Assert.assertEquals("", cfg.getProxyUser());
    }

    @Test
    //@Ignore("Require Rally server")
    public void testConnectionValid() throws IOException {
        final RallyConnector connector = new RallyConnector();
        FileConfig cfg = new FileConfig(new SettingsBean(new com.rally.integration.rally.RallyConfig()));
        connector.setConnectionSettings(cfg);

        Assert.assertFalse(connector.isConnectionValid());
        connector.setConnectionSettings(getValidConfig());
        Assert.assertTrue(connector.isConnectionValid());
    }

   @Test
    public void testPasswordEncryption() {
        final com.rally.integration.rally.RallyConfig config = new com.rally.integration.rally.RallyConfig();
        config.setDefaults();
        SettingsBean bean = new SettingsBean(config);

        Assert.assertFalse(bean.getEncryptedPassword().contains(bean.getPassword()));
    }

    public static FileConfig getValidConfig() {
        final com.rally.integration.rally.RallyConfig cfg = new com.rally.integration.rally.RallyConfig();
        cfg.setDefaults();
        cfg.setUrl(URL);
        cfg.setUserName(USER_NAME);
        cfg.setPassword(PASSWORD);
        return new FileConfig(new SettingsBean(cfg));
    }
}
