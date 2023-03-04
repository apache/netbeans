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
import org.netbeans.modules.db.metadata.model.api.MetadataModelException;
import org.netbeans.modules.db.metadata.model.api.Value;
import org.openide.nodes.PropertySupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Rob Englander
 */
public class ReturnValueNode  extends BaseNode {
    private static final String RETURN = "org/netbeans/modules/db/resources/paramReturn.gif";
    private static final String FOLDER = "ProcedureParam"; //NOI18N
    private static final String DBDATATYPE = "DBDatatype"; //NOI18N
    private static final String DBDATATYPEDESC = "DBDatatypeDescription";  //NOI18N

    /**
     * Create an instance of ReturnValueNode.
     *
     * @param dataLookup the lookup to use when creating node providers
     * @return the ReturnValueNode instance
     */
    public static ReturnValueNode create(NodeDataLookup dataLookup, NodeProvider provider) {
        ReturnValueNode node = new ReturnValueNode(dataLookup, provider);
        node.setup();
        return node;
    }

    private String name = ""; // NOI18N
    private final MetadataElementHandle<Value> valueHandle;
    private final DatabaseConnection connection;

    @SuppressWarnings("unchecked")
    private ReturnValueNode(NodeDataLookup lookup, NodeProvider provider) {
        super(lookup, FOLDER, provider);
        valueHandle = getLookup().lookup(MetadataElementHandle.class);
        connection = getLookup().lookup(DatabaseConnection.class);
    }

    @Override
    public synchronized void refresh() {
        setupNames();
        super.refresh();
    }

    private void setupNames() {
        boolean connected = connection.isConnected();
        MetadataModel metaDataModel = connection.getMetadataModel();
        if (connected && metaDataModel != null) {
            try {
                metaDataModel.runReadAction(
                    new Action<Metadata>() {
                        public void run(Metadata metaData) {
                            Value parameter = valueHandle.resolve(metaData);
                            if (parameter != null) {
                                name = parameter.getName();
                                updateProperties(parameter);
                            }
                        }
                    }
                );
            } catch (MetadataModelException e) {
                NodeRegistry.handleMetadataModelException(this.getClass(), connection, e, true);
            }
        }
    }

    private void updateProperties(Value param) {
        PropertySupport ps = new PropertySupport.Name(this);
        addProperty(ps);

        addProperty(TYPE, TYPEDESC, String.class, false, NbBundle.getMessage (ReturnValueNode.class, "Return")); // NOI18N
        addProperty(DATATYPE, DATATYPEDESC, String.class, false, param.getType().toString());

        addProperty(DBDATATYPE, DBDATATYPEDESC, String.class, false,
                param.getTypeName() == null ? "" : param.getTypeName());
    }

    protected void initialize() {
        setupNames();
    }

    @Override
    public String getIconBase() {
        return RETURN;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getShortDescription() {
        return NbBundle.getMessage (ReturnValueNode.class, "ND_ProcedureParam"); //NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(ReturnValueNode.class);
    }
}
