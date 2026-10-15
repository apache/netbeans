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

package org.netbeans.modules.server.ui.node;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.core.ide.ServicesTabNodeRegistration;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.server.ServerRegistry;
import org.netbeans.spi.server.ServerInstanceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;

public final class RootNode extends AbstractNode {

    private static final RequestProcessor REFRESH_PROCESSOR =
            new RequestProcessor("Server registry node update/refresh", 5);

    private static final String SERVERS_ICON = "org/netbeans/modules/server/ui/resources/servers.png"; // NOI18N
    private static final String CLOUD_ICON = "org/netbeans/modules/server/ui/resources/cloud.png"; // NOI18N

    private static final Logger LOGGER = Logger.getLogger(RootNode.class.getName());

    private static RootNode node;
    private static RootNode cloudNode;

    private ServerRegistry registry;

    private RootNode(ChildFactory factory, String displayName, String shortDesc, String iconBase, ServerRegistry registry) {
        super(Children.create(factory, true));
        this.registry = registry;

        setName(""); // NOI18N
        setDisplayName(displayName);
        setShortDescription(shortDesc);
        setIconBaseWithExtension(iconBase);
    }

    @ServicesTabNodeRegistration(
        name = "servers",
        displayName = "org.netbeans.modules.server.ui.node.Bundle#Server_Registry_Node_Name",
        shortDescription = "org.netbeans.modules.server.ui.node.Bundle#Server_Registry_Node_Short_Description",
        iconResource = "org/netbeans/modules/server/ui/resources/servers.png",
        position = 400
    )
    public static synchronized RootNode getInstance() {
        if (node == null) {
            ChildFactory factory = new ChildFactory(ServerRegistry.getInstance());
            factory.init();

            node = new RootNode(factory, 
                    NbBundle.getMessage(RootNode.class, "Server_Registry_Node_Name"),
                    NbBundle.getMessage(RootNode.class, "Server_Registry_Node_Short_Description"),
                    SERVERS_ICON,
                    ServerRegistry.getInstance());
        }
        return node;
    }

    @ServicesTabNodeRegistration(
        name = "cloud",
        displayName = "org.netbeans.modules.server.ui.node.Bundle#Cloud_Registry_Node_Name",
        shortDescription = "org.netbeans.modules.server.ui.node.Bundle#Cloud_Registry_Node_Short_Description",
        iconResource = "org/netbeans/modules/server/ui/resources/cloud.png",
        position = 444
    )
    public static synchronized RootNode getCloudInstance() {
        if (cloudNode == null) {
            ChildFactory factory = new ChildFactory(ServerRegistry.getCloudInstance());
            factory.init();

            cloudNode = new RootNode(factory,
                    NbBundle.getMessage(RootNode.class, "Cloud_Registry_Node_Name"),
                    NbBundle.getMessage(RootNode.class, "Cloud_Registry_Node_Short_Description"),
                    CLOUD_ICON,
                    ServerRegistry.getCloudInstance());
        }
        return cloudNode;
    }

    @Override
    public Action[] getActions(boolean context) {
        Action[] arr = Utilities.actionsForPath(registry.getPath()+"/Actions").toArray(new Action[0]); // NOI18N
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == null) {
                continue;
            }
            if (Boolean.TRUE.equals(arr[i].getValue("serverNodeHidden"))) { // NOI18N
                arr[i] = null;
            }
        }
        return arr;
    }


    static void enableActionsOnExpand(ServerRegistry registry) {
        FileObject fo = FileUtil.getConfigFile(registry.getPath()+"/Actions"); // NOI18N
        Enumeration<String> en;
        if (fo != null) {
            for (FileObject o : fo.getChildren()) {
                en = o.getAttributes();
                while (en.hasMoreElements()) {
                    String attr = en.nextElement();
                    boolean enable = false;
                    final String prefix = "property-"; // NOI18N
                    if (attr.startsWith(prefix)) {
                        attr = attr.substring(prefix.length());
                        if (System.getProperty(attr) != null) {
                            enable = true;
                        }
                    } else {
                        final String config = "config-"; // NOI18N
                        if (attr.startsWith(config)) {
                            attr = attr.substring(config.length());
                            FileObject configFile = FileUtil.getConfigFile(attr);
                            if (configFile != null) {
                                if (!configFile.isFolder() || configFile.getChildren().length > 0) {
                                    enable = true;
                                }
                            }
                        }
                    }

                    if (enable) {
                        Lookup l = Lookups.forPath(registry.getPath()+"/Actions"); // NOI18N
                        for (Lookup.Item<Action> item : l.lookupResult(Action.class).allItems()) {
                            if (item.getId().contains(o.getName())) {
                                Action a = item.getInstance();
                                a.actionPerformed(new ActionEvent(getInstance(), 0, "noui")); // NOI18N
                            }
                        }
                    }
                }
            }
        }
    }

    private static class ChildFactory extends org.openide.nodes.ChildFactory<ServerInstance>
            implements ChangeListener, Runnable {

        private static final Comparator<ServerInstance> COMPARATOR = new InstanceComparator();

        /** <i>GuardedBy("this")</i> */
        private final List<ServerInstanceProvider> types = new ArrayList<ServerInstanceProvider>();

        private final ServerRegistry registry;
        
        public ChildFactory(ServerRegistry registry) {
            super();
            this.registry = registry;
        }

        public void init() {
            REFRESH_PROCESSOR.post(new Runnable() {

                public void run() {
                    synchronized (ChildFactory.this) {
                        registry.addChangeListener(
                            WeakListeners.create(ChangeListener.class, ChildFactory.this, registry));
                        updateState(new ChangeEvent(registry));
                    }
                }
            });
        }

        public void stateChanged(final ChangeEvent e) {
            REFRESH_PROCESSOR.post(new Runnable() {

                public void run() {
                    updateState(e);
                }
            });
        }

        private synchronized void updateState(final ChangeEvent e) {
            if (e.getSource() instanceof ServerRegistry) {
                for (ServerInstanceProvider type : types) {
                    type.removeChangeListener(ChildFactory.this);
                }

                types.clear();
                types.addAll(((ServerRegistry) e.getSource()).getProviders());
                for (ServerInstanceProvider type : types) {
                    type.addChangeListener(ChildFactory.this);
                }
            }
            refresh();
        }

        protected final void refresh() {
            refresh(false);
        }

        @Override
        protected Node createNodeForKey(ServerInstance key) {
            return key.getFullNode();
        }

        @Override
        protected boolean createKeys(List<ServerInstance> toPopulate) {
            List<ServerInstance> fresh = new ArrayList<ServerInstance>();

            Mutex.EVENT.readAccess(this);

            for (ServerInstanceProvider type : registry.getProviders()) {
                List<ServerInstance> instances = type.getInstances();
                // #194962
                for (ServerInstance instance : instances) {
                    assert instance != null : "ServerInstance returned by provider " + type + " is null";
                    if (instance != null) {
                        fresh.add(instance);
                    }
                }
            }

            fresh.sort(COMPARATOR);

            toPopulate.addAll(fresh);
            return true;
        }

        private static boolean actionsPropertiesDone;
        public void run() {
            if (actionsPropertiesDone) {
                return;
            }
            assert EventQueue.isDispatchThread();
            actionsPropertiesDone = true;
            enableActionsOnExpand(registry);
            REFRESH_PROCESSOR.post(new Runnable() {

                @Override
                public void run() {
                    registry.getProviders();
                }
            });
        }
    } // end of ChildFactory

    private static class InstanceComparator implements Comparator<ServerInstance>, Serializable {

        public int compare(ServerInstance o1, ServerInstance o2) {
            boolean firstNull = false;
            boolean secondNull = false;

            if (o1.getDisplayName() == null) {
                LOGGER.log(Level.INFO, "Instance display name is null for {0}", o1);
                firstNull = true;
            }
            if (o2.getDisplayName() == null) {
                LOGGER.log(Level.INFO, "Instance display name is null for {0}", o2);
                secondNull = true;
            }

            if (firstNull && secondNull) {
                return 0;
            } else if (firstNull && !secondNull) {
                return -1;
            } else if (!firstNull && secondNull) {
                return 1;
            }

            return o1.getDisplayName().compareTo(o2.getDisplayName());
        }

    }
}
