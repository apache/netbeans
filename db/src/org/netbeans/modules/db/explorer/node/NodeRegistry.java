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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
