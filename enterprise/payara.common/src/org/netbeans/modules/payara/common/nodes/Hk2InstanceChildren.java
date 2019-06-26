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

package org.netbeans.modules.payara.common.nodes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.payara.common.PayaraInstance;
import org.netbeans.modules.payara.spi.PayaraModule.ServerState;
import org.netbeans.modules.payara.spi.PluggableNodeProvider;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;


/**
 * 
 * @author Peter Williams
 */
public class Hk2InstanceChildren extends Children.Keys<Node> implements Refreshable, ChangeListener {
    
    private PayaraInstance serverInstance;
    
    @SuppressWarnings("LeakingThisInConstructor")
    Hk2InstanceChildren(PayaraInstance instance) {
        serverInstance = instance;
        serverInstance.getCommonSupport().addChangeListener(
                WeakListeners.change(this, serverInstance));
    }

    @Override
    public void updateKeys(){
        List<Node> keys = new LinkedList<Node>();
        serverInstance.getCommonSupport().refresh();
        if(serverInstance.getServerState() == ServerState.RUNNING) {
            keys.add(new Hk2ItemNode(serverInstance.getLookup(), 
                    new Hk2ApplicationsChildren(serverInstance.getLookup()),
                    NbBundle.getMessage(Hk2InstanceNode.class, "LBL_Apps"),
                    Hk2ItemNode.J2EE_APPLICATION_FOLDER));
            keys.add(new Hk2ItemNode(serverInstance.getLookup(), 
                    new Hk2ResourceContainers(serverInstance.getLookup()),
                    NbBundle.getMessage(Hk2InstanceNode.class, "LBL_Resources"),
                    Hk2ItemNode.RESOURCES_FOLDER));
            String iid = serverInstance.getDeployerUri();
            if (null != iid && iid.contains("pfv3ee6wc")) {
                keys.add(new Hk2ItemNode(serverInstance.getLookup(),
                        new Hk2WSChildren(serverInstance.getLookup()),
                        NbBundle.getMessage(Hk2InstanceNode.class, "LBL_WS"),
                        Hk2ItemNode.WS_FOLDER));
            }
            List<Node> pluggableNodes = getExtensionNodes();
            for (Iterator itr = pluggableNodes.iterator(); itr.hasNext();) {
                keys.add((Node)itr.next());
            }
        }
        setKeys(keys);
    }
    
    @Override
    protected void addNotify() {
        updateKeys();
    }
    
    @Override
    protected void removeNotify() {
        Collection<Node> noKeys = java.util.Collections.emptySet();
        setKeys(noKeys);
    }
    
    @Override
    protected org.openide.nodes.Node[] createNodes(Node key) {
        return new Node [] { key };
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                updateKeys();
            }
        });
    }

    List<Node> getExtensionNodes() {
       List<Node> nodesList = new ArrayList<Node>();
        for (PluggableNodeProvider nep
                : Lookup.getDefault().lookupAll(PluggableNodeProvider.class)) {
            if (nep != null) {
                try {
                    Node node = nep.getPluggableNode(
                            serverInstance.getProperties());
                    if (node != null) {
                        nodesList.add(node);
                    }
                } catch (Exception ex) {
                    Logger.getLogger("payara-common").log(Level.SEVERE,
                            NbBundle.getMessage(Hk2InstanceChildren.class,
                            "WARN_BOGUS_GET_EXTENSION_NODE_IMPL", // NOI18N
                            nep.getClass().getName()));
                    Logger.getLogger("payara-common").log(Level.FINER,
                            NbBundle.getMessage(Hk2InstanceChildren.class,
                            "WARN_BOGUS_GET_EXTENSION_NODE_IMPL", // NOI18N
                            nep.getClass().getName()), ex);
                } catch (AssertionError ae) {
                    Logger.getLogger("payara-common").log(Level.SEVERE,
                            NbBundle.getMessage(Hk2InstanceChildren.class,
                            "WARN_BOGUS_GET_EXTENSION_NODE_IMPL", // NOI18N
                            nep.getClass().getName()+".")); // NOI18N
                    Logger.getLogger("payara-common").log(Level.FINER,
                            NbBundle.getMessage(Hk2InstanceChildren.class,
                            "WARN_BOGUS_GET_EXTENSION_NODE_IMPL", // NOI18N
                            nep.getClass().getName()), ae);
                }
             }
        }
       return nodesList;
   }
}
