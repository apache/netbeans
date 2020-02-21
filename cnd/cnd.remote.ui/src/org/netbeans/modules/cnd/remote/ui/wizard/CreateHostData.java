/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.cnd.remote.ui.wizard;

import org.netbeans.modules.cnd.spi.remote.setup.support.HostSetupResultImpl;
import org.netbeans.modules.cnd.api.toolchain.ToolsCacheManager;

/**
 * A structure that contains data used by wizard components
 * (a replacements for WizardDescriptor.getProperty() and WizardDescriptor.setProperty())
 */
/*package*/ class CreateHostData extends HostSetupResultImpl {

    private final ToolsCacheManager cacheManager;
    private int port = 22;
    private String hostName = "";
    private String userName;
    private final boolean manageUser;
    private boolean searchTools;
    private boolean checkACL;

    public CreateHostData(ToolsCacheManager toolsCacheManager, boolean manageUser) {
        this.cacheManager = toolsCacheManager;
        this.manageUser = manageUser;
        this.userName = System.getProperty("user.name", ""); // NOI18N
        searchTools = true;
    }

    public ToolsCacheManager getCacheManager() {
        return cacheManager;
    }

    public synchronized String getHostName() {
        return hostName;
    }

    public boolean isManagingUser() {
        return manageUser;
    }

    public synchronized String getUserName() {
        return userName;
    }

    public synchronized void setUserName(String name) {
        userName = name;
    }
    
    public synchronized void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public synchronized int getPort() {
        return port;
    }

    public synchronized void setPort(int port) {
        this.port = port;
    }

    public synchronized boolean getSearchTools() {
        return searchTools;
    }

    public synchronized void setSearchTools(boolean searchTools) {
        this.searchTools = searchTools;
    }

    public boolean isACLEnabled() {
        return checkACL;
    }

    public void enableACL(boolean enable) {
        this.checkACL = enable;
    }
    
    @Override
    public String toString() {
        return "CreateHostData " + hostName + ':' + port; // NOI18N
    }
}
