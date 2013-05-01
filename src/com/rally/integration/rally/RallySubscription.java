package com.rally.integration.rally;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.openapi.diagnostic.Logger;
import com.rally.integration.rally.entities.RallyBuildDef;
import com.rally.integration.rally.entities.RallyProject;
import com.rally.integration.rally.entities.RallyRepository;
import com.rally.integration.rally.entities.RallyWorkspace;

import java.util.ArrayList;
import java.util.List;

public class RallySubscription {

    private static final Logger LOG = Logger.getInstance(RallyConnector.class.getName());

    List<RallyWorkspace> workspaceList;
    List<RallyRepository> repositories;

    public RallySubscription(JsonObject subscriptionData, JsonArray scmRepositories) throws Exception {
        workspaceList = BuildWorkspaceList(subscriptionData);
        repositories  = BuildRepositoriesList(scmRepositories);
    }

    /**
     * Searches for a RallyWorkspace object in the list and returns that object
     * @param workspaceName - the name of the workspace to find.
     * @return a RallyWorkspace object
     */
    public RallyWorkspace FindWorkspace(String workspaceName) {
        for (RallyWorkspace ws : workspaceList) {
            if (ws.getName().compareToIgnoreCase(workspaceName) == 0) return ws;
        }
        return null;
    }

    /**
     * Given a workspace name and project name, this will return a RallyProject object which matches
     * the workspace and project names.
     * @param workspaceName - name of the workspace to find
     * @param projectName - name of the project to find
     * @return a RallyProject object
     */
    public RallyProject FindProject(String workspaceName, String projectName) {
        RallyWorkspace ws = FindWorkspace(workspaceName);
        if (ws != null) return ws.Find(projectName);
        return null;
    }

    /**
     * Given a project name, this will find the first project that matches that name across all
     * workspaces. If there are duplicate project names then the first one found will be used.
     * @param projectName - the name of the project to find
     * @return a RallyProject object instance
     */
    public RallyProject FindProject(String projectName) {
        for (RallyWorkspace ws : workspaceList) {
            if (ws.Find(projectName) != null) return ws.Find(projectName);
        }
        return null;
    }

    public RallyRepository FindRepository(String repositoryName) {
        for (RallyRepository rp: repositories) {
            if (rp.getName().compareToIgnoreCase(repositoryName) == 0) return rp;
        }
        return null;
    }

    public RallyBuildDef FindBuildDef(String workspaceName, String projectName, String buildDefName) {
        RallyProject project = FindProject(workspaceName,projectName);
        if (project != null) return project.Find(buildDefName);
        return null;
    }

    public RallyBuildDef FindBuildDef(String workspaceName, String buildDefName) {
        RallyWorkspace ws = FindWorkspace(workspaceName);
        if (ws != null) {
            for (RallyProject pj : ws.getProjects()) {
                if (pj.Find(buildDefName) != null) return pj.Find(buildDefName);
            }
        }
        return null;
    }

    public RallyBuildDef FindBuildDef(String buildDefName) {
        for (RallyWorkspace ws : workspaceList) {
            for (RallyProject pj : ws.getProjects()) {
                if (pj.Find(buildDefName)!= null) return pj.Find(buildDefName);
            }
        }
        return null;
    }

    protected List<RallyRepository> BuildRepositoriesList(JsonArray repositories) throws Exception {

        List<RallyRepository> repositoryList = new ArrayList<RallyRepository>();
        try {
            for (JsonElement repository : repositories) {
                String wsName = ((JsonObject)repository).get("Name").getAsString();
                String wsRef  = ((JsonObject)repository).get("_ref").getAsString();
                RallyRepository rp = new RallyRepository(wsName,wsRef);
                repositoryList.add(rp);
            }
            return repositoryList;
        } catch (Exception e) {
            LOG.error("Could not build Repositories List from Subscription data.");
            LOG.error(e);
            throw e;
        }
    }

    protected List<RallyWorkspace> BuildWorkspaceList(JsonObject subscriptionData) throws Exception {

        List<RallyWorkspace> workspaceList = new ArrayList<RallyWorkspace>();

        try {
            JsonArray workspaces = subscriptionData.get("Workspaces").getAsJsonArray();
            for (JsonElement workspace : workspaces) {

                // Create a Workspace Entry object and add it to the Dictionary
                String wsName = ((JsonObject)workspace).get("Name").getAsString();
                String wsRef  = ((JsonObject)workspace).get("_ref").getAsString();
                RallyWorkspace ws = new RallyWorkspace(wsName,wsRef);
                workspaceList.add(ws);

                // Next, scan through and find all the projects and project references
                JsonArray projects = ((JsonObject)workspace).get("Projects").getAsJsonArray();
                for (JsonElement project : projects) {
                    String pjName = ((JsonObject)project).get("Name").getAsString();
                    String pjRef  = ((JsonObject)project).get("_ref").getAsString();
                    RallyProject pj = new RallyProject(pjName,pjRef, ws);
                    ws.getProjects().add(pj);

                    // Next, scan through and find all the build definitions
                    JsonArray buildDefs = ((JsonObject)project).get("BuildDefinitions").getAsJsonArray();
                    for (JsonElement buildDef : buildDefs) {
                        String bdName = ((JsonObject)buildDef).get("Name").getAsString();
                        String bdRef  = ((JsonObject)buildDef).get("_ref").getAsString();
                        RallyBuildDef bd = new RallyBuildDef(bdName, bdRef, pj);
                        pj.getBuildDefs().add(bd);
                    }
                }
            }
            return workspaceList;
        } catch (Exception e) {
            LOG.error("Could not build Workspace and Projects list from Subscription data.");
            LOG.error(e);
            throw e;
        }
    }

}
