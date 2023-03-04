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

package org.netbeans.tests.j2eeserver.plugin;

import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.spi.ServerInstanceDescriptor;
import org.netbeans.tests.j2eeserver.plugin.jsr88.TestDeploymentManager;

/**
 *
 * @author Petr Hejl
 */
public class TestInstanceDescriptor implements ServerInstanceDescriptor {

    private final TestDeploymentManager manager;

    public TestInstanceDescriptor(DeploymentManager manager) {
        this.manager = (TestDeploymentManager) manager;
    }

    public String getHostname() {
        return "localhost";
    }

    public int getHttpPort() {
        return Integer.parseInt(manager.getInstanceProperties().getProperty(InstanceProperties.HTTP_PORT_NUMBER));
    }

    public boolean isLocal() {
        return true;
    }


}
