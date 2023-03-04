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

package org.netbeans.modules.j2ee.deployment.plugins.spi.support;

import java.util.Map;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.deployment.plugins.spi.AntDeploymentProvider;
import org.netbeans.modules.j2ee.deployment.plugins.spi.DatasourceManager;
import org.netbeans.modules.j2ee.deployment.plugins.spi.FindJSPServlet;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.spi.JDBCDriverDeployer;
import org.netbeans.modules.j2ee.deployment.plugins.spi.MessageDestinationDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.spi.OptionalDeploymentManagerFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.ServerInitializationException;
import org.netbeans.modules.j2ee.deployment.plugins.spi.ServerInstanceDescriptor;
import org.netbeans.modules.j2ee.deployment.plugins.spi.ServerLibraryManager;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;
import org.netbeans.modules.j2ee.deployment.plugins.spi.TargetModuleIDResolver;
import org.openide.WizardDescriptor.InstantiatingIterator;

/**
 * Provides a proxying implementation of {@link OptionalDeploymentManagerFactory}.
 * Handles optional <code>noInitializationFinish</code> attribute as a way
 * to prevent delegate's {@link #finishServerInitialization()} to be called.
 * This can be useful in situation when we want to prevent loading unnecessary
 * code (from delegate) being loaded during the {@link #finishServerInitialization()}
 * call. Designed to be used via XML layer filesystem.
 *
 * @author Petr Hejl
 * @since 1.66
 */
public final class ProxyOptionalFactory extends OptionalDeploymentManagerFactory {

    private final Map attributes;

    private final boolean noInitializationFinish;

    /* GuardedBy("this") */
    private OptionalDeploymentManagerFactory delegate;

    private ProxyOptionalFactory(Map attributes) {
        this.attributes = attributes;

        this.noInitializationFinish = Boolean.TRUE.equals(
                attributes.get("noInitializationFinish")); // NOI18N
    }

    public static ProxyOptionalFactory create(Map map) {
        return new ProxyOptionalFactory(map);
    }

    @Override
    public boolean isCommonUIRequired() {
        return getDelegate().isCommonUIRequired();
    }

    @Override
    public TargetModuleIDResolver getTargetModuleIDResolver(DeploymentManager dm) {
        return getDelegate().getTargetModuleIDResolver(dm);
    }

    @Override
    public StartServer getStartServer(DeploymentManager dm) {
        return getDelegate().getStartServer(dm);
    }

    @Override
    public ServerInstanceDescriptor getServerInstanceDescriptor(DeploymentManager dm) {
        return getDelegate().getServerInstanceDescriptor(dm);
    }

    @Override
    public MessageDestinationDeployment getMessageDestinationDeployment(DeploymentManager dm) {
        return getDelegate().getMessageDestinationDeployment(dm);
    }

    @Override
    public JDBCDriverDeployer getJDBCDriverDeployer(DeploymentManager dm) {
        return getDelegate().getJDBCDriverDeployer(dm);
    }

    @Override
    public IncrementalDeployment getIncrementalDeployment(DeploymentManager dm) {
        return getDelegate().getIncrementalDeployment(dm);
    }

    @Override
    public FindJSPServlet getFindJSPServlet(DeploymentManager dm) {
        return getDelegate().getFindJSPServlet(dm);
    }

    @Override
    public DatasourceManager getDatasourceManager(DeploymentManager dm) {
        return getDelegate().getDatasourceManager(dm);
    }

    @Override
    public AntDeploymentProvider getAntDeploymentProvider(DeploymentManager dm) {
        return getDelegate().getAntDeploymentProvider(dm);
    }

    @Override
    public InstantiatingIterator getAddInstanceIterator() {
        return getDelegate().getAddInstanceIterator();
    }

    @Override
    public void finishServerInitialization() throws ServerInitializationException {
        if (!noInitializationFinish) {
            getDelegate().finishServerInitialization();
        }
    }

    @Override
    public ServerLibraryManager getServerLibraryManager(DeploymentManager dm) {
        return getDelegate().getServerLibraryManager(dm);
    }

    private OptionalDeploymentManagerFactory getDelegate() {
        synchronized (this) {
            if (delegate != null) {
                return delegate;
            }
        }

        OptionalDeploymentManagerFactory factory = (OptionalDeploymentManagerFactory) attributes.get("delegate"); // NOI18N
        assert factory != null : "Delegate is null";
        
        synchronized (this) {
            if (delegate == null) {
                delegate = factory;
            }
            return delegate;
        }
    }

}
