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
package org.netbeans.modules.javaee.wildfly.ide;

import java.util.Map;
import java.util.WeakHashMap;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.spi.DatasourceManager;
import org.netbeans.modules.j2ee.deployment.plugins.spi.FindJSPServlet;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.spi.MessageDestinationDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.spi.OptionalDeploymentManagerFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.ServerInstanceDescriptor;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;
import org.netbeans.modules.javaee.wildfly.WildflyDeploymentManager;
import org.netbeans.modules.javaee.wildfly.config.WildflyDatasourceManager;
import org.netbeans.modules.javaee.wildfly.config.WildflyMessageDestinationManager;
import org.netbeans.modules.javaee.wildfly.ide.ui.WildflyInstantiatingIterator;
import org.openide.WizardDescriptor.InstantiatingIterator;

/**
 *
 * @author Martin Adamek
 */
public class WildflyOptionalDeploymentManagerFactory extends OptionalDeploymentManagerFactory {

    private final Map<InstanceProperties, StartServer> serverCache
            = new WeakHashMap<InstanceProperties, StartServer>();

    @Override
    public synchronized StartServer getStartServer(DeploymentManager dm) {
        InstanceProperties ip = InstanceProperties.getInstanceProperties(((WildflyDeploymentManager) dm).getUrl());
        if (serverCache.containsKey(ip)) {
            return serverCache.get(ip);
        }
        StartServer startServer = new WildflyStartServer(dm);
        serverCache.put(ip, startServer);
        return startServer;
    }

    @Override
    public IncrementalDeployment getIncrementalDeployment(DeploymentManager dm) {
        WildflyDeploymentManager wdm = (WildflyDeploymentManager) dm;
       return new WildflyIncrementalDeployment(wdm);
    }

    @Override
    public FindJSPServlet getFindJSPServlet(DeploymentManager dm) {
        return new WildFlyFindJSPServlet((WildflyDeploymentManager) dm);
    }

    @Override
    public InstantiatingIterator getAddInstanceIterator() {
        return new WildflyInstantiatingIterator();
    }

    @Override
    public DatasourceManager getDatasourceManager(DeploymentManager dm) {
        if (!(dm instanceof WildflyDeploymentManager)) {
            throw new IllegalArgumentException("Wrong instance of DeploymentManager: " + dm);
        }

        WildflyDeploymentManager jbdm = ((WildflyDeploymentManager) dm);
        return new WildflyDatasourceManager(jbdm);
    }

    @Override
    public MessageDestinationDeployment getMessageDestinationDeployment(DeploymentManager dm) {
        if (!(dm instanceof WildflyDeploymentManager)) {
            throw new IllegalArgumentException("Wrong instance of DeploymentManager: " + dm);
        }
        return new WildflyMessageDestinationManager(((WildflyDeploymentManager) dm));
    }

    @Override
    public ServerInstanceDescriptor getServerInstanceDescriptor(DeploymentManager dm) {
        return new WildFlyInstanceDescriptor((WildflyDeploymentManager) dm);
    }

}
