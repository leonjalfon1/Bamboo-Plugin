/*
 * Copyright 2012 Bunney Apps, Brisbane, Australia.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.cx.plugin.admin;

import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.cx.client.CxClientService;
import com.cx.client.CxClientServiceImpl;
import org.apache.commons.lang.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Gavin Bunney
 */
public class ConfigureEnvironmentAction extends BambooActionSupport {

    private String url;
    private String userPass;
    private String userName;
    private EnvironmentConfigManager environmentConfigManager;
    private CxClientService cxClientService = null;
    private String testing;

    public ConfigureEnvironmentAction() {
    }

    public ConfigureEnvironmentAction(EnvironmentConfigManager environmentConfigManager) {
        this.environmentConfigManager =  environmentConfigManager;
    }

    @Override
    public void validate() {
        clearErrorsAndMessages();

        if (StringUtils.isBlank(userName)) {
            addFieldError("userName", "Please specify a userName for the environment.");
        }

        if (StringUtils.isBlank(url)) {
            addFieldError("url", "Please specify a URL of the environment.");
        } else {
            try {
                new URL(url);

            } catch (MalformedURLException mue) {
                addFieldError("url", "Please specify a valid URL of the environment.");
            }
        }
    }

    public String doCreate() throws Exception {
        if (testConnection()){
            return "input";
        }
        return "success";
    }


    private boolean testConnection() {

        EnvironmentConfig testDetails = new EnvironmentConfig();
        testDetails.setUrl(url);
        testDetails.setPass(userPass);
        testDetails.setUserName(userName);
        try {
            if (cxClientService == null) {
                cxClientService = new CxClientServiceImpl(new URL(url), userName, userPass); //TODO ask dor when change the password
            }
            cxClientService.loginToServer();
        } catch (Exception e) {
            addActionError("Connection failed - check the url and the build file exists"); //TODO- what s it??
            return false;
        }

        addActionMessage("Connection successful!");
        return true;
    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserPass() {
        return userPass;
    }

    public void setUserPass(String userPass) {
        this.userPass = userPass;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public EnvironmentConfigManager getEnvironmentConfigManager() {
        return environmentConfigManager;
    }

    public void setEnvironmentConfigManager(EnvironmentConfigManager environmentConfigManager) {
        this.environmentConfigManager = environmentConfigManager;
    }

    public String getTesting() {
        return testing;
    }
    public void setTesting(String testing) {
        this.testing = testing;
    }
    private boolean isTesting() {
        return "Test".equals(getTesting());
    }
}
