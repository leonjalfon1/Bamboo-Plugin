package com.cx.plugin.dto;

/**
 * Created by galn on 12/02/2017.
 */
public class CxClass {

    public String id;

    public String value;

    public CxClass(String id, String value) {
        this.id = id;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CxClass)) return false;

        CxClass cxClass = (CxClass) o;

        if (getId() != null ? !getId().equals(cxClass.getId()) : cxClass.getId() != null) return false;
        return getValue() != null ? getValue().equals(cxClass.getValue()) : cxClass.getValue() == null;

    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getValue() != null ? getValue().hashCode() : 0);
        return result;
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
