/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
        super(new ChildNodeFactory(lookup), lookup, Type.SYSTEM.equals(type) ? SYSTEM_FOLDER : FOLDER, provider);
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
