package com.rally.integration.rally.entities;

import java.util.ArrayList;
import java.util.List;

public class RallyProject extends RallyObject implements IRallyObject {

    private RallyWorkspace parent;
    private List<RallyBuildDef> buildDefs;

    public RallyProject(String name, String ref, RallyWorkspace parent) {
        super(name,ref);
        this.parent = parent;
        buildDefs = new ArrayList<RallyBuildDef>();
    }

    public RallyWorkspace getParent() { return parent; }
    public List<RallyBuildDef> getBuildDefs() { return buildDefs;  }

    /**
     * Looks for a Project in a workspace given its name and returns the RallyProject oibject for
     * that projects. This then allows usage of the REF field needed for other Rally calls.
     * @param name - the name of the project to find (case insensitive)
     * @return RallyProject reference which includes the _ref attribute
     */
    public RallyBuildDef Find (String name) {
        for (RallyBuildDef buildDef : buildDefs) {
            if (buildDef.getName().compareToIgnoreCase(name) == 0) return buildDef;
        }
        return null;
    }
}
