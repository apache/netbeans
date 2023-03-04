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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import org.netbeans.api.db.explorer.node.NodeProvider;
import org.netbeans.api.db.explorer.node.NodeProviderFactory;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.metadata.model.api.Action;
import org.netbeans.modules.db.metadata.model.api.Catalog;
import org.netbeans.modules.db.metadata.model.api.Metadata;
import org.netbeans.modules.db.metadata.model.api.MetadataElementHandle;
import org.netbeans.modules.db.metadata.model.api.MetadataModel;
import org.netbeans.modules.db.metadata.model.api.MetadataModelException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 *
 * @author Rob Englander
 */
public class CatalogNodeProvider extends NodeProvider implements PropertyChangeListener {

    // lazy initialization holder class idiom for static fields is used
    // for retrieving the factory
    public static NodeProviderFactory getFactory() {
        return FactoryHolder.FACTORY;
    }

    private static class FactoryHolder {
        static final NodeProviderFactory FACTORY = new NodeProviderFactory() {
            @Override
            public CatalogNodeProvider createInstance(Lookup lookup) {
                CatalogNodeProvider provider = new CatalogNodeProvider(lookup);
                return provider;
            }
        };
    }
    private final List<Node> nodes = new ArrayList<>();
    private final DatabaseConnection connection;

    private CatalogNodeProvider(Lookup lookup) {
        super(lookup, catalogComparator);
        connection = getLookup().lookup(DatabaseConnection.class);
    }

    @Override
    protected synchronized void initialize() {
        final List<Node> newList = new ArrayList<>();
        final List<Node> otherList = new ArrayList<>();

        MetadataModel metaDataModel = connection.getMetadataModel();
        boolean isConnected = connection.isConnected();

        nodes.clear();

        if (isConnected && metaDataModel != null) {
            try {
                metaDataModel.runReadAction(
                        new Action<Metadata>() {
                    @Override
                    public void run(Metadata metaData) {
                        Collection<Catalog> catalogs = metaData.getCatalogs();

                        for (Catalog catalog : catalogs) {
                            boolean oneCatalog = catalogs.size() == 1;
                            if (catalog.getName() != null || oneCatalog) {
                                if (isDefaultCatalog(catalog, connection)) {
                                    updateNode(newList, catalog);
                                } else {
                                    updateNode(otherList, catalog);
                                }
                            }
                        }

                        nodes.addAll(newList);
                        nodes.addAll(otherList);

                        if (!otherList.isEmpty()) {
                            newList.add(new CatalogNodeProvider.OtherCatalogsNode(
                                    otherList));
                        }

                        if (newList.size() == 1) {
                            setProxyNodes(newList);
                        } else {
                            setNodes(newList);
                        }
                    }
                });
            } catch (MetadataModelException e) {
                NodeRegistry.handleMetadataModelException(this.getClass(), connection, e, true);
            }
        } else if (!isConnected) {
           setNodes(newList);
        }
        connection.addPropertyChangeListener(WeakListeners.propertyChange(this, connection));
    }

    private boolean isDefaultCatalog(Catalog catalog,
            DatabaseConnection connection) {

        String def = connection.getDefaultCatalog();
        return (def == null && catalog.isDefault())
                || (def != null && def.equals(catalog.getName())
                || connection.isImportantCatalog(catalog.getName()));
    }

    private void updateNode(List<Node> newList, Catalog catalog) {
        MetadataElementHandle<Catalog> catalogHandle = MetadataElementHandle.create(catalog);
        Collection<Node> matches = getNodes(catalogHandle);
        if (matches != null && matches.size() > 0) {
            newList.addAll(matches);
        } else {
            NodeDataLookup lookup = new NodeDataLookup();
            lookup.add(connection);
            lookup.add(catalogHandle);

            newList.add(CatalogNode.create(lookup, this));
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        if (this.initialized && "importantCatalogs".equals(pce.getPropertyName())) { //NOI18N
            final List<Node> mainList = new ArrayList<>();
            final List<Node> otherList = new ArrayList<>();

            for (Node node : new ArrayList<>(nodes)) {
                if (connection.isImportantCatalog(node.getName())) {
                    mainList.add(node.cloneNode());
                } else {
                    otherList.add(node.cloneNode());
                }
            }

            if (!otherList.isEmpty()) {
                mainList.add(new OtherCatalogsNode(otherList));
            }

            setNodes(mainList);
        }
    }

    private static final Comparator<Node> catalogComparator = new Comparator<Node>() {

        @Override
        public int compare(Node node1, Node node2) {
            if (node1 instanceof OtherCatalogsNode) {
                return 1;
            }
            if (node2 instanceof OtherCatalogsNode) {
                return -1;
            }
            return node1.getDisplayName().compareToIgnoreCase(node2.getDisplayName());
        }
    };

    @NbBundle.Messages({
        "LBL_OtherDatabases=Other databases"
    })
    private static class OtherCatalogsNode extends AbstractNode {

        private static final String ICON_BASE =
                "org/netbeans/modules/db/resources/database.gif";       //NOI18N

        public OtherCatalogsNode(List<Node> otherList) {
            super(createChildren(otherList));
            setDisplayName(Bundle.LBL_OtherDatabases());
            setIconBaseWithExtension(ICON_BASE);
        }

        private static Children createChildren(final List<Node> otherList) {
            Children c = Children.create(new ChildFactory<Node>() {
                @Override
                protected boolean createKeys(final List<Node> toPopulate) {
                    toPopulate.addAll(otherList);
                    return true;
                }

                @Override
                protected Node createNodeForKey(Node key) {
                    return key;
                }
            }, false);
            return c;
        }
    }
}
