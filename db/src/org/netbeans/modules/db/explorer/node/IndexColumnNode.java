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
