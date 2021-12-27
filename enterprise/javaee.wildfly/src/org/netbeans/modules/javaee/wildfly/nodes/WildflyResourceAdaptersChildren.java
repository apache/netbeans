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
package org.netbeans.modules.javaee.wildfly.nodes;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.javaee.wildfly.WildflyDeploymentManager;
import org.netbeans.modules.javaee.wildfly.config.WildflyResourceAdapter;
import org.netbeans.modules.javaee.wildfly.nodes.actions.Refreshable;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * It describes children nodes of the EJB Modules node. Implements Refreshable
 * interface and due to it can be refreshed via ResreshModulesAction.
 *
 * @author Michal Mocnak
 */
public class WildflyResourceAdaptersChildren extends WildflyAsyncChildren implements Refreshable {

    private static final Logger LOGGER = Logger.getLogger(WildflyResourceAdaptersChildren.class.getName());

    private final Lookup lookup;

    public WildflyResourceAdaptersChildren(Lookup lookup) {
        this.lookup = lookup;
    }

    @Override
    public void updateKeys() {
        setKeys(new Object[]{Util.WAIT_NODE});
        getExecutorService().submit(new WildflyResourceAdaptersNodeUpdater(), 0);

    }

    class WildflyResourceAdaptersNodeUpdater implements Runnable {

        List<WildflyResourceAdapterNode> keys = new ArrayList<WildflyResourceAdapterNode>();

        @Override
        public void run() {
            try {
                WildflyDeploymentManager dm = lookup.lookup(WildflyDeploymentManager.class);
                for (WildflyResourceAdapter resourceAdapter : dm.getClient().listResourceAdapters()) {
                    keys.add(new WildflyResourceAdapterNode(resourceAdapter, lookup));
                }
            } catch (Exception ex) {
                LOGGER.log(Level.INFO, null, ex);
            }

            setKeys(keys);
        }
    }

    @Override
    protected void addNotify() {
        updateKeys();
    }

    @Override
    protected void removeNotify() {
        setKeys(java.util.Collections.EMPTY_SET);
    }

    @Override
    protected org.openide.nodes.Node[] createNodes(Object key) {
        if (key instanceof WildflyResourceAdapterNode) {
            return new Node[]{(WildflyResourceAdapterNode) key};
        }

        if (key instanceof String && key.equals(Util.WAIT_NODE)) {
            return new Node[]{Util.createWaitNode()};
        }
        return null;
    }

}
