package com.cx.plugin.dto;

import java.util.List;

/**
 * Created by galn on 12/02/2017.
 */
public class TestConnectionResponse {

    public String loginResponse;
    public List<CxClass> presetList;
    public List<CxClass> teamPathList;


    public TestConnectionResponse(String loginResponse, List<CxClass> presetList, List<CxClass> teamPathList) {
        this.loginResponse = loginResponse;
        this.presetList = presetList;
        this.teamPathList = teamPathList;
    }

    public TestConnectionResponse(String loginResponse) {
        this.loginResponse = loginResponse;
    }

    public String getLoginResponse() {
        return loginResponse;
    }

    public void setLoginResponse(String loginResponse) {
        this.loginResponse = loginResponse;
    }

    public List<CxClass> getPresetList() {
        return presetList;
    }

    public void setPresetList(List<CxClass> presetList) {
        this.presetList = presetList;
    }

    public List<CxClass> getTeamPathList() {
        return teamPathList;
    }

    public void setTeamPathList(List<CxClass> teamPathList) {
        this.teamPathList = teamPathList;
    }
}



