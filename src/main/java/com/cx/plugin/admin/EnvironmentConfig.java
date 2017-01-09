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

import java.io.Serializable;

/**
 * @author Gavin Bunney
 */
public class EnvironmentConfig implements Serializable {

    private String id;
    private String pass;
    private String userName;
    private String url;


    public EnvironmentConfig() {
    }

    public EnvironmentConfig(String pass, String name, String url) {
        this.pass = pass;
        this.userName = name;
        this.url = url;
    }

    public EnvironmentConfig(EnvironmentConfig environmentConfig) {
        this.id = environmentConfig.id;
        this.pass = environmentConfig.pass;
        this.userName = environmentConfig.userName;
        this.url = environmentConfig.url;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}