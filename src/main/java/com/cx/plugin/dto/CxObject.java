package com.cx.plugin.dto;

/**
 * Created by galn on 27/12/2016.
 */
public class CxObject {

    public String id;
    public String name;

    public CxObject(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
