package com.rally.integration.rally;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.intellij.openapi.diagnostic.Logger;
import com.rally.integration.rally.entities.RallyBuildDef;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.WebLinks;
import jetbrains.buildServer.vcs.SVcsModification;
import jetbrains.buildServer.vcs.SelectPrevBuildPolicy;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class RallyBuild {
    private static final int MESSAGE_SIZE = 4000;
    private static final SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private static final Logger LOG = Logger.getInstance(RallyConnector.class.getName());

    private final SRunningBuild buildInfo;
    private final WebLinks webLinks;
    private final RallyConfig config;

    //
    // Creates a DTO object that manages the details of a build as passed from Team City
    //
    public RallyBuild(SRunningBuild buildInfo, WebLinks webLinks, RallyConfig config) {
        this.buildInfo = buildInfo;
        this.webLinks = webLinks;
        this.config = config;
    }

    protected SRunningBuild getTeamCityBuildInfo() { return buildInfo; }

    //
    // Properties to return data from the Team City build information DTO
    //
    public String getBuildName()   { return buildInfo.getFullName(); }
    public String getBuildID()     { return buildInfo.getBuildNumber(); }
    public String getStartTime()   { return isoFormat.format(buildInfo.getStartDate());}
    public String getUrl()         { return webLinks.getViewResultsUrl(buildInfo);}
    public String getStatus()      { return buildInfo.getBuildStatus().toString();}
    public float  getDuration()    { return buildInfo.getElapsedTime(); }
    public boolean isSuccessful(){ return buildInfo.getBuildStatus().isSuccessful(); }
    public boolean isForced()    { return buildInfo.getTriggeredBy().isTriggeredByUser(); }

    private String truncate(String string) {
        if (string == null) return "";
        return (string.length() <= MESSAGE_SIZE) ? string : string.substring(string.length()-MESSAGE_SIZE, string.length());
    }

    public List<String> getChangeSetIDs() {
        List<String> changeIDs = new ArrayList<String>();
        if (getBuildChanges() != null && getBuildChanges().size() >0) {
            for (SVcsModification s : getBuildChanges()) {
                String id = s.getVcsRoot().getName() + ":" + s.getDisplayVersion();
                changeIDs.add(id);
            }
        }
        return changeIDs;
    }

    public List<SVcsModification> getBuildChanges() {
        buildInfo.getChanges(SelectPrevBuildPolicy.SINCE_LAST_BUILD, true);
        return buildInfo.getContainingChanges();
    }

    /**
     * Provides access to the Properties collection from the Build Server. Properties need to be defined to provide
     * information on where to place this build within Rally. Properties include the Workspace and Project.
     * @param name - the name of the Property to find
     * @return the value of the provided property
     * @throws InvalidParameterException if the propoerty oes not exist.
     */
    public String GetProperty(String name) throws InvalidParameterException {
        try {
            return buildInfo.getParametersProvider().get(name);
        } catch (Exception e) {
            LOG.error("Could not find property '" + name + "' in the Build configuration.");
            return null;
        }
    }

    //
    // Converts the contents of this DTO into a JSON object that can be added to Rally.
    //
    public JsonObject toJSON(RallyBuildDef buildDef, List<String> changeSets) throws IOException {
        JsonObject obj = new JsonObject();
        obj.addProperty("workspace", buildDef.getWorkspace().getRef());
        obj.addProperty("buildDefinition",buildDef.getRef());
        obj.addProperty("duration",getDuration());
        obj.addProperty("message", getBuildName());
        obj.addProperty("number",getBuildID());
        obj.addProperty("start",getStartTime());
        obj.addProperty("status",convertStatus(getStatus()));
        obj.addProperty("uri", getUrl());

        if (changeSets != null && changeSets.size() >0) {
            JsonArray changeSetList = new JsonArray();
            for (String id : changeSets) {
                changeSetList.add(new JsonPrimitive(id));
            }
            obj.add("Changesets", changeSetList);
        }
        return obj;
    }

    private String convertStatus(String status) {
        // Valid RALLY Status Codes are: [SUCCESS, FAILURE, INCOMPLETE, UNKNOWN, NO BUILDS]
        // TeamCity status codes are: ERROR, FAILURE, NORMAL, SUCCESS, UNKNOWN or WARNING
        LOG.info("TeamCity Status Code: " + status);
        if (status.toUpperCase().equals("SUCCESS")) return "SUCCESS";
        if (status.toUpperCase().equals("NORMAL")) return "SUCCESS";
        if (status.toUpperCase().equals("FAILURE")) return "FAILURE";
        if (status.toUpperCase().equals("ERROR")) return "FAILURE";
        if (status.toUpperCase().equals("WARNING")) return "INCOMPLETE";
        if (status.toUpperCase().equals("UNKNOWN")) return "UNKNOWN";
        return "UNKNOWN";
    }
}