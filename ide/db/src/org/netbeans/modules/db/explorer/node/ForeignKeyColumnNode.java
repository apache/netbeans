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

import org.netbeans.api.db.explorer.node.BaseNode;
import org.netbeans.api.db.explorer.node.NodeProvider;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.metadata.model.api.Action;
import org.netbeans.modules.db.metadata.model.api.Column;
import org.netbeans.modules.db.metadata.model.api.Metadata;
import org.netbeans.modules.db.metadata.model.api.MetadataElementHandle;
import org.netbeans.modules.db.metadata.model.api.MetadataModel;
import org.netbeans.modules.db.metadata.model.api.ForeignKeyColumn;
import org.netbeans.modules.db.metadata.model.api.MetadataModelException;
import org.openide.nodes.PropertySupport;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Rob Englander
 */
public class ForeignKeyColumnNode extends BaseNode {
    private static final String ICON = "org/netbeans/modules/db/resources/columnForeign.gif";
    private static final String FOLDER = "ForeignKeyColumn"; //NOI18N

    /**
     * Create an instance of ForeignKeyColumnNode.
     *
     * @param dataLookup the lookup to use when creating node providers
     * @return the ForeignKeyColumnNode instance
     */
    public static ForeignKeyColumnNode create(NodeDataLookup dataLookup, NodeProvider provider) {
        ForeignKeyColumnNode node = new ForeignKeyColumnNode(dataLookup, provider);
        node.setup();
        return node;
    }

    private String name = ""; // NOI18N
    private final MetadataElementHandle<ForeignKeyColumn> keyColumnHandle;
    private final DatabaseConnection connection;

    @SuppressWarnings("unchecked")
    private ForeignKeyColumnNode(NodeDataLookup lookup, NodeProvider provider) {
        super(lookup, FOLDER, provider);
        connection = getLookup().lookup(DatabaseConnection.class);
        keyColumnHandle = getLookup().lookup(MetadataElementHandle.class);
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
                            ForeignKeyColumn column = keyColumnHandle.resolve(metaData);
                            name = column.getReferringColumn().getName()
                                    + " -> " + column.getReferredColumn().getParent().getName() + "." // NOI18N
                                    + column.getReferredColumn().getName(); // NOI18N

                            updateProperties(column);
                        }
                    }
                );
            } catch (MetadataModelException e) {
                NodeRegistry.handleMetadataModelException(this.getClass(), connection, e, true);
            }
        }
    }

    private void updateProperties(ForeignKeyColumn column) {
        PropertySupport ps = new PropertySupport.Name(this);
        addProperty(ps);

        try {
            Column referred = column.getReferredColumn();
            Column referring = column.getReferringColumn();

            addProperty(FKPOSITION, FKPOSITIONDESC, Integer.class, false, column.getPosition());
            addProperty(FKREFERRINGSCHEMA, FKREFERRINGSCHEMADESC, String.class, false, referring.getParent().getParent().getName());
            addProperty(FKREFERRINGTABLE, FKREFERRINGTABLEDESC, String.class, false, referring.getParent().getName());
            addProperty(FKREFERRINGCOLUMN, FKREFERRINGCOLUMNDESC, String.class, false, referring.getName());
            addProperty(FKREFERREDSCHEMA, FKREFERREDSCHEMADESC, String.class, false, referred.getParent().getParent().getName());
            addProperty(FKREFERREDTABLE, FKREFERREDTABLEDESC, String.class, false, referred.getParent().getName());
            addProperty(FKREFERREDCOLUMN, FKREFERREDCOLUMNDESC, String.class, false, referred.getName());

        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
    }

    public int getPosition() {
        MetadataModel metaDataModel = connection.getMetadataModel();
        final int[] array = new int[1];

        try {
            metaDataModel.runReadAction(
                new Action<Metadata>() {
                    @Override
                    public void run(Metadata metaData) {
                        ForeignKeyColumn column = keyColumnHandle.resolve(metaData);
                        array[0] = column == null ? -1 : column.getPosition();
                    }
                }
            );
        } catch (MetadataModelException e) {
            NodeRegistry.handleMetadataModelException(this.getClass(), connection, e, true);
        }

        return array[0];
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
        return ICON;
    }

    @Override
    public String getShortDescription() {
        return NbBundle.getMessage (ForeignKeyColumnNode.class, "ND_Column"); //NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(ForeignKeyColumnNode.class);
    }
}
