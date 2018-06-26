/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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
