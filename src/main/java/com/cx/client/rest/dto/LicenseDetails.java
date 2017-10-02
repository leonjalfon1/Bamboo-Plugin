package com.cx.client.rest.dto;

/**
 * Created by Galn on 27/08/2017.
 */
public class LicenseDetails {
    public String organizationName;
    public String HID;
    public long maxConcurrentScans;
    public boolean osaEnabled;
    public String osaExpirationDate;

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getHID() {
        return HID;
    }

    public void setHID(String HID) {
        this.HID = HID;
    }

    public long getMaxConcurrentScans() {
        return maxConcurrentScans;
    }

    public void setMaxConcurrentScans(long maxConcurrentScans) {
        this.maxConcurrentScans = maxConcurrentScans;
    }

    public boolean getOsaEnabled() {
        return osaEnabled;
    }

    public void setOsaEnabled(boolean osaEnabled) {
        this.osaEnabled = osaEnabled;
    }

    public String getOsaExpirationDate() {
        return osaExpirationDate;
    }

    public void setOsaExpirationDate(String osaExpirationDate) {
        this.osaExpirationDate = osaExpirationDate;
    }
}
