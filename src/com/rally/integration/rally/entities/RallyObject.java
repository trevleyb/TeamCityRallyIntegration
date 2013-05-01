package com.rally.integration.rally.entities;

public class RallyObject implements IRallyObject {

    private String name;
    private String ref;

    public RallyObject(String name, String ref) {
        this.name = name;
        this.ref = ref;
    }

    public String getName() { return name;  }
    public void setName(String name) { this.name = name; }
    public String getRef() { return ref; }
    public void setRef(String ref) { this.ref = ref; }

}
