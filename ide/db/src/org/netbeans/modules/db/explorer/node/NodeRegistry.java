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

package org.netbeans.modules.db.explorer.node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.db.explorer.node.NodeProvider;
import org.netbeans.api.db.explorer.node.NodeProviderFactory;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.metadata.model.api.MetadataModelException;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;

/**
 * NodeRegistry contains all of the NodeProvider instances defined in a folder in
 * the xml layer.  The folder used for the lookup uses the path:
 * 
 *      Databases/Explorer/[folder]/NodeProviders
 * 
 *      where [folder] is the specific folder name associated with a given node type.
 * 
 * The providers are retrieved by lookup from the xml layer.  The NodeRegistry is used 
 * by the BaseNode instance to retrieve its list of child nodes.
 * 
 * @author Rob Englander
 */
public class NodeRegistry implements ChangeListener {
    private static final String PATH = "Databases/Explorer/"; //NOI18N
    private static final String NODEPROVIDERS = "/NodeProviders"; //NOI18N
    
    private final ChangeSupport changeSupport;
    private final List<NodeProvider> providers = new CopyOnWriteArrayList<NodeProvider>();

    private Lookup.Result lookupResult;
    private LookupListener lookupListener;
    
    /** 
     * Create an instance of NodeRegistry.
     * 
     * @param folder the name of the xml layer folder to use
     * @param dataLookup the lookup to use when creating node providers
     * @return the NodeRegistry instance
     */
    public static NodeRegistry create(String folder, NodeDataLookup dataLookup) {
        NodeRegistry registry = new NodeRegistry();
        registry.init(folder, dataLookup);
        return registry;
    }

    private NodeRegistry() {
        changeSupport = new ChangeSupport(this);
    }
    
    /**
     * Initialize the registry
     * @param folder the name of the xml layer folder to use
     * @param dataLookup the lookup to use when creating providers
     */
    private void init(String folder, final Lookup dataLookup) {
        Lookup lookup = Lookups.forPath(PATH + folder + NODEPROVIDERS);
        lookupResult = lookup.lookupResult(NodeProviderFactory.class);

        initProviders(dataLookup);
        
        // listen for changes and re-init the providers when the lookup changes
        lookupResult.addLookupListener(WeakListeners.create(LookupListener.class,
            lookupListener = new LookupListener() {
                @Override
                public void resultChanged(LookupEvent ev) {
                    initProviders(dataLookup);
                    changeSupport.fireChange();
                }
            },
            lookupResult)
        );
    }
    
    /**
     * Initialize the node providers
     * 
     * @param lookup the lookup to use when creating each provider
     */
    private void initProviders(Lookup lookup) {
        providers.clear();
        Collection<NodeProviderFactory> factoryList = lookupResult.allInstances();
        for (NodeProviderFactory factory : factoryList) {
            NodeProvider provider = factory.createInstance(lookup);
            provider.addChangeListener(this);
            providers.add(provider);
        }
    }

    public synchronized void refresh() {
        for (NodeProvider provider : providers) {
            provider.refresh();
        }
    }

    //#170935 - workaround
    public synchronized void removeAllNodes() {
        for (NodeProvider provider : providers) {
            provider.removeAllNodes();
        }
    }

    /**
     * Get the nodes from all of the registered providers.
     * 
     * @return the nodes
     */
    public synchronized Collection<? extends Node> getNodes() {
        List<Node> results = new ArrayList<Node>();

        for (NodeProvider provider : providers) {
            results.addAll(provider.getNodes());
        } 
        
        return Collections.unmodifiableCollection(results);
    }
    
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }
    
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public void stateChanged(ChangeEvent evt) {
        changeSupport.fireChange();
    }

    public static synchronized void handleMetadataModelException(Class<?> clazz, DatabaseConnection connection, MetadataModelException e, boolean closeConnectionIfBroken) {
        Logger.getLogger(clazz.getName()).log(Level.FINE, e.getLocalizedMessage(), e);
        if (connection == null) {
            return;
        }
        if (! connection.isVitalConnection()) {
            try {
                if ( connection.isConnected() && closeConnectionIfBroken) {
                    String msg = e.getCause().getLocalizedMessage();
                    if (msg.length() > 280) {
                        msg = msg.substring(0, 280) + NbBundle.getMessage(NodeRegistry.class, "NodeRegistry_CloseBrokenConnectionMore"); // NOI18N
                    }
                    DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(
                            NbBundle.getMessage(NodeRegistry.class, "NodeRegistry_CloseBrokenConnection", // NOI18N
                            connection.getName(),
                            msg)));
                    connection.disconnect();
                }
            } catch (DatabaseException ex) {
                Logger.getLogger(clazz.getName()).log(Level.INFO, "While disconnecting a broken connection: " + connection + " was thrown " + ex.getLocalizedMessage(), ex);
            }
        }
    }
}
