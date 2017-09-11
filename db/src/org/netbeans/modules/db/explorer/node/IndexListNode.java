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
import org.netbeans.modules.db.metadata.model.api.Table;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Rob Englander
 */
public class IndexListNode extends BaseNode {
    private static final String NAME = "Indexes"; // NOI18N
    private static final String ICONBASE = "org/netbeans/modules/db/resources/folder.gif";
    private static final String FOLDER = "IndexList"; //NOI18N

    private MetadataElementHandle<Table> tableHandle;
    private final DatabaseConnection connection;

    /**
     * Create an instance of IndexListNode.
     *
     * @param dataLookup the lookup to use when creating node providers
     * @return the TableListNode instance
     */
    public static IndexListNode create(NodeDataLookup dataLookup, NodeProvider provider) {
        IndexListNode node = new IndexListNode(dataLookup, provider);
        node.setup();
        return node;
    }

    private IndexListNode(NodeDataLookup lookup, NodeProvider provider) {
        super(new ChildNodeFactory(lookup), lookup, FOLDER, provider);
        connection = getLookup().lookup(DatabaseConnection.class);
    }

    @SuppressWarnings("unchecked")
    protected void initialize() {
        tableHandle = getLookup().lookup(MetadataElementHandle.class);
    }

    public String getCatalogName() {
        return getCatalogName(connection, tableHandle);
    }

    public String getSchemaName() {
        return getSchemaName(connection, tableHandle);
    }

    public String getTableName() {
        return getTableName(connection, tableHandle);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage (IndexListNode.class, "IndexListNode_DISPLAYNAME"); // NOI18N
    }

    @Override
    public String getIconBase() {
        return ICONBASE;
    }

    @Override
    public String getShortDescription() {
        return NbBundle.getMessage (IndexListNode.class, "ND_IndexList"); //NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(IndexListNode.class);
    }

    public static String getTableName(DatabaseConnection connection, final MetadataElementHandle<Table> handle) {
        MetadataModel metaDataModel = connection.getMetadataModel();
        final String[] array = { null };

        try {
            metaDataModel.runReadAction(
                new Action<Metadata>() {
                    public void run(Metadata metaData) {
                        Table table = handle.resolve(metaData);
                        if (table != null) {
                            array[0] = table.getName();
                        }
                    }
                }
            );
        } catch (MetadataModelException e) {
            NodeRegistry.handleMetadataModelException(IndexListNode.class, connection, e, true);
        }

        return array[0];
    }

    public static String getSchemaName(DatabaseConnection connection, final MetadataElementHandle<Table> handle) {
        MetadataModel metaDataModel = connection.getMetadataModel();
        final String[] array = new String[1];

        try {
            metaDataModel.runReadAction(
                new Action<Metadata>() {
                    public void run(Metadata metaData) {
                        Table table = handle.resolve(metaData);
                        if (table != null) {
                            array[0] = table.getParent().getName();
                        }
                    }
                }
            );
        } catch (MetadataModelException e) {
            NodeRegistry.handleMetadataModelException(IndexListNode.class, connection, e, true);
        }

        return array[0];
    }

    public static String getCatalogName(DatabaseConnection connection, final MetadataElementHandle<Table> handle) {
        MetadataModel metaDataModel = connection.getMetadataModel();
        final String[] array = new String[1];

        try {
            metaDataModel.runReadAction(
                new Action<Metadata>() {
                    public void run(Metadata metaData) {
                        Table table = handle.resolve(metaData);
                        if (table != null) {
                            array[0] = table.getParent().getParent().getName();
                        }
                    }
                }
            );
        } catch (MetadataModelException e) {
            NodeRegistry.handleMetadataModelException(IndexListNode.class, connection, e, true);
        }

        return array[0];
    }
}
