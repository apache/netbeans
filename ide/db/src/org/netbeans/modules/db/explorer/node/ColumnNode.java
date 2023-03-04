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
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.DatabaseMetaDataTransfer;
import org.netbeans.api.db.explorer.node.BaseNode;
import org.netbeans.api.db.explorer.node.NodeProvider;
import org.netbeans.lib.ddl.impl.RemoveColumn;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.DatabaseConnector;
import org.netbeans.modules.db.explorer.DatabaseMetaDataTransferAccessor;
import org.netbeans.modules.db.metadata.model.api.Action;
import org.netbeans.modules.db.metadata.model.api.Column;
import org.netbeans.modules.db.metadata.model.api.Index;
import org.netbeans.modules.db.metadata.model.api.IndexColumn;
import org.netbeans.modules.db.metadata.model.api.Metadata;
import org.netbeans.modules.db.metadata.model.api.MetadataElementHandle;
import org.netbeans.modules.db.metadata.model.api.MetadataModel;
import org.netbeans.modules.db.metadata.model.api.MetadataModelException;
import org.netbeans.modules.db.metadata.model.api.Nullable;
import org.netbeans.modules.db.metadata.model.api.Table;
import org.netbeans.modules.db.metadata.model.api.Tuple;
import org.netbeans.modules.db.metadata.model.api.PrimaryKey;
import org.openide.nodes.PropertySupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.ExTransferable;

/**
 *
 * @author Rob Englander
 */
public class ColumnNode extends BaseNode implements SchemaNameProvider, ColumnNameProvider {

    private static final String COLUMN = "org/netbeans/modules/db/resources/column.gif";
    private static final String PRIMARY = "org/netbeans/modules/db/resources/columnPrimary.gif";
    private static final String INDEX = "org/netbeans/modules/db/resources/columnIndex.gif";
    private static final String FOLDER = "Column"; //NOI18N
    private static final Logger LOG = Logger.getLogger(ColumnNode.class.getName());
    private static final String DBDATATYPE = "DBDatatype"; //NOI18N
    private static final String DBDATATYPEDESC = "DBDatatypeDescription"; //NOI18N

    /**
     * Create an instance of ColumnNode.
     *
     * @param dataLookup the lookup to use when creating node providers
     * @return the ColumnNode instance
     */
    public static ColumnNode create(NodeDataLookup dataLookup, NodeProvider provider) {
        ColumnNode node = new ColumnNode(dataLookup, provider);
        node.setup();
        return node;
    }
    private String name = ""; // NOI18N
    private String icon;
    /** Description used for tooltip. */
    private String description = "";
    private final MetadataElementHandle<Column> columnHandle;
    private final DatabaseConnection connection;
    private boolean isTableColumn = true;

    @SuppressWarnings("unchecked")
    private ColumnNode(NodeDataLookup lookup, NodeProvider provider) {
        super(lookup, FOLDER, provider);
        // Fix for bug 219113 - see also getShortDescription
        setValue("nodeDescription", 
                NbBundle.getMessage(ConnectionNode.class, "ND_Column"));
        columnHandle = getLookup().lookup(MetadataElementHandle.class);
        connection = getLookup().lookup(DatabaseConnection.class);
    }

    @Override
    public synchronized void refresh() {
        setupNames();
        super.refresh();
    }

    @Override
    protected void initialize() {
        setupNames();
    }

    private void setupNames() {
        boolean connected = connection.isConnected();
        MetadataModel metaDataModel = connection.getMetadataModel();
        if (connected && metaDataModel != null) {
            try {
                metaDataModel.runReadAction(
                    new Action<Metadata>() {

                        @Override
                        public void run(Metadata metaData) {
                                boolean isPartOfPrimaryKey = false;
                                boolean isPartOfIndex = false;
                            Column column = columnHandle.resolve(metaData);
                            if (column != null) {
                                name = column.getName();
                                icon = COLUMN;

                                Tuple tuple = column.getParent();
                                if (tuple instanceof Table) {
                                        Table table = (Table) tuple;
                                    PrimaryKey pkey = table.getPrimaryKey();

                                    if (pkey != null) {
                                        Collection<Column> columns = pkey.getColumns();
                                        for (Column c : columns) {
                                            if (c != null && column.getName().equals(c.getName())) {
                                                icon = PRIMARY;
                                                    isPartOfPrimaryKey = true;
                                                break;
                                            }
                                        }
                                    }

                                        Collection<Index> indexes = table.getIndexes();
                                        for (Index index : indexes) {
                                            Collection<IndexColumn> columns = index.getColumns();
                                            for (IndexColumn c : columns) {
                                                if (c.getName().equals(column.getName())) {
                                                    if (!isPartOfPrimaryKey) {
                                                    icon = INDEX;
                                                }
                                                    isPartOfIndex = true;
                                                    break;
                                            }
                                        }
                                    }
                                        isTableColumn = true;
                                } else {
                                    isTableColumn = false;
                                }
                                    
                                    updateProperties(column, isPartOfPrimaryKey, isPartOfIndex);
                            }
                        }
                        });
            } catch (MetadataModelException e) {
                NodeRegistry.handleMetadataModelException(this.getClass(), connection, e, true);
            }
        }
    }

    private void updateProperties(Column column, boolean isPartOfPrimaryKey, boolean isPartOfIndex) {
        PropertySupport.Name ps = new PropertySupport.Name(this);
        addProperty(ps);

        assert column != null : "Column " + this + " cannot be null.";
        if (column == null) {
            return;
        }

        try {
            addProperty(NULL, NULLDESC, Boolean.class, false, column.getNullable() == Nullable.NULLABLE);

            if (column.getType() != null) {
                addProperty(DATATYPE, DATATYPEDESC, String.class, false, column.getType().toString());
            }

            addProperty(DBDATATYPE, DBDATATYPEDESC, String.class, false,
                    column.getTypeName() == null ? "" : column.getTypeName());

            int len = column.getLength();

            if (len == 0) {
                len = column.getPrecision();
            }

            addProperty(COLUMNSIZE, COLUMNSIZEDESC, Integer.class, false, len);
            addProperty(DIGITS, DIGITSDESC, Short.class, false, column.getScale());
            addProperty(POSITION, POSITIONDESC, Integer.class, false, column.getPosition());
            addProperty(PKPART, PKPARTDESC, Boolean.class, false, isPartOfPrimaryKey);
            addProperty(INDEXPART, INDEXPARTDESC, Boolean.class, false, isPartOfIndex);
            
        StringBuilder strBuf = new StringBuilder("<html>");
        strBuf.append("<table border=0 cellspacing=0 cellpadding=0 >")
              .append("<tr><td>&nbsp;")
              .append(NbBundle.getMessage(BaseNode.class, TYPE))
              .append("</td><td>&nbsp; : &nbsp; <b>")
              .append(column.getType())
              .append("</b></td></tr>")
              .append("<tr><td>&nbsp;")
              .append(NbBundle.getMessage(BaseNode.class, DBDATATYPE))
              .append("</td><td>&nbsp; : &nbsp; <b>")
              .append(column.getTypeName())
              .append("</b></td></tr>")
              .append("<tr><td>&nbsp;")
              .append(NbBundle.getMessage(BaseNode.class, COLUMNSIZE))
              .append("</td><td>&nbsp; : &nbsp; <b>")
              .append(len)
              .append("</b></td></tr>")
              .append("<tr><td>&nbsp;")
              .append(NbBundle.getMessage(BaseNode.class, DIGITS))
              .append("</td><td>&nbsp; : &nbsp; <b>")
              .append(column.getScale())
              .append("</b></td></tr>")
              .append("<tr><td>&nbsp;")
              .append(NbBundle.getMessage(BaseNode.class, PKPART))
              .append("</td><td>&nbsp; : &nbsp; <b>")
              .append(isPartOfPrimaryKey)
              .append("</b></td></tr>")
              .append("<tr><td>&nbsp;")
              .append(NbBundle.getMessage(BaseNode.class, INDEXPART))
              .append("</td><td>&nbsp; : &nbsp; <b>")
              .append(isPartOfIndex)
              .append("</b></td></tr>")
              .append("<tr><td>&nbsp;")
              .append(NbBundle.getMessage(BaseNode.class, POSITION))
              .append("</td><td>&nbsp; : &nbsp; <b>")
              .append(column.getPosition())
              .append("</b></td></tr>")
              .append("</table></html>")
            ;
            description = strBuf.toString();
        } catch (Exception e) {
            LOG.log(Level.INFO, e.getMessage(), e);
        }
    }

    @Override
    public String getColumnName() {
        return getColumnName(connection, columnHandle);
    }

    @Override
    public String getSchemaName() {
        return getSchemaName(connection, columnHandle);
    }

    @Override
    public String getCatalogName() {
        return getCatalogName(connection, columnHandle);
    }

    @Override
    public String getParentName() {
        return getParentName(connection, columnHandle);
    }

    public int getPosition() {
        MetadataModel metaDataModel = connection.getMetadataModel();
        final int[] array = new int[1];
        array[0] = 1;
        
        if (metaDataModel == null) {
            LOG.log(Level.INFO, "Null MetadataModel for " + connection);
            return array[0];
        }

        try {
            metaDataModel.runReadAction(
                new Action<Metadata>() {

                    @Override
                    public void run(Metadata metaData) {
                        Column column = columnHandle.resolve(metaData);
                        if (column != null) {
                            array[0] = column.getPosition();
                        }
                    }
                    });
        } catch (MetadataModelException e) {
            NodeRegistry.handleMetadataModelException(this.getClass(), connection, e, true);
        }

        return array[0];
    }

    @Override
    public void destroy() {
        DatabaseConnector connector = connection.getConnector();
        Specification spec = connector.getDatabaseSpecification();

        try {
            RemoveColumn command = spec.createCommandRemoveColumn(getParentName());

            String schema = getSchemaName();
            if (schema == null) {
                schema = getCatalogName();
            }

            command.setObjectOwner(schema);
            command.removeColumn(getName());
            command.execute();
        } catch (Exception e) {
            LOG.log(Level.INFO, e.getMessage(), e);
        }

        setValue(BaseFilterNode.REFRESH_ANCESTOR_DISTANCE, Integer.valueOf(1));
    }

    @Override
    public boolean canDestroy() {
        if (isTableColumn) {
            DatabaseConnector connector = connection.getConnector();
            return connector.supportsCommand(Specification.REMOVE_COLUMN);
        } else {
            return false;
        }
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
        return icon;
    }

    @Override
    public boolean canCopy() {
        return true;
    }

    @Override
    public Transferable clipboardCopy() throws IOException {
        ExTransferable result = ExTransferable.create(super.clipboardCopy());
        result.put(new ExTransferable.Single(DatabaseMetaDataTransfer.COLUMN_FLAVOR) {

            @Override
            protected Object getData() {
                return DatabaseMetaDataTransferAccessor.DEFAULT.createColumnData(connection.getDatabaseConnection(),
                        connection.findJDBCDriver(), getParentName(), getName());
            }
        });
        return result;
    }

    @Override
    public String getShortDescription() {
        // the description is intended to be used by the tooltips and in this
        // case shows info about column, the node description (used in the 
        // property sheet display) is overrriden via 
        // putValue("nodeDescription", ...) in the constructor)
        return description;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(ColumnNode.class);
    }

    public static String getColumnName(DatabaseConnection connection, final MetadataElementHandle<Column> handle) {
        MetadataModel metaDataModel = connection.getMetadataModel();
        final String[] array = {null};
        try {
            metaDataModel.runReadAction(
                new Action<Metadata>() {

                    @Override
                    public void run(Metadata metaData) {
                        Column column = handle.resolve(metaData);
                        if (column != null) {
                            array[0] = column.getName();
                        }
                    }
                    });
        } catch (MetadataModelException e) {
            NodeRegistry.handleMetadataModelException(ColumnNode.class, connection, e, true);
        }

        return array[0];
    }

    public static String getParentName(DatabaseConnection connection, final MetadataElementHandle<Column> handle) {
        MetadataModel metaDataModel = connection.getMetadataModel();
        final String[] array = {null};

        try {
            metaDataModel.runReadAction(
                new Action<Metadata>() {

                    @Override
                    public void run(Metadata metaData) {
                        Column column = handle.resolve(metaData);
                        if (column != null) {
                            array[0] = column.getParent().getName();
                        }
                    }
                    });
        } catch (MetadataModelException e) {
            NodeRegistry.handleMetadataModelException(ColumnNode.class, connection, e, true);
        }

        return array[0];
    }

    public static String getSchemaName(DatabaseConnection connection, final MetadataElementHandle<Column> handle) {
        MetadataModel metaDataModel = connection.getMetadataModel();
        final String[] array = new String[1];

        try {
            metaDataModel.runReadAction(
                new Action<Metadata>() {

                    @Override
                    public void run(Metadata metaData) {
                        Column column = handle.resolve(metaData);
                        if (column != null) {
                            array[0] = column.getParent().getParent().getName();
                        }
                    }
                    });
        } catch (MetadataModelException e) {
            NodeRegistry.handleMetadataModelException(ColumnNode.class, connection, e, true);
        }

        return array[0];
    }

    public static String getCatalogName(DatabaseConnection connection, final MetadataElementHandle<Column> handle) {
        MetadataModel metaDataModel = connection.getMetadataModel();
        final String[] array = new String[1];

        try {
            metaDataModel.runReadAction(
                new Action<Metadata>() {

                    @Override
                    public void run(Metadata metaData) {
                        Column column = handle.resolve(metaData);
                        if (column != null) {
                            array[0] = column.getParent().getParent().getParent().getName();
                        }
                    }
                    });
        } catch (MetadataModelException e) {
            NodeRegistry.handleMetadataModelException(ColumnNode.class, connection, e, true);
        }

        return array[0];
    }
}
