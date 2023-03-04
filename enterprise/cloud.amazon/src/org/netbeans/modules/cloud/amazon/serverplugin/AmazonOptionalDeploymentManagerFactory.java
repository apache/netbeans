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
package org.netbeans.modules.cloud.amazon.serverplugin;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.cloud.amazon.ui.serverplugin.AmazonJ2EEServerWizardIterator;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo;
import org.netbeans.modules.j2ee.deployment.plugins.spi.FindJSPServlet;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.spi.OptionalDeploymentManagerFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.ServerInitializationException;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;
import org.openide.WizardDescriptor.InstantiatingIterator;

/**
 *
 */
public class AmazonOptionalDeploymentManagerFactory extends OptionalDeploymentManagerFactory {

    @Override
    public StartServer getStartServer(DeploymentManager dm) {
        return new AmazonStartServer();
    }

    @Override
    public IncrementalDeployment getIncrementalDeployment(DeploymentManager dm) {
        return null;
    }

    @Override
    public InstantiatingIterator getAddInstanceIterator() {
        return new AmazonJ2EEServerWizardIterator();
    }
    
    @Override
    public FindJSPServlet getFindJSPServlet(DeploymentManager dm) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isCommonUIRequired() {
        return false;
    }

    @Override
    public void finishServerInitialization() throws ServerInitializationException {
        AmazonJ2EEServerInstanceProvider.getProvider().refreshServers();
    }

    public static final class AmazonStartServer extends StartServer {

        @Override
        public boolean isAlsoTargetServer(Target target) {
            return true;
        }

        @Override
        public boolean supportsStartDeploymentManager() {
            return false;
        }

        @Override
        public ProgressObject startDeploymentManager() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ProgressObject stopDeploymentManager() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean needsStartForConfigure() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean needsStartForTargetList() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean needsStartForAdminConfig() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isRunning() {
            return true;
        }

        @Override
        public boolean isDebuggable(Target target) {
            return false;
        }

        @Override
        public ProgressObject startDebugging(Target target) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ServerDebugInfo getDebugInfo(Target target) {
            return null;
        }
        
    }
}
