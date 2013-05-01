package com.rally.integration.teamcity.tests;

import com.rally.integration.teamcity.FileConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;


public class FileConfigTester {
    private static final String DIR = ".";

    @After
    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    public void deleteCfgFile() {
        new File(DIR, FileConfig.CONFIG_FILENAME).delete();
    }

    @Test
    public void testFileReadWrite() {
        final String url = "https://rallydev.com";
        final String userName = "usr";
        final String pass = "psw";
        {
            final FileConfig cfg = new FileConfig(DIR);
            cfg.setUrl(url);
            cfg.setUserName(userName);
            cfg.setPassword(pass);
            cfg.save();
        }
        {
            final FileConfig cfg = new FileConfig(DIR);
            Assert.assertEquals(url, cfg.getUrl());
            Assert.assertEquals(userName, cfg.getUserName());
            Assert.assertEquals(pass, cfg.getPassword());
        }
    }
}
