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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.node.BaseNode;
import org.netbeans.api.db.explorer.node.ChildNodeFactory;
import org.netbeans.api.db.explorer.node.NodeProvider;
import org.netbeans.lib.ddl.DDLException;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.DatabaseConnector;
import org.netbeans.modules.db.metadata.model.api.Action;
import org.netbeans.modules.db.metadata.model.api.Index;
import org.netbeans.modules.db.metadata.model.api.Metadata;
import org.netbeans.modules.db.metadata.model.api.MetadataElementHandle;
import org.netbeans.modules.db.metadata.model.api.MetadataModel;
import org.netbeans.modules.db.metadata.model.api.MetadataModelException;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.PropertySupport;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Rob Englander
 */
public class IndexNode extends BaseNode {
    private static final String ICONBASE = "org/netbeans/modules/db/resources/index.gif";
    private static final String FOLDER = "Index"; //NOI18N

    /**
     * Create an instance of IndexNode.
     *
     * @param dataLookup the lookup to use when creating node providers
     * @return the IndexNode instance
     */
    public static IndexNode create(NodeDataLookup dataLookup, NodeProvider provider) {
        IndexNode node = new IndexNode(dataLookup, provider);
        node.setup();
        return node;
    }

    private String name = ""; // NOI18N
    private final MetadataElementHandle<Index> indexHandle;
    private final DatabaseConnection connection;

    @SuppressWarnings("unchecked")
    private IndexNode(NodeDataLookup lookup, NodeProvider provider) {
        super(new ChildNodeFactory(lookup), lookup, FOLDER, provider);
        connection = getLookup().lookup(DatabaseConnection.class);
        indexHandle = getLookup().lookup(MetadataElementHandle.class);
    }

    protected void initialize() {
        boolean connected = connection.isConnected();
        MetadataModel metaDataModel = connection.getMetadataModel();
        if (connected && metaDataModel != null) {
            try {
                metaDataModel.runReadAction(
                    new Action<Metadata>() {
                        public void run(Metadata metaData) {
                            Index index = indexHandle.resolve(metaData);
                            if(index.getName() != null) {
                                name = index.getName();
                            }
                            updateProperties(index);
                        }
                    }
                );
            } catch (MetadataModelException e) {
                NodeRegistry.handleMetadataModelException(this.getClass(), connection, e, true);
            }
        }
    }

    private void updateProperties(Index index) {
        PropertySupport.Name ps = new PropertySupport.Name(this);
        addProperty(ps);

        addProperty(UNIQUE, UNIQUEDESC, Boolean.class, false, index.isUnique());
    }

    public String getCatalogName() {
        return getCatalogName(connection, indexHandle);
    }

    public String getSchemaName() {
        return getSchemaName(connection, indexHandle);
    }

    public String getTableName() {
        return getTableName(connection, indexHandle);
    }

    @Override
    public String getShortDescription() {
        return NbBundle.getMessage (IndexNode.class, "ND_Index"); //NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(IndexNode.class);
    }

    @Override
    public void destroy() {
        DatabaseConnector connector = connection.getConnector();

        final String tablename = getTableName();
        String schemaName = getSchemaName();
        String catalogName = getCatalogName();

        if (schemaName == null) {
            schemaName = catalogName;
        }

        try {
            Specification spec = connector.getDatabaseSpecification();
            DDLHelper.deleteIndex(spec, schemaName, tablename, getName());
            setValue(BaseFilterNode.REFRESH_ANCESTOR_DISTANCE, Integer.valueOf(2));
        } catch (DDLException e) {
            Logger.getLogger(IndexNode.class.getName()).log(Level.INFO, e + " while deleting index " + getName());
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(e.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
    }

    @Override
    public boolean canDestroy() {
        DatabaseConnector connector = connection.getConnector();
        return connector.supportsCommand(Specification.DROP_INDEX);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return getName();
    }

    @Override
    public String getIconBase() {
        return ICONBASE;
    }

    public static String getTableName(DatabaseConnection connection, final MetadataElementHandle<Index> handle) {
        MetadataModel metaDataModel = connection.getMetadataModel();
        final String[] array = { null };

        try {
            metaDataModel.runReadAction(
                new Action<Metadata>() {
                    public void run(Metadata metaData) {
                        Index index = handle.resolve(metaData);
                        if (index != null) {
                            array[0] = index.getParent().getName();
                        }
                    }
                }
            );
        } catch (MetadataModelException e) {
            // TODO report exception
        }

        return array[0];
    }

    public static String getSchemaName(DatabaseConnection connection, final MetadataElementHandle<Index> handle) {
        MetadataModel metaDataModel = connection.getMetadataModel();
        final String[] array = new String[1];

        try {
            metaDataModel.runReadAction(
                new Action<Metadata>() {
                    public void run(Metadata metaData) {
                        Index index = handle.resolve(metaData);
                        if (index != null) {
                            array[0] = index.getParent().getParent().getName();
                        }
                    }
                }
            );
        } catch (MetadataModelException e) {
            // TODO report exception
        }

        return array[0];
    }

    public static String getCatalogName(DatabaseConnection connection, final MetadataElementHandle<Index> handle) {
        MetadataModel metaDataModel = connection.getMetadataModel();
        final String[] array = new String[1];

        try {
            metaDataModel.runReadAction(
                new Action<Metadata>() {
                    public void run(Metadata metaData) {
                    Index index = handle.resolve(metaData);
                    if (index != null) {
                        array[0] = index.getParent().getParent().getParent().getName();
                    }
                    }
                }
            );
        } catch (MetadataModelException e) {
            // TODO report exception
        }

        return array[0];
    }
}
