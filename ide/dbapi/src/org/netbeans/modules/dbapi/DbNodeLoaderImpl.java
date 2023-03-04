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

package org.netbeans.modules.dbapi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.db.api.explorer.NodeProvider;
import org.netbeans.modules.db.explorer.DbNodeLoader;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;

/**
 * Loads nodes from all registered node providers and delivers them to
 * the caller of getAllNodes().
 *  
 * @author David Van Couvering
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.db.explorer.DbNodeLoader.class)
public class DbNodeLoaderImpl implements DbNodeLoader, ChangeListener {
    
            /** 
     * Not private because used in the tests.
     */
    static final String NODE_PROVIDER_PATH = "Databases/NodeProviders"; // NOI18N
    static Collection providers;
    
    final CopyOnWriteArrayList<ChangeListener> listeners = 
            new CopyOnWriteArrayList<ChangeListener>();
    
    public List<Node> getAllNodes() {
        List<Node> nodes = new ArrayList<Node>();
        
        if ( providers == null ) {
            providers = Lookups.forPath(NODE_PROVIDER_PATH).lookupAll(NodeProvider.class);    
        }
        
        for (Iterator<NodeProvider> i = providers.iterator(); i.hasNext();) {
            NodeProvider provider = i.next();
            List<Node> nodeList = provider.getNodes();
            if (nodeList != null) {
                nodes.addAll(nodeList);
            }
            
            provider.addChangeListener(this);
        }
        
        return nodes;
    }

    public void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        listeners.remove(listener);
    }

    public synchronized void stateChanged(ChangeEvent evt) {
        // At this point, any state change simply means that the consumer 
        // should re-call getAllNodes(), so delegate the state change up to 
        // the consumer.
        for ( ChangeListener listener : listeners ) {
            listener.stateChanged(new ChangeEvent(this));
        }
    }
}
