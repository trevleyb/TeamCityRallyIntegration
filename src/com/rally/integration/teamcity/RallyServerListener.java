package com.rally.integration.teamcity;

import com.intellij.openapi.diagnostic.Logger;
import com.rally.integration.rally.RallyBuild;
import com.rally.integration.rally.RallyConnector;
import com.rally.integration.rally.RallyManager;
import jetbrains.buildServer.serverSide.*;

import java.io.IOException;

public class RallyServerListener extends BuildServerAdapter {

    public static final String PLUGIN_NAME = "TeamCityRallyIntegrator";
    private static final Logger LOG = Logger.getInstance(RallyServerListener.class.getName());
    private final SBuildServer myBuildServer;
    private final RallyManager myManager;
    private final FileConfig myConfig;
    private final WebLinks weblinks;

    public RallyServerListener(SBuildServer server, WebLinks links, RallyConnector connector, ServerPaths serverPaths) throws IOException {
        myBuildServer = server;
        weblinks = links;
        myConfig = new FileConfig(serverPaths.getConfigDir());
        myManager = new RallyManager(myConfig, connector);
    }

    public void register() {
        LOG.info("RallyServerListener.register()");
        myBuildServer.addListener(this);
    }

    @Override
    public void buildFinished(SRunningBuild runningBuild) {
        LOG.info("START: Build Running Start: ('" + runningBuild + "')");
        int result = myManager.submitBuildRun(new RallyBuild(runningBuild, weblinks, myConfig));
        LOG.info("FINISH: Build Running Stop: ('" + runningBuild + "') result =" + result);
    }

}
