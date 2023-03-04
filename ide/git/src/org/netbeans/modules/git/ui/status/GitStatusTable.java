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

package org.netbeans.modules.git.ui.status;

import org.netbeans.modules.git.GitStatusNode;
import org.netbeans.modules.git.ui.status.VersioningPanelController.ModeKeeper;
import org.netbeans.modules.versioning.util.status.VCSStatusTableModel;
import org.netbeans.modules.versioning.util.status.VCSStatusTable;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableCellRenderer;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.git.ui.commit.DeleteLocalAction;
import org.netbeans.modules.git.ui.diff.DiffAction;
import org.netbeans.modules.git.ui.status.VersioningPanelController.GitStatusNodeImpl;
import org.netbeans.modules.versioning.util.FilePathCellRenderer;
import org.netbeans.modules.versioning.util.status.VCSStatusNode;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 * Controls the {@link #getComponent() tsble} that displays nodes
 * in the Versioning view. The table is  {@link #setTableModel populated)
 * from VersioningPanel.
 * 
 * @author Maros Sandor
 */
class GitStatusTable extends VCSStatusTable<GitStatusNodeImpl> {
    private final ModeKeeper modeKeeper;
    private final VersioningPanelController master;

    public GitStatusTable (VersioningPanelController master, VCSStatusTableModel<GitStatusNodeImpl> model,
            VersioningPanelController.ModeKeeper modeKeeper) {
        super(model);
        this.master = master;
        this.modeKeeper = modeKeeper;
        setDefaultRenderer(new SyncTableCellRenderer());
    }

    @Override
    protected void setModelProperties () {
        Node.Property [] properties = new Node.Property[3];
        properties[0] = new ColumnDescriptor<>(GitStatusNode.NameProperty.NAME, String.class, GitStatusNode.NameProperty.DISPLAY_NAME, GitStatusNode.NameProperty.DESCRIPTION);
        properties[1] = new ColumnDescriptor<>(GitStatusNode.GitStatusProperty.NAME, String.class, GitStatusNode.GitStatusProperty.DISPLAY_NAME, GitStatusNode.GitStatusProperty.DESCRIPTION);
        properties[2] = new ColumnDescriptor<>(GitStatusNode.PathProperty.NAME, String.class, GitStatusNode.PathProperty.DISPLAY_NAME, GitStatusNode.PathProperty.DESCRIPTION);
        tableModel.setProperties(properties);
        getTable().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "DeleteAction");
        getTable().getActionMap().put("DeleteAction", SystemAction.get(DeleteLocalAction.class));
    }
        
    @Override
    @NbBundle.Messages({
        "CTL_GitStatusTable.popup.initializing=Initializing..."
    })
    protected JPopupMenu getPopup () {
        return master.getPopupFor(getSelectedNodes());
    }

    @Override
    protected void mouseClicked (VCSStatusNode node) {
        Action action = node.getNodeAction();
        if (action != null && action.isEnabled()) {
            if (action instanceof DiffAction) {
                modeKeeper.storeMode();
            }
            action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, node.getFile().getAbsolutePath()));
        }
    }

    @Override
    public Object prepareModel (GitStatusNodeImpl[] nodes) {
        return null;
    }

    @Override
    public void setModel (GitStatusNodeImpl[] nodes, EditorCookie[] editorCookies, Object modelData) {
        super.setNodes(nodes);
    }

    private class SyncTableCellRenderer extends DefaultTableCellRenderer {
        private FilePathCellRenderer pathRenderer = new FilePathCellRenderer();

        @Override
        public Component getTableCellRendererComponent (JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component renderer;
            int modelColumnIndex = table.convertColumnIndexToModel(column);
            GitStatusNode node = null;
            if (modelColumnIndex == 0) {
                node = tableModel.getNode(table.convertRowIndexToModel(row));
                if (!isSelected) {
                    value = node.getHtmlDisplayName();
                }
                if (GitModuleConfig.getDefault().isExcludedFromCommit(node.getFile().getAbsolutePath())) {
                    value = "<s>" + value + "</s>"; //NOI18N
                }
                value = "<html>" + value; // NOI18N
            }
            if (modelColumnIndex == 2) {
                renderer = pathRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            } else {
                renderer = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
            if (renderer instanceof JComponent) {
                if (node == null) {
                    node = tableModel.getNode(table.convertRowIndexToModel(row));
                }
                String path = node.getFile().getAbsolutePath();
                ((JComponent) renderer).setToolTipText(path);
            }
            return renderer;
        }
    }
}
