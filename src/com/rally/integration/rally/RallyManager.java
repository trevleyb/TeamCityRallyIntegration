package com.rally.integration.rally;

import com.intellij.openapi.diagnostic.Logger;
import com.rally.integration.rally.entities.RallyBuildDef;
import com.rally.integration.rally.entities.RallyProject;
import com.rally.integration.teamcity.FileConfig;
import jetbrains.buildServer.vcs.SVcsModification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This is the manager class that coordinates all calls to Rally. This class manages the details
 * of finding projects and workspaces and the creation, updates, deletions of objects in Rally.
 */
public class RallyManager {
    private static final Logger LOG = Logger.getInstance(RallyManager.class.getName());

    public final static int NOTIFY_SUCCESS = 0;
    public final static int NOTIFY_FAIL_CONNECTION = 1;
    public final static int NOTIFY_FAIL_INVALID_INFO = 3;

    private final FileConfig config;
    private RallyConnector connector;

    public RallyManager(FileConfig config, RallyConnector connector) throws IOException {
        this.config     = config;
        this.connector  = connector;
        connector.setConnectionSettings(config);
    }

    // Called by the listener when a build has been completed.
    public int submitBuildRun(RallyBuild buildInfo) {

        LOG.info("RallyManager.submitBuildRun called: " + buildInfo.getBuildID());
        try {
            if (!connector.isConnectionValid()) {
                LOG.info("RallyManager.submitBuildRun: Failed to connect to Rally.");
                return NOTIFY_FAIL_CONNECTION;
            }

            DumpDebugInformation(buildInfo);
            if (!isValidBuildInfo(buildInfo)) {
                LOG.warn("RallyManager.submitBuildRun: No valid build info found to create a build reference. Skipped.");
                return NOTIFY_FAIL_INVALID_INFO;
            }

            List<String> changeSets = GetChangeSets(buildInfo.getChangeSetIDs());
            if (!config.isTestOnly) {
               String ref = connector.Create("build", buildInfo.toJSON(getBuildDef(buildInfo),changeSets));
               LOG.info("RallyManager.submitBuildRun: Created Build reference: " + ref);
            } else {
                LOG.info("RallyManager.submitBuildRun: Did not create Build reference. IsTestOnly=TRUE");
            }
        } catch (IOException e) {
            LOG.error("RallyManager.submitBuildRun: Could not create the Build reference.");
            LOG.error(e);
        }
        LOG.info("RallyManager.submitBuildRun finished: " + buildInfo.getBuildID());
        return NOTIFY_SUCCESS;
    }

    private List<String> GetChangeSets(List<String> changeSetIDs) {
        if (changeSetIDs == null || changeSetIDs.size() == 0) {
            LOG.info("No change set data was provided from Team City.");
            return null;
        }

        List<String> changeSets = new ArrayList<String>();
        for (String id : changeSetIDs) {
            String[] scmParts = id.split(":");
            if (scmParts.length == 2) {
                String ref = connector.FindChangeSet(scmParts[0],scmParts[1]);
                if (!isNullOrBlank(ref)) changeSets.add(ref);
            }
        }
        LOG.info("Found " + changeSets.size() + " change sets to associate.");
        return changeSets;
    }

    /**
     * Used for dumping debugging information to the LOG file.
     * @param buildInfo
     */
    private void DumpDebugInformation(RallyBuild buildInfo) {
        LOG.info("BuildInfo: ID            =" + buildInfo.getBuildID());
        LOG.info("BuildInfo: NAME          =" + buildInfo.getBuildName());
        LOG.info("BuildInfo: URL           =" + buildInfo.getUrl());
        LOG.info("BuildInfo: START         =" + buildInfo.getStartTime());
        LOG.info("BuildInfo: ELAPSED       =" + buildInfo.getDuration());
        LOG.info("BuildInfo: isFORCED      =" + buildInfo.isForced());
        LOG.info("BuildInfo: isSUCCESS     =" + buildInfo.isSuccessful());
        LOG.info("BuildInfo: BUILDTYPE     =" + buildInfo.getTeamCityBuildInfo().getBuildType());
        LOG.info("BuildInfo: BUILDTYPENAME =" + buildInfo.getTeamCityBuildInfo().getBuildTypeName());
        LOG.info("BuildInfo: FULLNAME      =" + buildInfo.getTeamCityBuildInfo().getFullName());
        LOG.info("BuildInfo: STATUSDESC    =" + buildInfo.getTeamCityBuildInfo().getStatusDescriptor());
        LOG.info("BuildInfo: STATUS        =" + buildInfo.getTeamCityBuildInfo().getBuildStatus().toString());
        LOG.info("BuildInfo: NUMBER        =" + buildInfo.getTeamCityBuildInfo().getBuildNumber());

        for (Iterator<SVcsModification> it = buildInfo.getBuildChanges().iterator(); it.hasNext();) {
            SVcsModification mod = it.next();
            LOG.info("BuildInfo: MODIFICATIONS");
            LOG.info("BuildInfo: MODIFICATIONS DESC=" + mod.getDescription());
            LOG.info("BuildInfo: MODIFICATIONS DVER=" + mod.getDisplayVersion());
            LOG.info("BuildInfo: MODIFICATIONS USER=" + mod.getUserName());
            LOG.info("BuildInfo: MODIFICATIONS VCN =" + mod.getVersionControlName());
            LOG.info("BuildInfo: MODIFICATIONS VER =" + mod.getVersion());
            LOG.info("BuildInfo: MODIFICATIONS ID  =" + mod.getId());
            LOG.info("BuildInfo: MODIFICATIONS DSP =" + mod.getDisplayVersion());
            LOG.info("BuildInfo: MODIFICATIONS ROOT=" + mod.getVcsRoot());
            LOG.info("BuildInfo: MODIFICATIONS ROOTNAME=" + mod.getVcsRoot().getName());
            LOG.info("BuildInfo: MODIFICATIONS NAME=" + mod.getId());
        }
        LOG.info("Change Set IDs: "+ buildInfo.getChangeSetIDs().toString());
    }

    protected boolean isValidBuildInfo(RallyBuild buildInfo)  {
        return getBuildDef(buildInfo) != null;
    }

    protected RallyBuildDef getBuildDef(RallyBuild buildInfo)  {
        try {
            String workspaceName = buildInfo.GetProperty("RallyWorkspace");
            String projectName   = buildInfo.GetProperty("RallyProject");
            String buildDefName  = buildInfo.GetProperty("RallyBuildDef");

            LOG.info("Build Details =" + buildInfo.getBuildName()+":"+buildInfo.getBuildID());
            LOG.info("RallyWorkspace=" + workspaceName);
            LOG.info("RallyProject  =" + projectName);
            LOG.info("RallyBuildDef =" + buildDefName);

            RallyBuildDef buildDef = getRallyBuildDef(buildInfo.getBuildName()+":"+buildInfo.getBuildID(), workspaceName, projectName, buildDefName);
            if (buildDef != null) LOG.info("Found a build definition: " + buildDef.getRef());
            if (buildDef == null) LOG.info("No suitable build definition was found.");
            return buildDef;
        } catch (Exception e) {
            LOG.warn("Build not setup for tracking. Build will be skipped.");
        }
        return null;
    }

    public RallyBuildDef getRallyBuildDef(String buildID, String workspaceName, String projectName, String buildDefName) throws Exception {
        LOG.info("Looking for a build definition for: " + buildID + ", " + workspaceName + ", " + projectName + ", " + buildDefName);
        if (isNullOrBlank(workspaceName) && isNullOrBlank(projectName) && isNullOrBlank(buildDefName)) {
            LOG.warn("Build '" + buildID + "' does not contain RallyWorkspace, RallyProject or RallyBuildDef properties.");
            return null;
        }

        if (isNullOrBlank(workspaceName) && isNullOrBlank(projectName)) {
            RallyBuildDef buildDef = connector.getSubscription().FindBuildDef(buildDefName);
            if (buildDef != null) return buildDef;
            LOG.warn("Build '" + buildID + "' property RallyBuildDef is invalid.");
            return null;
        }

        if (!isNullOrBlank(workspaceName) && !isNullOrBlank(projectName) && isNullOrBlank(buildDefName)) {
            RallyBuildDef buildDef = connector.getSubscription().FindProject(workspaceName,projectName).getBuildDefs().get(0);
            if (buildDef != null) return buildDef;
            LOG.warn("Build '" + buildID + "' does not have a default BuildDef.");
            return null;
        }

        if (isNullOrBlank(projectName)) {
            RallyBuildDef buildDef = connector.getSubscription().FindBuildDef(workspaceName, buildDefName);
            if (buildDef != null) return buildDef;
            LOG.warn("Build '" + buildID + "' properties RallyWorkspace and RallyBuildDef are invalid.");
            return null;
        }

        RallyBuildDef buildDef = connector.getSubscription().FindBuildDef(workspaceName, projectName, buildDefName);
        if (buildDef != null) return buildDef;

        // If we get here then no build definition exists so we may need to create one.
        // Only create one if we didn't find one, the Create flag is true and Test is false
        // --------------------------------------------------------------------------------------------------
        if (buildDef == null && config.isCreateNotExist && !config.isTestOnly) {
            LOG.info("No build definition was found, creating one. ");
            RallyProject project = connector.getSubscription().FindProject(workspaceName,projectName);
            if (project != null) {

                // Create a new BuildDef object and store it in Rally. Then add it to the collection so
                // subsequent builds will find this reference.
                // ------------------------------------------------------------------------------------------
                buildDef = new RallyBuildDef (buildDefName, null, project);
                if (buildDef.toJson() != null) {
                    String ref = connector.Create("BuildDefinition", buildDef.toJson());
                    buildDef.setRef(ref);
                    project.getBuildDefs().add(buildDef);
                    return buildDef;
                }
            }
        }
        LOG.warn("Build '" + buildID + "'. Did not find a match in Rally to attribute this build to.");
        return null;
    }

    public static boolean isNullOrBlank(String param) {
        return param == null || param.trim().length() == 0;
    }

}
