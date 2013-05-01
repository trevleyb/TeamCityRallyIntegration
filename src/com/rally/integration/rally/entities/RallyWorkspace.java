package com.rally.integration.rally.entities;

import java.util.ArrayList;
import java.util.List;

public class RallyWorkspace extends RallyObject implements IRallyObject {

    private List<RallyProject> projects;

    public RallyWorkspace(String name, String ref) {
        super(name,ref);
        projects = new ArrayList<RallyProject>();
    }

    public List<RallyProject> getProjects() { return projects;  }

    /**
     * Looks for a Project in a workspace given its name and returns the RallyProject oibject for
     * that projects. This then allows usage of the REF field needed for other Rally calls.
     * @param name - the name of the project to find (case insensitive)
     * @return RallyProject reference which includes the _ref attribute
     */
    public RallyProject Find (String name) {
        for (RallyProject project : projects) {
            if (project.getName().compareToIgnoreCase(name) == 0) return project;
        }
        return null;
    }
}
