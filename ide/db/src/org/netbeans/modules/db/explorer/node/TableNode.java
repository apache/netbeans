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

import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.DatabaseMetaDataTransfer;
import org.netbeans.api.db.explorer.node.BaseNode;
import org.netbeans.api.db.explorer.node.ChildNodeFactory;
import org.netbeans.api.db.explorer.node.NodeProvider;
import org.netbeans.lib.ddl.DDLException;
import org.netbeans.lib.ddl.impl.AbstractCommand;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.DatabaseConnector;
import org.netbeans.modules.db.explorer.DatabaseMetaDataTransferAccessor;
import org.netbeans.modules.db.metadata.model.api.Action;
import org.netbeans.modules.db.metadata.model.api.Metadata;
import org.netbeans.modules.db.metadata.model.api.MetadataElementHandle;
import org.netbeans.modules.db.metadata.model.api.MetadataModel;
import org.netbeans.modules.db.metadata.model.api.MetadataModelException;
import org.netbeans.modules.db.metadata.model.api.Table;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.ExTransferable;

/**
 *
 * @author Rob Englander
 */
public class TableNode extends BaseNode implements SchemaNameProvider {
    private static final String ICONBASE = "org/netbeans/modules/db/resources/table.gif"; // NOI18N
    private static final String FOLDER = "Table"; //NOI18N
    private static final String SYSTEM = "System"; //NOI18N
    private static final String SYSTEMDESC = "SystemDesc"; //NOI18N
    private static final Map<Node, Object> NODES_TO_REFRESH =
            new WeakHashMap<Node, Object>();

    /**
     * Create an instance of TableNode.
     *
     * @param dataLookup the lookup to use when creating node providers
     * @return the TableNode instance
     */
    public static TableNode create(NodeDataLookup dataLookup, NodeProvider provider) {
        TableNode node = new TableNode(dataLookup, provider);
        node.setup();
        return node;
    }

    private String name = ""; // NOI18N
    private boolean system = false;
    private final MetadataElementHandle<Table> tableHandle;
    private final DatabaseConnection connection;

    @SuppressWarnings("unchecked")
    private TableNode(NodeDataLookup lookup, NodeProvider provider) {
        super(new ChildNodeFactory(lookup), lookup, FOLDER, provider);
        connection = getLookup().lookup(DatabaseConnection.class);
        tableHandle = getLookup().lookup(MetadataElementHandle.class);
    }

    @Override
    protected void initialize() {
        boolean connected = connection.isConnected();
        MetadataModel metaDataModel = connection.getMetadataModel();
        if (connected && metaDataModel != null) {
            try {
                metaDataModel.runReadAction(
                    new Action<Metadata>() {
                    @Override
                        public void run(Metadata metaData) {
                            Table table = tableHandle.resolve(metaData);
                            if (table == null) {
                                Logger.getLogger(TableNode.class.getName()).log(Level.INFO, "Cannot get table name for " + tableHandle);
                                return ;
                            }
                            name = table.getName();
                            system = table.isSystem();
                            updateProperties(table);
                        }
                    }
                );
            } catch (MetadataModelException e) {
                NodeRegistry.handleMetadataModelException(this.getClass(), connection, e, true);
            }

        }
    }

    private void updateProperties(Table table) {
        PropertySupport.Name ps = new PropertySupport.Name(TableNode.this);
        addProperty(ps);

        addProperty(CATALOG, CATALOGDESC, String.class, false, getCatalogName());
        addProperty(SCHEMA, SCHEMADESC, String.class, false, getSchemaName());
        addProperty(SYSTEM, SYSTEMDESC, Boolean.class, false, isSystem());
    }

    public MetadataElementHandle<Table> getTableHandle() {
        return tableHandle;
    }

    @Override
    public String getCatalogName() {
        return getCatalogName(connection, tableHandle);
    }

    @Override
    public String getSchemaName() {
        return getSchemaName(connection, tableHandle);
    }

    @Override
    public void destroy() {
        DatabaseConnector connector = connection.getConnector();
        Specification spec = connector.getDatabaseSpecification();

        try {
            AbstractCommand command = spec.createCommandDropTable(getName());
            String schemaName = getSchemaName();
            String catalogName = getCatalogName();
            if (schemaName == null) {
                schemaName = catalogName;
            }

            command.setObjectOwner(schemaName);
            command.execute();
        } catch (DDLException e) {
            Logger.getLogger(TableNode.class.getName()).log(Level.INFO, e + " while deleting table " + getName());
            DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(e.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }

        setValue(BaseFilterNode.REFRESH_ANCESTOR_DISTANCE, Integer.valueOf(1));
    }

    @Override
    public boolean canDestroy() {
        DatabaseConnector connector = connection.getConnector();
        return (! system) && connector.supportsCommand(Specification.DROP_TABLE);
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

    public boolean isSystem() {
        return system;
    }

    @Override
    public String getShortDescription() {
        return NbBundle.getMessage (TableNode.class, "ND_Table"); //NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(TableNode.class);
    }

    @Override
    public boolean canCopy() {
        return true;
    }

    @Override
    public Transferable clipboardCopy() throws IOException {
        ExTransferable result = ExTransferable.create(super.clipboardCopy());
        result.put(new ExTransferable.Single(DatabaseMetaDataTransfer.TABLE_FLAVOR) {
            @Override
            protected Object getData() {
                return DatabaseMetaDataTransferAccessor.DEFAULT.createTableData(connection.getDatabaseConnection(),
                        connection.findJDBCDriver(), getName());
            }
        });
        return result;
    }

    public static String getSchemaName(DatabaseConnection connection, final MetadataElementHandle<Table> handle) {
        MetadataModel metaDataModel = connection.getMetadataModel();
        final String[] array = new String[1];

        try {
            metaDataModel.runReadAction(
                new Action<Metadata>() {
                @Override
                    public void run(Metadata metaData) {
                        Table table = handle.resolve(metaData);
                        if (table != null && table.getParent() != null) {
                            array[0] = table.getParent().getName();
                        }
                    }
                }
            );
        } catch (MetadataModelException e) {
            NodeRegistry.handleMetadataModelException(TableNode.class, connection, e, true);
        }

        return array[0];
    }

    public static String getCatalogName(DatabaseConnection connection, final MetadataElementHandle<Table> handle) {
        MetadataModel metaDataModel = connection.getMetadataModel();
        final String[] array = new String[1];

        try {
            metaDataModel.runReadAction(
                new Action<Metadata>() {
                @Override
                    public void run(Metadata metaData) {
                        Table table = handle.resolve(metaData);
                        if (table != null && table.getParent() != null) {
                            array[0] = table.getParent().getParent().getName();
                        }
                    }
                }
            );
        } catch (MetadataModelException e) {
            NodeRegistry.handleMetadataModelException(TableNode.class, connection, e, true);
        }

        return array[0];
    }
}
