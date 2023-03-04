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
import javax.swing.SwingWorker;
import org.netbeans.api.db.explorer.node.BaseNode;
import org.netbeans.api.db.explorer.node.ChildNodeFactory;
import org.netbeans.api.db.explorer.node.NodeProvider;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.metadata.model.api.Action;
import org.netbeans.modules.db.metadata.model.api.Catalog;
import org.netbeans.modules.db.metadata.model.api.Metadata;
import org.netbeans.modules.db.metadata.model.api.MetadataElementHandle;
import org.netbeans.modules.db.metadata.model.api.MetadataModel;
import org.netbeans.modules.db.metadata.model.api.MetadataModelException;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 *
 * @author Rob Englander
 */
public class CatalogNode extends BaseNode implements PropertyChangeListener {
    private static final String ICONBASE = "org/netbeans/modules/db/resources/database.gif";
    private static final String FOLDER = "Catalog"; //NOI18N

    /**
     * Create an instance of CatalogNode.
     *
     * @param dataLookup the lookup to use when creating node providers
     * @return the CatalogNode instance
     */
    public static CatalogNode create(NodeDataLookup dataLookup, NodeProvider provider) {
        CatalogNode node = new CatalogNode(dataLookup, provider);
        node.setup();
        return node;
    }

    private String name = ""; // NOI18N
    private String htmlName = null;
    private final DatabaseConnection connection;
    private final MetadataElementHandle<Catalog> catalogHandle;
    private final ToggleImportantInfo toggleImportantInfo =
            new ToggleImportantInfo(Catalog.class);

    @SuppressWarnings("unchecked")
    private CatalogNode(NodeDataLookup lookup, NodeProvider provider) {
        super(new ChildNodeFactory(lookup), lookup, FOLDER, provider);
        connection = getLookup().lookup(DatabaseConnection.class);
        catalogHandle = getLookup().lookup(MetadataElementHandle.class);
        lookup.add(toggleImportantInfo);
    }

    @Override
    protected void initialize() {
        refreshMetaData();

        connection.addPropertyChangeListener(WeakListeners.propertyChange(this, connection));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(DatabaseConnection.PROP_DEFCATALOG)) {
            updateProperties();
        }
    }

    private void refreshMetaData() {
        final MetadataModel metaDataModel = connection.getMetadataModel();
        boolean connected = connection.isConnected();
        if (connected && metaDataModel != null) {
            new SwingWorker() {
                @Override
                protected Object doInBackground() throws Exception {
                    try {
                        metaDataModel.runReadAction(
                                new Action<Metadata>() {
                            @Override
                            public void run(Metadata metaData) {
                                Catalog catalog = catalogHandle.resolve(metaData);
                                toggleImportantInfo.setDefault(catalog.isDefault());
                                toggleImportantInfo.setImportant(connection.isImportantCatalog(name));
                                renderNames(catalog);
                            }
                        });
                        return null;
                    } catch (MetadataModelException e) {
                        NodeRegistry.handleMetadataModelException(this.getClass(), connection, e, true);
                        return null;
                    }
                }
            }.run();
        }
    }

    @Override
    protected void updateProperties() {
        refreshMetaData();
        super.updateProperties();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    @Override
    public String getHtmlDisplayName() {
        return htmlName;
    }

    private void renderNames(Catalog catalog) {
        if (catalog == null) {
            name = "";
        } else {
            name = catalog.getName();
            if (name == null) {
                name = "Default"; // NOI18N
            }
        }

        if (catalog != null) {
            boolean isDefault;
            String def = connection.getDefaultCatalog();
            if (def != null) {
                isDefault = def.equals(name);
            } else {
                isDefault = catalog.isDefault();
            }

            if (isDefault) {
                htmlName = "<b>" + name + "</b>"; // NOI18N
            } else {
                htmlName = null;
            }
        }
    }

    @Override
    public String getIconBase() {
        return ICONBASE;
    }

    @Override
    public String getShortDescription() {
        return NbBundle.getMessage (CatalogNode.class, "ND_Catalog"); //NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(CatalogNode.class);
    }
}
