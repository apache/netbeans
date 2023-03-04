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
import org.netbeans.modules.db.metadata.model.api.Metadata;
import org.netbeans.modules.db.metadata.model.api.MetadataElementHandle;
import org.netbeans.modules.db.metadata.model.api.MetadataModel;
import org.netbeans.modules.db.metadata.model.api.MetadataModelException;
import org.netbeans.modules.db.metadata.model.api.Schema;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 *
 * @author Rob Englander
 */
public class SchemaNode extends BaseNode implements PropertyChangeListener {
    private static final String ICONBASE = "org/netbeans/modules/db/resources/schema.png"; // NOI18N
    private static final String FOLDER = "Schema"; //NOI18N

    /**
     * Create an instance of SchemaNode.
     *
     * @param dataLookup the lookup to use when creating node providers
     * @return the SchemaNode instance
     */
    public static SchemaNode create(NodeDataLookup dataLookup, NodeProvider provider) {
        SchemaNode node = new SchemaNode(dataLookup, provider);
        node.setup();
        return node;
    }

    private String name = ""; // NOI18N
    private String htmlName = null;

    private final MetadataElementHandle<Schema> schemaHandle;
    private final DatabaseConnection connection;
    private final ToggleImportantInfo toggleImportantInfo =
            new ToggleImportantInfo(Schema.class);

    @SuppressWarnings("unchecked")
    private SchemaNode(NodeDataLookup lookup, NodeProvider provider) {
        super(new ChildNodeFactory(lookup), lookup, FOLDER, provider);
        connection = getLookup().lookup(DatabaseConnection.class);
        schemaHandle = getLookup().lookup(MetadataElementHandle.class);
        lookup.add(toggleImportantInfo);
    }

    @Override
    protected void initialize() {
        setupNames();

        connection.addPropertyChangeListener(WeakListeners.propertyChange(this, connection));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(DatabaseConnection.PROP_DEFSCHEMA)) {
            updateProperties();
        }
    }

    private void setupNames() {
        boolean connected = connection.isConnected();
        final MetadataModel metaDataModel = connection.getMetadataModel();
        if (connected && metaDataModel != null) {
            new SwingWorker() {
                @Override
                protected Object doInBackground() throws Exception {
                    try {
                        metaDataModel.runReadAction(
                                new Action<Metadata>() {
                            @Override
                            public void run(Metadata metaData) {
                                Schema schema = schemaHandle.resolve(metaData);
                                toggleImportantInfo.setDefault(schema.isDefault());
                                toggleImportantInfo.setImportant(connection.isImportantSchema(name));
                                renderNames(schema);
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
        setupNames();
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

    private void renderNames(Schema schema) {
        if (schema == null) {
            name = "";
        } else {
            name = schema.getName();
        }
        if (name == null) {
            name = schema.getParent().getName();
        }
        if (name == null) {
            name = "";
        }

        if (schema != null) {
            boolean isDefault;
            String def = connection.getDefaultSchema();
            if (def != null) {
                isDefault = def.equals(name);
            } else {
                isDefault = schema.isDefault();
            }

            if (isDefault) {
                htmlName = "<b>" + name + "</b>"; // NOI18N
            } else {
                htmlName = null;
            }
        }
    }

    @Override
    public String getHtmlDisplayName() {
        return htmlName;
    }

    @Override
    public String getIconBase() {
        return ICONBASE;
    }

    @Override
    public String getShortDescription() {
        return NbBundle.getMessage (SchemaNode.class, "ND_Schema"); //NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(SchemaNode.class);
    }
}
