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

package org.netbeans.modules.j2ee.genericserver.ide;

import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;

/**
 *
 * @author Martin Adamek
 */
public class GSStartServer extends StartServer {

    public ProgressObject startDebugging(Target target) {
        return null;
    }

    public boolean isDebuggable(Target target) {
        return false;
    }

    public boolean isAlsoTargetServer(Target target) {
        return true;
    }

    public ServerDebugInfo getDebugInfo(Target target) {
        return null;
    }

    public boolean supportsStartDeploymentManager() {
        return false;
    }

    public ProgressObject stopDeploymentManager() {
        return null;
    }

    public ProgressObject startDeploymentManager() {
        return null;
    }

    public boolean needsStartForTargetList() {
        return false;
    }

    public boolean needsStartForConfigure() {
        return false;
    }

    public boolean needsStartForAdminConfig() {
        return false;
    }

    public boolean isRunning() {
        return false;
    }
    
}
