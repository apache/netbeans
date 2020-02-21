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
package org.netbeans.modules.mercurial.remote.ui.diff;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import org.netbeans.modules.mercurial.remote.HgModuleConfig;
import org.netbeans.modules.remotefs.versioning.util.common.FileTreeView;
import org.netbeans.swing.outline.RenderDataProvider;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * 
 */
class DiffFileTreeImpl extends FileTreeView<DiffNode> {
        
    /**
     * Defines labels for Diff view table columns.
     */ 
    private static final Map<String, String[]> columnLabels = new HashMap<>(4);
    private final MultiDiffPanel master;

    static {
        ResourceBundle loc = NbBundle.getBundle(DiffFileTreeImpl.class);
        columnLabels.put(DiffNode.COLUMN_NAME_STATUS, new String [] { 
                loc.getString("CTL_DiffTable_Column_Status_Title"), 
                loc.getString("CTL_DiffTable_Column_Status_Desc")});
        columnLabels.put(DiffNode.COLUMN_NAME_LOCATION, new String [] { 
                loc.getString("CTL_DiffTable_Column_Location_Title"), 
                loc.getString("CTL_DiffTable_Column_Location_Desc")});
    }

    public DiffFileTreeImpl (MultiDiffPanel master) {
        super();
        this.master = master;
        setupColumns();
    }
    
    @SuppressWarnings("unchecked")
    private void setupColumns() {
        view.setPropertyColumns(DiffNode.COLUMN_NAME_STATUS, columnLabels.get(DiffNode.COLUMN_NAME_STATUS)[0],
                DiffNode.COLUMN_NAME_LOCATION, columnLabels.get(DiffNode.COLUMN_NAME_LOCATION)[0]);
        view.setPropertyColumnDescription(DiffNode.COLUMN_NAME_STATUS, columnLabels.get(DiffNode.COLUMN_NAME_STATUS)[1]);
        view.setPropertyColumnDescription(DiffNode.COLUMN_NAME_LOCATION, columnLabels.get(DiffNode.COLUMN_NAME_LOCATION)[1]);
        view.getAccessibleContext().setAccessibleName(NbBundle.getMessage(DiffFileTreeImpl.class, "ACSN_DiffTable")); // NOI18N
        view.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DiffFileTreeImpl.class, "ACSD_DiffTable")); // NOI18N
        view.getOutline().setRenderDataProvider(createRenderProvider());
    }
    
    @Override
    protected DiffNode convertToAcceptedNode (Node node) {
        return node instanceof DiffNode ? (DiffNode) node : null;
    }

    private RenderDataProvider createRenderProvider () {
        return new AbstractRenderDataProvider() {
            @Override
            protected String annotateName (DiffNode node, String originalLabel) {
                if (HgModuleConfig.getDefault(master.getRoot()).isExcludedFromCommit(node.getSetup().getBaseFile().getPath())) {
                    originalLabel = "<s>" + (originalLabel == null ? node.getName() : originalLabel) + "</s>"; //NOI18N
                }
                return originalLabel;
            }
        };
    }

    @Override
    protected void nodeSelected (DiffNode node) {
        master.nodeSelected(node);
    }

    @Override
    protected JPopupMenu getPopup () {
        return master.getPopup();
    }
    
    @Override
    protected void setDefaultColumnSizes() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                int width = view.getWidth();
                view.getOutline().getColumnModel().getColumn(0).setPreferredWidth(width * 40 / 100);
                view.getOutline().getColumnModel().getColumn(1).setPreferredWidth(width * 20 / 100);
                view.getOutline().getColumnModel().getColumn(2).setPreferredWidth(width * 40 / 100);
            }
        });
    }
    
}
