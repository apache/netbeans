/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.db.explorer.node;

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

/**
 *
 * @author Rob Englander
 */
public class TableListNode extends BaseNode implements SchemaNameProvider {

    public enum Type {

        SYSTEM, STANDARD, ALL
    }

    private static final String NAME = "Tables"; // NOI18N
    private static final String SYSTEM_NAME = "SystemTables"; //NOI18N
    private static final String ICONBASE = "org/netbeans/modules/db/resources/folder.gif"; // NOI18N
    private static final String FOLDER = "TableList"; //NOI18N
    private static final String SYSTEM_FOLDER = "SystemTableList"; //NOI18N

    private MetadataElementHandle<Schema> schemaHandle;
    private final DatabaseConnection connection;
    private final Type type;

    /**
     * Create an instance of TableListNode.
     * 
     * @param dataLookup the lookup to use when creating node providers
     * @return the TableListNode instance
     */
    public static TableListNode create(NodeDataLookup dataLookup, NodeProvider provider, Type type) {
        TableListNode node = new TableListNode(dataLookup, provider, type);
        node.setup();
        return node;
    }

    private TableListNode(NodeDataLookup lookup, NodeProvider provider, Type type) {
        super(new ChildNodeFactory(lookup), lookup, Type.SYSTEM == type ? SYSTEM_FOLDER : FOLDER, provider);
        this.connection = getLookup().lookup(DatabaseConnection.class);
        this.type = type;
    }
    
    @SuppressWarnings("unchecked")
    protected void initialize() {
        schemaHandle = getLookup().lookup(MetadataElementHandle.class);
    }
    
    @Override
    public String getName() {
        switch (type) {
            case SYSTEM:
                return SYSTEM_NAME;
            default:
                return NAME;
        }
    }

    @Override
    public String getDisplayName() {
        switch (type) {
            case SYSTEM:
                return NbBundle.getMessage(TableListNode.class,
                        "SystemTableListNode_DISPLAYNAME"); //NOI18N
            default:
                return NbBundle.getMessage(TableListNode.class,
                        "TableListNode_DISPLAYNAME"); //NOI18N
        }
    }

    @Override
    public String getIconBase() {
        return ICONBASE;
    }

    @Override
    public String getShortDescription() {
        switch (type) {
            case SYSTEM:
                return NbBundle.getMessage(TableListNode.class, "ND_SystemTableList"); //NOI18N
            default:
                return NbBundle.getMessage(TableListNode.class, "ND_TableList"); //NOI18N
        }
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(TableListNode.class);
    }

    public String getSchemaName() {
        return getSchemaName(connection, schemaHandle);
    }

    public String getCatalogName() {
        return getCatalogName(connection, schemaHandle);
    }

    public static String getSchemaName(DatabaseConnection connection, final MetadataElementHandle<Schema> handle) {
        MetadataModel metaDataModel = connection.getMetadataModel();
        final String[] array = new String[1];

        try {
            metaDataModel.runReadAction(
                new Action<Metadata>() {
                    public void run(Metadata metaData) {
                        Schema schema = handle.resolve(metaData);
                        if (schema != null) {
                            array[0] = schema.getName();
                        }
                    }
                }
            );
        } catch (MetadataModelException e) {
            NodeRegistry.handleMetadataModelException(TableListNode.class, connection, e, true);
        }

        return array[0];
    }

    public static String getCatalogName(DatabaseConnection connection, final MetadataElementHandle<Schema> handle) {
        MetadataModel metaDataModel = connection.getMetadataModel();
        final String[] array = new String[1];

        try {
            metaDataModel.runReadAction(
                new Action<Metadata>() {
                    public void run(Metadata metaData) {
                        Schema schema = handle.resolve(metaData);
                        if (schema != null) {
                            array[0] = schema.getParent().getName();
                        }
                    }
                }
            );
        } catch (MetadataModelException e) {
            NodeRegistry.handleMetadataModelException(TableListNode.class, connection, e, true);
        }

        return array[0];
    }

    public Type getType() {
        return type;
    }
}
