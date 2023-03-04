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
import org.netbeans.modules.db.metadata.model.api.Schema;
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
public class SchemaNodeProvider extends NodeProvider implements PropertyChangeListener {

    // lazy initialization holder class idiom for static fields is used
    // for retrieving the factory
    public static NodeProviderFactory getFactory() {
        return FactoryHolder.FACTORY;
    }

    private static class FactoryHolder {
        static final NodeProviderFactory FACTORY = new NodeProviderFactory() {
            @Override
            public SchemaNodeProvider createInstance(Lookup lookup) {
                SchemaNodeProvider provider = new SchemaNodeProvider(lookup);
                return provider;
            }
        };
    }

    private final List<Node> nodes = new ArrayList<>();
    private final DatabaseConnection connection;
    private final MetadataElementHandle<Catalog> catalogHandle;

    private SchemaNodeProvider(Lookup lookup) {
        super(lookup, schemaComparator);
        connection = getLookup().lookup(DatabaseConnection.class);
        catalogHandle = getLookup().lookup(MetadataElementHandle.class);
    }

    @Override
    protected synchronized void initialize() {
        final List<Node> newList = new ArrayList<>();
        final List<Node> otherList = new ArrayList<>();

        boolean connected = connection.isConnected();
        MetadataModel metaDataModel = connection.getMetadataModel();

        nodes.clear();

        if (connected && metaDataModel != null) {
            try {
                metaDataModel.runReadAction(
                    new Action<Metadata>() {
                        @Override
                        public void run(Metadata metaData) {
                            Catalog cat = catalogHandle.resolve(metaData);
                            if (cat != null) {
                                Schema syntheticSchema = cat.getSyntheticSchema();

                                if (syntheticSchema != null) {
                                    updateNode(newList, syntheticSchema);
                                } else {
                                    Collection<Schema> schemas = cat.getSchemas();
                                    for (Schema schema : schemas) {
                                        if (isDefaultSchema(schema, connection)) {
                                            updateNode(newList, schema);
                                        } else {
                                            updateNode(otherList, schema);
                                        }
                                    }
                                    nodes.addAll(newList);
                                    nodes.addAll(otherList);

                                    if (!otherList.isEmpty()) {
                                        newList.add(new OtherSchemasNode(
                                                otherList));
                                    }
                                }

                                if (syntheticSchema != null) {
                                    setProxyNodes(newList);
                                } else {
                                    setNodes(newList);
                                }
                            }
                        }
                    }
                );
            } catch (MetadataModelException e) {
                NodeRegistry.handleMetadataModelException(this.getClass(), connection, e, true);
            }
        } else {
            setNodes(newList);
        }
        connection.addPropertyChangeListener(WeakListeners.propertyChange(this, connection));
    }

    private boolean isDefaultSchema(Schema schema,
            DatabaseConnection connection) {

        String def = connection.getDefaultSchema();
        return (def == null && schema.isDefault())
                || (def != null && def.equals(schema.getName())
                || connection.isImportantSchema(schema.getName()));
    }

    private void updateNode(List<Node> newList, Schema schema) {
        MetadataElementHandle<Schema> schemaHandle = MetadataElementHandle.create(schema);
        Collection<Node> matches = getNodes(schemaHandle);
        if (matches != null && matches.size() > 0) {
            newList.addAll(matches);
        } else {
            NodeDataLookup lookup = new NodeDataLookup();
            lookup.add(connection);
            lookup.add(schemaHandle);

            newList.add(SchemaNode.create(lookup, this));
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        if (this.initialized && "importantSchemas".equals(pce.getPropertyName())) {
            final List<Node> mainList = new ArrayList<>();
            final List<Node> otherList = new ArrayList<>();

            for(Node node: new ArrayList<>(nodes)) {
                if(connection.isImportantSchema(node.getName())) {
                    mainList.add(node.cloneNode());
                } else {
                    otherList.add(node.cloneNode());
                }
            }
            if(! otherList.isEmpty()) {
                mainList.add(new OtherSchemasNode(otherList));
            }
            setNodes(mainList);
        }
    }

    private static final Comparator<Node> schemaComparator = new Comparator<Node>() {

        @Override
        public int compare(Node node1, Node node2) {
            assert node1.getDisplayName() != null : node1 + " has display name.";
            assert node2.getDisplayName() != null : node2 + " has display name.";
            if(node1 instanceof OtherSchemasNode) {
                return 1;
            }
            if(node2 instanceof OtherSchemasNode) {
                return -1;
            }
            return node1.getDisplayName().compareToIgnoreCase(node2.getDisplayName());
        }
    };

    @NbBundle.Messages({
        "LBL_OtherSchemas=Other schemas"
    })
    private static class OtherSchemasNode extends AbstractNode {

        private static final String ICON_BASE =
                "org/netbeans/modules/db/resources/schema.png";         //NOI18N

        public OtherSchemasNode(List<Node> otherList) {
            super(createChildren(otherList));
            setDisplayName(Bundle.LBL_OtherSchemas());
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
