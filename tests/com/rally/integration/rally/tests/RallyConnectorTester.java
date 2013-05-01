package com.rally.integration.rally.tests;

import com.google.gson.JsonObject;
import com.rally.integration.rally.RallyConfig;
import com.rally.integration.rally.RallyConnector;
import com.rally.integration.rally.RallyManager;
import com.rally.integration.rally.entities.RallyBuildDef;
import com.rally.integration.rally.entities.RallyProject;
import com.rally.integration.rally.entities.RallyWorkspace;
import com.rally.integration.teamcity.FileConfig;
import com.rally.integration.teamcity.SettingsBean;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RallyConnectorTester {

    @Test
    @Ignore("Require Rally server")
    public void testWorkspaceList() throws Exception {
        final RallyConnector connector = new RallyConnector();
        RallyConfig cfg = new RallyConfig();
        cfg.setDefaults();
        connector.setConnectionSettings(cfg);
        connector.getRallyInstance();

        RallyWorkspace ws = connector.getSubscription().FindWorkspace("Enterprise Division (Play)");
        Assert.assertNotNull(ws);

        RallyProject pj = connector.getSubscription().FindProject("Enterprise Division (Play)","Incoming (ES)");
        Assert.assertNotNull(pj);

        RallyProject ps = connector.getSubscription().FindProject("Incoming (ES)");
        Assert.assertNotNull(ps);
        Assert.assertEquals(ps,pj);
    }

    @Test
    @Ignore("Require Rally server")
    public void testGetBuildDef() throws Exception {
        final RallyConnector connector = new RallyConnector();
        FileConfig cfg = new FileConfig(new SettingsBean(new com.rally.integration.rally.RallyConfig()));
        cfg.setDefaults();

        RallyManager manager = new RallyManager(cfg,connector);
        RallyBuildDef def = manager.getRallyBuildDef("test","Enterprise Division (Play)","Memphis Payroll","Memphis Build");
        Assert.assertNotNull(def);

    }


    @Test
    @Ignore("Require Rally server")
    public void testGetChangeSet() throws Exception {
        final RallyConnector connector = new RallyConnector();
        RallyConfig cfg = new RallyConfig();
        cfg.setDefaults();
        connector.setConnectionSettings(cfg);
        connector.getRallyInstance();

        String id1 = connector.FindChangeSet("Comacc","5700");
        Assert.assertNotNull(id1);

        String id2 = connector.FindChangeSet("Comacc","5696");
        Assert.assertNotNull(id2);

        String id3 = connector.FindChangeSet("Comacc","91919191");
        Assert.assertNull(id3);
    }

    @Test
    //@Ignore("Require Rally server")
    public void createAndDeleteBuildTest() throws Exception {
        final RallyConnector connector = new RallyConnector();
        FileConfig cfg = new FileConfig(new SettingsBean(new com.rally.integration.rally.RallyConfig()));
        cfg.setDefaults();

        RallyManager manager = new RallyManager(cfg,connector);
        RallyBuildDef def = manager.getRallyBuildDef("test","Enterprise Division (Play)","Memphis Payroll","Memphis Build");

        DateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");

        JsonObject obj = new JsonObject();
        obj.addProperty("Workspace", def.getWorkspace().getRef());
        obj.addProperty("Duration",1.05);
        obj.addProperty("Message", "Master 4683 Success");
        obj.addProperty("Start", isoFormat.format(new Date()));
        obj.addProperty("Status","SUCCESS");
        obj.addProperty("Number","4683");
        obj.addProperty("Uri", "http://jenkins-build:8080/hudson/view/master/job/master-deploy/4683/");
        obj.addProperty("BuildDefinition",def.getRef());

        String ref = connector.Create("Build",obj);
        Assert.assertNotNull(ref);
        connector.Delete(ref, null);
    }

    @Test
    @Ignore("Require Rally server")
    public void createDefectTest() throws Exception {
        final RallyConnector connector = new RallyConnector();
        FileConfig cfg = new FileConfig(new SettingsBean(new com.rally.integration.rally.RallyConfig()));
        cfg.setDefaults();

        RallyManager manager = new RallyManager(cfg,connector);
        RallyBuildDef def = manager.getRallyBuildDef("test","Enterprise Division (Play)","Memphis Payroll","Memphis Build");

        JsonObject obj = new JsonObject();
        obj.addProperty("workspace", def.getWorkspace().getRef());
        obj.addProperty("ScheduleState","Backlog");
        obj.addProperty("State","Submitted");
        obj.addProperty("Name", "A defect");
        obj.addProperty("Project",def.getParent().getRef());

        String ref = connector.Create("Defect",obj);
        Assert.assertNotNull(ref);
        connector.Delete(ref, null);
    }

}
