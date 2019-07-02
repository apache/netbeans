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

package org.netbeans.modules.server.test;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.spi.server.ServerInstanceFactory;
import org.netbeans.spi.server.ServerInstanceImplementation;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author Petr Hejl
 */
public final class MockInstanceImplementation implements ServerInstanceImplementation {

    private final MockInstanceProvider provider;

    private final String serverName;

    private final String instanceName;

    private final boolean removable;

    private ServerInstance serverInstance;

    private JPanel customizer;

    private MockInstanceImplementation(MockInstanceProvider provider, String serverName,
            String instanceName, boolean removable) {

        this.provider = provider;
        this.serverName = serverName;
        this.instanceName = instanceName;
        this.removable = removable;
    }

    public static MockInstanceImplementation createInstance(MockInstanceProvider provider,
            String serverName, String instanceName, boolean removable) {

        MockInstanceImplementation created = new MockInstanceImplementation(
                provider, serverName, instanceName, removable);
        created.serverInstance = ServerInstanceFactory.createServerInstance(created);
        return created;
    }

    public Node getFullNode() {
        return new AbstractNode(Children.LEAF) {

            @Override
            public String getDisplayName() {
                return instanceName;
            }

        };
    }

    public Node getBasicNode() {
        return new AbstractNode(Children.LEAF) {

            @Override
            public String getDisplayName() {
                return instanceName;
            }

        };
    }

    public JComponent getCustomizer() {
        synchronized (this) {
            if (customizer == null) {
                customizer = new JPanel();
                customizer.add(new JLabel(instanceName));
            }
            return customizer;
        }
    }

    public String getDisplayName() {
        return instanceName;
    }

    public String getServerDisplayName() {
        return serverName;
    }

    public boolean isRemovable() {
        return removable;
    }

    public void remove() {
        provider.removeInstance(serverInstance);
    }

    public ServerInstance getServerInstance() {
        return serverInstance;
    }

    @Override
    public String getProperty(String key) {
        return serverInstance.getProperty(key);
    }

}
