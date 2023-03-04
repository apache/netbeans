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
import org.netbeans.modules.db.metadata.model.api.Parameter;
import org.openide.nodes.PropertySupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Rob Englander
 */
public class ProcedureParamNode  extends BaseNode {
    private static final String IN = "org/netbeans/modules/db/resources/paramIn.gif"; // NOI18N
    private static final String OUT = "org/netbeans/modules/db/resources/paramOut.gif"; // NOI18N
    private static final String INOUT = "org/netbeans/modules/db/resources/paramInOut.gif"; // NOI18N
    private static final String FOLDER = "ProcedureParam"; //NOI18N
    private static final String DBDATATYPE = "DBDatatype"; //NOI18N
    private static final String DBDATATYPEDESC = "DBDatatypeDescription";  //NOI18N

    /**
     * Create an instance of ProcedureParamNode.
     *
     * @param dataLookup the lookup to use when creating node providers
     * @return the ProcedureParamNode instance
     */
    public static ProcedureParamNode create(NodeDataLookup dataLookup, NodeProvider provider) {
        ProcedureParamNode node = new ProcedureParamNode(dataLookup, provider);
        node.setup();
        return node;
    }

    private String name = ""; // NOI18N
    private String icon = ""; // NOI18N
    private final MetadataElementHandle<Parameter> paramHandle;
    private final DatabaseConnection connection;

    @SuppressWarnings("unchecked")
    private ProcedureParamNode(NodeDataLookup lookup, NodeProvider provider) {
        super(lookup, FOLDER, provider);
        paramHandle = getLookup().lookup(MetadataElementHandle.class);
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
                    @Override
                        public void run(Metadata metaData) {
                            Parameter parameter = paramHandle.resolve(metaData);
                            if (parameter != null) {
                                name = parameter.getName();

                                switch (parameter.getDirection()) {
                                    case IN:
                                        icon = IN;
                                        break;
                                    case OUT:
                                        icon = OUT;
                                        break;
                                    case INOUT:
                                        icon = INOUT;
                                        break;
                                }

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

    private void updateProperties(Parameter param) {
        PropertySupport<String> ps = new PropertySupport.Name(this);
        addProperty(ps);

        switch (param.getDirection()) {
            case IN:
                addProperty(TYPE, TYPEDESC, String.class, false, NbBundle.getMessage (ProcedureParamNode.class, "In")); // NOI18N
                break;
            case OUT:
                addProperty(TYPE, TYPEDESC, String.class, false, NbBundle.getMessage (ProcedureParamNode.class, "Out")); // NOI18N
                break;
            case INOUT:
                addProperty(TYPE, TYPEDESC, String.class, false, NbBundle.getMessage (ProcedureParamNode.class, "InOut")); // NOI18N
                break;
        }

        addProperty(DATATYPE, DATATYPEDESC, String.class, false, param.getType() == null ? "null" : param.getType().toString()); // NOI18N

        addProperty(DBDATATYPE, DBDATATYPEDESC, String.class, false,
                param.getTypeName() == null ? "" : param.getTypeName());
    }

    @Override
    protected void initialize() {
        setupNames();
    }

    public int getPosition() {
        MetadataModel metaDataModel = connection.getMetadataModel();
        final int[] array = new int[1];

        try {
            metaDataModel.runReadAction(
                new Action<Metadata>() {
                @Override
                    public void run(Metadata metaData) {
                        Parameter param = paramHandle.resolve(metaData);
                        array[0] = param.getOrdinalPosition();
                    }
                }
            );
        } catch (MetadataModelException e) {
            NodeRegistry.handleMetadataModelException(this.getClass(), connection, e, true);
        }

        return array[0];
    }

    @Override
    public String getIconBase() {
        return icon;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getShortDescription() {
        return NbBundle.getMessage (ProcedureParamNode.class, "ND_ProcedureParam"); //NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(ProcedureParamNode.class);
    }
}
