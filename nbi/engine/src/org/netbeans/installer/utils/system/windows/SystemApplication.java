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

package org.netbeans.installer.utils.system.windows;

import java.io.File;

/**
 *
 * @author Dmitry Lipin
 */
public class SystemApplication {
    private String location;
    private String friendlyName;
    private String command;
    private Boolean useByDefault;
    private boolean addOpenWithList;
    
    public SystemApplication(String location) {
        this.location = location;
    }
    public SystemApplication(File file) {
        this((file!=null) ? file.getPath() : null);
    }
    protected SystemApplication(SystemApplication sapp) {
        location=sapp.location;
        friendlyName=sapp.friendlyName;
        command = sapp.command;
        useByDefault = sapp.isUseByDefault();
        addOpenWithList = sapp.isAddOpenWithList();
    }
    public String getLocation() {
        return location;
    }

    public void setLocation(String appLocation) {
        this.location = appLocation;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String appFriendlyName) {
        this.friendlyName = appFriendlyName;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
    /**
     * @param useByDefault If it is <code>true</code>, then the current app would be set as default.<br>
     * If it is <code>false</code>, then the current app would not be set as default.<br>
     * If it is <code>null</code>, then the current app would be set as default only if there is no default app set for the particular extension yet.
     */
    public void setByDefault(Boolean useByDefault) {
        this.useByDefault = useByDefault;
    }
    
    public void setOpenWithList(boolean addOpenWithList) {
        this.addOpenWithList = addOpenWithList;
    }

    public Boolean isUseByDefault() {
        return useByDefault;
    }

    public boolean isAddOpenWithList() {
        return addOpenWithList;
    }
    
}
