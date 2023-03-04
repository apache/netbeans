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
import org.netbeans.modules.db.metadata.model.api.Metadata;
import org.netbeans.modules.db.metadata.model.api.MetadataElementHandle;
import org.netbeans.modules.db.metadata.model.api.MetadataModel;
import org.netbeans.modules.db.metadata.model.api.IndexColumn;
import org.netbeans.modules.db.metadata.model.api.MetadataModelException;
import org.netbeans.modules.db.metadata.model.api.Ordering;
import org.openide.nodes.PropertySupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Rob Englander
 */
public class IndexColumnNode extends BaseNode {
    private static final String ICONDOWN = "org/netbeans/modules/db/resources/indexDown.gif";
    private static final String ICONUP = "org/netbeans/modules/db/resources/indexUp.gif";
    private static final String FOLDER = "IndexColumn"; //NOI18N

    /**
     * Create an instance of IndexColumnNode.
     *
     * @param dataLookup the lookup to use when creating node providers
     * @return the IndexColumnNode instance
     */
    public static IndexColumnNode create(NodeDataLookup dataLookup, NodeProvider provider) {
        IndexColumnNode node = new IndexColumnNode(dataLookup, provider);
        node.setup();
        return node;
    }

    private String name = ""; // NOI18N
    private String icon = ""; // NOI18N
    private int position = 0;
    private MetadataElementHandle<IndexColumn> indexColumnHandle;
    private final DatabaseConnection connection;

    @SuppressWarnings("unchecked")
    private IndexColumnNode(NodeDataLookup lookup, NodeProvider provider) {
        super(lookup, FOLDER, provider);
        connection = getLookup().lookup(DatabaseConnection.class);
        indexColumnHandle = getLookup().lookup(MetadataElementHandle.class);
    }

    protected void initialize() {
        boolean connected = connection.isConnected();
        MetadataModel metaDataModel = connection.getMetadataModel();
        if (connected && metaDataModel != null) {
            try {
                metaDataModel.runReadAction(
                    new Action<Metadata>() {
                        public void run(Metadata metaData) {
                            IndexColumn column = indexColumnHandle.resolve(metaData);
                            name = column.getName();
                            if (column.getOrdering() == Ordering.DESCENDING) {
                                icon = ICONUP;
                            } else {
                                icon = ICONDOWN;
                            }

                            position = column.getPosition();

                            updateProperties(column);
                        }
                    }
                );
            } catch (MetadataModelException e) {
                NodeRegistry.handleMetadataModelException(this.getClass(), connection, e, true);
            }
        }
    }

    private void updateProperties(IndexColumn column) {
        PropertySupport ps = new PropertySupport.Name(this);
        addProperty(ps);

        addProperty(POSITION, POSITIONDESC, Integer.class, false, column.getPosition());
    }

    public int getPosition() {
        return position;
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
    public String getShortDescription() {
        return NbBundle.getMessage (ForeignKeyListNode.class, "ND_Column"); //NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(IndexColumnNode.class);
    }
}
