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
package org.netbeans.modules.j2ee.weblogic9.optional;

import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.spi.DatasourceManager;
import org.netbeans.modules.j2ee.deployment.plugins.spi.FindJSPServlet;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.spi.JDBCDriverDeployer;
import org.netbeans.modules.j2ee.deployment.plugins.spi.MessageDestinationDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.spi.OptionalDeploymentManagerFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.ServerInstanceDescriptor;
import org.netbeans.modules.j2ee.deployment.plugins.spi.ServerLibraryManager;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;
import org.netbeans.modules.j2ee.deployment.plugins.spi.TargetModuleIDResolver;
import org.netbeans.modules.j2ee.weblogic9.WLDeploymentFactory;
import org.netbeans.modules.j2ee.weblogic9.config.WLDatasourceManager;
import org.netbeans.modules.j2ee.weblogic9.config.WLMessageDestinationDeployment;
import org.netbeans.modules.j2ee.weblogic9.config.WLServerLibraryManager;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLDeploymentManager;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLDriverDeployer;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLIncrementalDeployment;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLTargetModuleIDResolver;
import org.netbeans.modules.j2ee.weblogic9.ui.wizard.WLInstantiatingIterator;
import org.openide.WizardDescriptor.InstantiatingIterator;


/**
 * An entry point to the plugin's optional functionality, such as server
 * start/stop, incremental deployment, custom wizard for instance addition and
 * the ability to locate the servlet for a jsp page.
 *
 * @author Kirill Sorokin
 */
public class WLOptionalDeploymentManagerFactory extends OptionalDeploymentManagerFactory {

    /**
     * Returns an object responsible for starting a particular server instance.
     * The information about the instance is fetched from the supplied
     * deployment manager.
     *
     * @param dm the server's deployment manager
     *
     * @return an object for starting/stopping the server
     */
    @Override
    public StartServer getStartServer(DeploymentManager dm) {
        return new WLStartServer((WLDeploymentManager) dm);
    }

    /**
     * Returns an object responsible for performing incremental deployment on
     * a particular server instance. The instance information should be fetched
     * from the supplied deployment manager.
     * We do not support that, thus return null
     *
     * @param dm the server's deployment manager
     *
     * @return an object for performing the incremental deployment, i.e. null
     */
    @Override
    public IncrementalDeployment getIncrementalDeployment(DeploymentManager dm) {
        return new WLIncrementalDeployment((WLDeploymentManager) dm);
    }

    /**
     * Returns an object responsible for finding a corresponsing servlet for a
     * given jsp deployed on a particular server instance. Instance data should
     * be fetched from the supplied deployment manager.
     * We do not support that, thus return null
     *
     * @param dm the server's deployment manager
     *
     * @return an object for finding the servlet, i.e. null
     */
    @Override
    public FindJSPServlet getFindJSPServlet(DeploymentManager dm) {
        WLDeploymentManager manager = (WLDeploymentManager) dm;
        if (!manager.isRemote()) {
            return new WLFindJSPServlet(manager);
        }
        return null;
    }

    @Override
    public DatasourceManager getDatasourceManager(DeploymentManager dm) {
        return new WLDatasourceManager((WLDeploymentManager) dm);
    }

    @Override
    public MessageDestinationDeployment getMessageDestinationDeployment(DeploymentManager dm) {
        return new WLMessageDestinationDeployment((WLDeploymentManager) dm);
    }

    @Override
    public JDBCDriverDeployer getJDBCDriverDeployer(DeploymentManager dm) {
        return new WLDriverDeployer((WLDeploymentManager) dm);
    }

    /**
     * Returns an instance of the custom wizard for adding a server instance.
     *
     * @return a custom wizard
     */
    @Override
    public InstantiatingIterator getAddInstanceIterator() {
        return new WLInstantiatingIterator();
    }

    @Override
    public ServerInstanceDescriptor getServerInstanceDescriptor(DeploymentManager dm) {
        return new WLServerInstanceDescriptor((WLDeploymentManager) dm);
    }

    @Override
    public ServerLibraryManager getServerLibraryManager(DeploymentManager dm) {
        WLDeploymentManager manager = (WLDeploymentManager) dm;
        if (!manager.isRemote()) {
            return new WLServerLibraryManager(manager);
        }
        return null;
    }

    @Override
    public TargetModuleIDResolver getTargetModuleIDResolver(DeploymentManager dm) {
        return new WLTargetModuleIDResolver((WLDeploymentManager) dm);
    }

    private static class WLServerInstanceDescriptor implements ServerInstanceDescriptor {

        private final WLDeploymentManager manager;

        public WLServerInstanceDescriptor(WLDeploymentManager manager) {
            this.manager = manager;
        }

        @Override
        public String getHostname() {
            return manager.getCommonConfiguration().getHost();
        }

        @Override
        public int getHttpPort() {
            return manager.getCommonConfiguration().getPort();
        }

        @Override
        public boolean isLocal() {
            return !manager.isRemote();
        }
    }
}
