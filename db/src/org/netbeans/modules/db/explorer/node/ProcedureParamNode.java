/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009-2010 Sun Microsystems, Inc.
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
