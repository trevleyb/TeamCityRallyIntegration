package com.rally.integration.rally.entities;

import com.google.gson.JsonObject;

public class RallyBuildDef extends RallyObject implements IRallyObject {

    private RallyProject parent;

    public RallyBuildDef(String name, String ref, RallyProject parent) {
        super(name, ref);
        this.parent = parent;
    }

    public RallyProject getParent() {
        return parent;
    }

    public RallyWorkspace getWorkspace() {
        return getParent().getParent();
    }

    public JsonObject toJson() {

        if (parent == null || parent.getParent() == null ||
                parent.getRef() == null || parent.getParent().getRef() == null) return null;

        JsonObject obj = new JsonObject();
        obj.addProperty("workspace", parent.getParent().getRef());
        obj.addProperty("name", this.getName());
        obj.addProperty("description", this.getName());
        obj.addProperty("project", parent.getRef());
        return obj;
    }
}
