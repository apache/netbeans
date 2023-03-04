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
package org.netbeans.modules.websvc.core.jaxws.nodes;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import java.beans.PropertyChangeListener;
import org.openide.util.RequestProcessor;

public class JaxWsClientRootChildren extends Children.Keys<Client> {
    
    private static final RequestProcessor JAX_WS_CLIENT_ROOT_RP =
            new RequestProcessor(JaxWsClientRootChildren.class); //NOI18N
    
    JaxWsModel jaxWsModel;
    Client[] clients;
    JaxWsListener listener;
    FileObject srcRoot;
    
    private final RequestProcessor.Task updateNodeTask = JAX_WS_CLIENT_ROOT_RP.create(new Runnable() {
        public void run() {
            updateKeys();
        }
    });
    
    public JaxWsClientRootChildren(JaxWsModel jaxWsModel, FileObject srcRoot) {
        this.jaxWsModel = jaxWsModel;
        this.srcRoot=srcRoot;
    }
    
    @Override
    protected void addNotify() {
        listener = new JaxWsListener();
        jaxWsModel.addPropertyChangeListener(listener);
        updateKeys();
    }
    
    @Override
    protected void removeNotify() {
        setKeys(Collections.<Client>emptySet());
        jaxWsModel.removePropertyChangeListener(listener);
    }
       
    private void updateKeys() {
        List<Client> keys = new ArrayList<Client>();
        clients = jaxWsModel.getClients();
        if (clients != null) {
            for (int i = 0; i < clients.length; i++) {
                keys.add(clients[i]);
            }
        }
        setKeys(keys);
    }

    protected Node[] createNodes(Client key) {
        return new Node[] {new JaxWsClientNode(jaxWsModel, key, srcRoot)};
    }
    
    class JaxWsListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            updateNodeTask.schedule(2000);
        }        
    }

}
