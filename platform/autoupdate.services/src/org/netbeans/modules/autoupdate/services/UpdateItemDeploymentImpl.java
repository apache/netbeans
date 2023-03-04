/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.autoupdate.services;

import org.netbeans.spi.autoupdate.CustomInstaller;
import org.netbeans.spi.autoupdate.CustomUninstaller;

/**
 *
 * @author Jiri Rechtacek
 */
public class UpdateItemDeploymentImpl {
    private Boolean needsRestart;
    private Boolean isGlobal;
    private String targetCluster;
    private CustomInstaller installer;
    private CustomUninstaller uninstaller;
    
    /** Creates a new instance of UpdateDeploymentImpl */
    public UpdateItemDeploymentImpl (Boolean needsRestart, Boolean isGlobal, String targetCluster, CustomInstaller installer, CustomUninstaller uninstaller) {
        this.needsRestart = needsRestart;
        this.isGlobal = isGlobal;
        this.targetCluster = targetCluster;
        this.installer = installer;
        this.uninstaller = uninstaller;
    }
    
    public String getTargetCluster () {
        return targetCluster;
    }
    
    public Boolean needsRestart () {
        return needsRestart;
    }
    
    public void setNeedsRestart(Boolean needsRestart) {
        this.needsRestart = needsRestart;
    }
    
    public Boolean isGlobal () {
        return isGlobal;
    }
    
    public CustomInstaller getCustomInstaller () {
        return installer;
    }
    
    public CustomUninstaller getCustomUninstaller () {
        return uninstaller;
    }
}
