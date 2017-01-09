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

import com.atlassian.bamboo.bandana.PlanAwareBandanaContext;
//import com.atlassian.bamboo.security.StringEncrypter;
import com.atlassian.bandana.BandanaManager;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Gavin Bunney
 */
public class EnvironmentConfigManager implements Serializable {

    private transient BandanaManager bandanaManager;
    private final List<EnvironmentConfig> configuredEnvironments = new CopyOnWriteArrayList<EnvironmentConfig>();
    private AtomicLong nextAvailableId = new AtomicLong(0);

    public EnvironmentConfigManager(BandanaManager bandanaManager) {
        setBandanaManager(bandanaManager);
    }

    public List<EnvironmentConfig> getAllEnvironmentConfigs() {
        List<EnvironmentConfig> allConfigs = getAllEnvironmentConfigs();
        return allConfigs;//Lists.newArrayList(configuredEnvironments);
    }

    public void deleteEnvironmentConfiguration(final long id) {
        for (EnvironmentConfig environmentConfig : configuredEnvironments) {
            configuredEnvironments.remove(environmentConfig);
            break;
        }
    }


    public void setBandanaManager(BandanaManager bandanaManager) {
        this.bandanaManager = bandanaManager;

        Integer configCount = (Integer) bandanaManager.getValue(PlanAwareBandanaContext.GLOBAL_CONTEXT, "count");
        if (configCount == null)
            return;


        for (int idx = 0; idx < configCount; ++idx) {

            configuredEnvironments.add(new EnvironmentConfig((String) bandanaManager.getValue(PlanAwareBandanaContext.GLOBAL_CONTEXT, "userName"),
                    (String) bandanaManager.getValue(PlanAwareBandanaContext.GLOBAL_CONTEXT, "url"),
                    (String) bandanaManager.getValue(PlanAwareBandanaContext.GLOBAL_CONTEXT, "password")));
        }
    }
}