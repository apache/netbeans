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
package org.netbeans.modules.mercurial.ui.diff;

import java.awt.Image;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.versioning.util.common.FileTreeView;
import org.netbeans.swing.outline.RenderDataProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Ondrej Vrabec
 */
class DiffFileTreeImpl extends FileTreeView<DiffNode> {
    
    private static final String ICON_KEY_UIMANAGER = "Tree.closedIcon"; //NOI18N
    private static final String ICON_KEY_UIMANAGER_NB = "Nb.Explorer.Folder.icon"; //NOI18N
    private static final String PATH_SEPARATOR_REGEXP = File.separator.replace("\\", "\\\\"); //NOI18N
    
    /**
     * Defines labels for Diff view table columns.
     */ 
    private static final Map<String, String[]> columnLabels = new HashMap<String, String[]>(4);
    private static Image FOLDER_ICON;
    private final MultiDiffPanel master;

    {
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
                if (HgModuleConfig.getDefault().isExcludedFromCommit(node.getSetup().getBaseFile().getAbsolutePath())) {
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
