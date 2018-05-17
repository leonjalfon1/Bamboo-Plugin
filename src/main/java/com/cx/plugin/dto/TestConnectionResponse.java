package com.cx.plugin.dto;

import com.cx.restclient.dto.Team;
import com.cx.restclient.sast.dto.Preset;

import java.util.List;

/**
 * Created by galn on 12/02/2017.
 */
public class TestConnectionResponse {

    public String loginResponse;
    public List<Preset> presetList;
    public List<Team> teamPathList;


    public TestConnectionResponse(String loginResponse, List<Preset> presetList, List<Team> teamPathList) {
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

    public List<Preset> getPresetList() {
        return presetList;
    }

    public void setPresetList(List<Preset> presetList) {
        this.presetList = presetList;
    }

    public List<Team> getTeamPathList() {
        return teamPathList;
    }

    public void setTeamPathList(List<Team> teamPathList) {
        this.teamPathList = teamPathList;
    }
}



