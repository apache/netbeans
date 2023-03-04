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

package org.netbeans.modules.j2ee.deployment.impl.bridge;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.j2ee.deployment.impl.Server;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.spi.OptionalDeploymentManagerFactory;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.AsynchronousInstantiatingIterator;
import org.openide.WizardDescriptor.InstantiatingIterator;
import org.openide.WizardDescriptor.Panel;

/**
 *
 * @author Petr Hejl
 */
public class BridgingServerWizardProvider implements org.netbeans.spi.server.ServerWizardProvider {

    private final Server server;

    private final OptionalDeploymentManagerFactory optional;

    public BridgingServerWizardProvider(Server server, OptionalDeploymentManagerFactory optional) {
        this.server = server;
        this.optional = optional;
    }

    public InstantiatingIterator getInstantiatingIterator() {
        if (optional.getAddInstanceIterator() == null) {
            return null;
        }
        return new InstantiatingIteratorBridge(optional.getAddInstanceIterator(), server);
    }

    public String getDisplayName() {
        return server.getDisplayName();
    }

    private static class InstantiatingIteratorBridge implements AsynchronousInstantiatingIterator {

        private final InstantiatingIterator iterator;

        private final Server server;

        public InstantiatingIteratorBridge(InstantiatingIterator iterator, Server server) {
            this.iterator = iterator;
            this.server = server;
        }

        public void uninitialize(WizardDescriptor wizard) {
            iterator.uninitialize(wizard);
        }

        public Set instantiate() throws IOException {
            Set objects = iterator.instantiate();
            if (!objects.isEmpty()) {
                Object value = objects.iterator().next();
                String url = null;
                if (value instanceof String) {
                    url = (String) value;
                } else if (value instanceof InstanceProperties) {
                    url = ((InstanceProperties) value).getProperty(InstanceProperties.URL_ATTR);
                }
                if (url != null) {
                    org.netbeans.api.server.ServerInstance instance =
                            getBridge(ServerRegistry.getInstance().getServerInstance(url));
                    if (instance != null) {
                        objects = new HashSet();
                        objects.add(instance);
                    }
                }
            }
            return objects;
        }

        public void initialize(WizardDescriptor wizard) {
            iterator.initialize(wizard);
        }

        public void removeChangeListener(ChangeListener l) {
            iterator.removeChangeListener(l);
        }

        public void previousPanel() {
            iterator.previousPanel();
        }

        public void nextPanel() {
            iterator.nextPanel();
        }

        public String name() {
            return iterator.name();
        }

        public boolean hasPrevious() {
            return iterator.hasPrevious();
        }

        public boolean hasNext() {
            return iterator.hasNext();
        }

        public Panel current() {
            return iterator.current();
        }

        public void addChangeListener(ChangeListener l) {
            iterator.addChangeListener(l);
        }

        private org.netbeans.api.server.ServerInstance getBridge(org.netbeans.modules.j2ee.deployment.impl.ServerInstance instance) {
            Collection<? extends org.netbeans.spi.server.ServerInstanceProvider> providers = ServerInstanceProviderLookup.getInstance().lookupAll(org.netbeans.spi.server.ServerInstanceProvider.class);
            for (org.netbeans.spi.server.ServerInstanceProvider provider : providers) {
                if (provider instanceof BridgingServerInstanceProvider) {
                    org.netbeans.api.server.ServerInstance bridgingInstance = ((BridgingServerInstanceProvider) provider).getBridge(instance);
                    if (bridgingInstance != null) {
                        return bridgingInstance;
                    }
                }
            }
            return null;
        }

    }
}
