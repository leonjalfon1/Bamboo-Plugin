package com.cx.plugin.configuration.dto;

import com.cx.restclient.configuration.CxScanConfig;

/**
 * Created by Galn on 11/8/2018.
 */
public class BambooScanConfig extends CxScanConfig{
    private boolean hideResults = false;

    public BambooScanConfig() {
        super();
    }

    public boolean getHideResults() {
        return hideResults;
    }

    public void setHideResults(boolean hideResults) {
        this.hideResults = hideResults;
    }
}
