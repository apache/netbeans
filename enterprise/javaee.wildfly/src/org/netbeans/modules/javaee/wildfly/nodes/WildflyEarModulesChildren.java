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

package org.netbeans.modules.javaee.wildfly.nodes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.javaee.wildfly.WildflyDeploymentManager;
import org.netbeans.modules.javaee.wildfly.nodes.actions.Refreshable;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 * It describes children nodes of the enterprise application node.
 *
 * @author Michal Mocnak
 * @author Emmanuel Hugonnet (ehsavoie) <ehsavoie@netbeans.org>
 */
public class WildflyEarModulesChildren extends WildflyAsyncChildren implements Refreshable  {

    private static final Logger LOGGER = Logger.getLogger(WildflyEarApplicationsChildren.class.getName());

    private final Lookup lookup;
    private final String j2eeAppName;

    public WildflyEarModulesChildren(Lookup lookup, String j2eeAppName) {
        this.lookup = lookup;
        this.j2eeAppName = j2eeAppName;
    }

    @Override
    public void updateKeys(){
        setKeys(new Object[]{Util.WAIT_NODE});
        getExecutorService().submit(new WildflyEarModulesNodeUpdater(), 0);
    }

    class WildflyEarModulesNodeUpdater implements Runnable {

        List keys = new ArrayList();

        @Override
        public void run() {
            try {
                WildflyDeploymentManager dm = lookup.lookup(WildflyDeploymentManager.class);
                keys.addAll(dm.getClient().listEarSubModules(lookup, j2eeAppName));
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
        if (key instanceof WildflyEjbModuleNode){
            return new Node[]{(WildflyEjbModuleNode)key};
        }

        if (key instanceof WildflyWebModuleNode){
            return new Node[]{(WildflyWebModuleNode)key};
        }

        if (key instanceof String && key.equals(Util.WAIT_NODE)){
            return new Node[]{Util.createWaitNode()};
        }

        return null;
    }
}
