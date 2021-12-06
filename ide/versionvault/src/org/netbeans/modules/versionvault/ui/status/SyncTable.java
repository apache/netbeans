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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

/*
 * Copyright 2021 HCL America, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.netbeans.modules.versionvault.ui.status;

import org.openide.explorer.view.NodeTableModel;
import org.openide.nodes.*;
import org.openide.nodes.PropertySupport.ReadOnly;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle;
import org.openide.awt.Mnemonics;
import org.openide.awt.MouseUtils;
import org.netbeans.modules.versioning.util.FilePathCellRenderer;
import org.netbeans.modules.versioning.util.TableSorter;
import org.netbeans.modules.versioning.util.OpenInEditorAction;
import org.netbeans.modules.versioning.spi.VCSContext;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.AncestorEvent;
import java.lang.reflect.InvocationTargetException;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.Component;
import java.awt.Color;
import java.awt.Point;
import java.util.*;
import java.util.logging.Level;
import java.io.File;

import org.netbeans.modules.versionvault.Clearcase;
import org.netbeans.modules.versionvault.ClearcaseModuleConfig;
import org.netbeans.modules.versionvault.ui.AnnotateAction;
import org.netbeans.modules.versionvault.ui.diff.DiffAction;
import org.netbeans.modules.versionvault.ui.IgnoreAction;
import org.netbeans.modules.versionvault.ui.add.AddAction;
import org.netbeans.modules.versionvault.ui.history.BrowseHistoryAction;
import org.netbeans.modules.versionvault.ui.history.BrowseVersionTreeAction;
import org.netbeans.modules.versionvault.ui.checkin.CheckinAction;
import org.netbeans.modules.versionvault.ui.checkin.ExcludeAction;
import org.netbeans.modules.versionvault.ui.checkout.CheckoutAction;
import org.netbeans.modules.versionvault.ui.checkout.ReserveAction;
import org.netbeans.modules.versionvault.ui.update.UpdateAction;
import org.netbeans.modules.versioning.util.SortedTable;

/**
 * Controls the {@link #getComponent() tsble} that displays nodes
 * in the Versioning view. The table is  {@link #setTableModel populated)
 * from VersioningPanel.
 * 
 * @author Maros Sandor
 */
class SyncTable implements MouseListener, ListSelectionListener, AncestorListener {

    private NodeTableModel  tableModel;
    private JTable          table;
    private JScrollPane     component;
    private SyncFileNode [] nodes = new SyncFileNode[0];
    
    private String []   tableColumns; 
    private TableSorter sorter;

    /**
     * Defines labels for Versioning view table columns.
     */ 
    private static final Map<String, String[]> columnLabels = new HashMap<String, String[]>(4);
    {
        ResourceBundle loc = NbBundle.getBundle(SyncTable.class);
        columnLabels.put(SyncFileNode.COLUMN_NAME_BRANCH, new String [] {
                                          loc.getString("CTL_VersioningView_Column_Branch_Title"), 
                                          loc.getString("CTL_VersioningView_Column_Branch_Desc")});
        columnLabels.put(SyncFileNode.COLUMN_NAME_NAME, new String [] { 
                                          loc.getString("CTL_VersioningView_Column_File_Title"), 
                                          loc.getString("CTL_VersioningView_Column_File_Desc")});
        columnLabels.put(SyncFileNode.COLUMN_NAME_STATUS, new String [] { 
                                          loc.getString("CTL_VersioningView_Column_Status_Title"), 
                                          loc.getString("CTL_VersioningView_Column_Status_Desc")});
        columnLabels.put(SyncFileNode.COLUMN_NAME_PATH, new String [] { 
                                          loc.getString("CTL_VersioningView_Column_Path_Title"), 
                                          loc.getString("CTL_VersioningView_Column_Path_Desc")});
        columnLabels.put(SyncFileNode.COLUMN_NAME_RULE, new String [] { 
                                          loc.getString("CTL_VersioningView_Column_Rule_Title"), 
                                          loc.getString("CTL_VersioningView_Column_Rule_Desc")});
    }

    private static final Comparator NodeComparator = new Comparator() {
        public int compare(Object o1, Object o2) {
            Node.Property p1 = (Node.Property) o1;
            Node.Property p2 = (Node.Property) o2;
            String sk1 = (String) p1.getValue("sortkey"); // NOI18N
            if (sk1 != null) {
                String sk2 = (String) p2.getValue("sortkey"); // NOI18N
                return sk1.compareToIgnoreCase(sk2);
            } else {
                try {
                    String s1 = (String) p1.getValue();
                    String s2 = (String) p2.getValue();
                    return s1.compareToIgnoreCase(s2);
                } catch (Exception e) {
                    Clearcase.LOG.log(Level.SEVERE, null, e);
                    return 0;
                }
            }
        }
    };
    
    public SyncTable() {
        tableModel = new NodeTableModel();
        sorter = new TableSorter(tableModel);
        sorter.setColumnComparator(Node.Property.class, NodeComparator);
        table = new SortedTable(sorter);
        table.setRowHeight(table.getRowHeight() * 6 / 5);
        component = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        component.getViewport().setBackground(table.getBackground());
        Color borderColor = UIManager.getColor("scrollpane_border"); // NOI18N
        if (borderColor == null) borderColor = UIManager.getColor("controlShadow"); // NOI18N
        component.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, borderColor));
        table.addMouseListener(this);
        table.setDefaultRenderer(Node.Property.class, new SyncTableCellRenderer());
        table.getSelectionModel().addListSelectionListener(this);
        table.addAncestorListener(this);
        table.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SyncTable.class, "ACSN_VersioningTable")); // NOI18N
        table.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SyncTable.class, "ACSD_VersioningTable")); // NOI18N
        setColumns(new String[] {
            SyncFileNode.COLUMN_NAME_NAME,
            SyncFileNode.COLUMN_NAME_STATUS,
            SyncFileNode.COLUMN_NAME_PATH,
            SyncFileNode.COLUMN_NAME_RULE}
        );
        table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT ).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_F10, KeyEvent.SHIFT_DOWN_MASK ), "org.openide.actions.PopupAction");
  
        table.getActionMap().put("org.openide.actions.PopupAction", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                showPopup(org.netbeans.modules.versioning.util.Utils.getPositionForPopup(table));
            }
        });
        
    }

    void setDefaultColumnSizes() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                int width = table.getWidth();
                if (tableColumns.length == 4) {
                    for (int i = 0; i < tableColumns.length; i++) {
                        if (SyncFileNode.COLUMN_NAME_PATH.equals(tableColumns[i])) {
                            table.getColumnModel().getColumn(i).setPreferredWidth(width * 55 / 100);
                        } else {
                            table.getColumnModel().getColumn(i).setPreferredWidth(width * 15 / 100);
                        }
                    }
                }
            }
        });
    }

    public void ancestorAdded(AncestorEvent event) {
        setDefaultColumnSizes();
    }

    public void ancestorMoved(AncestorEvent event) {
    }

    public void ancestorRemoved(AncestorEvent event) {
    }

    public SyncFileNode [] getDisplayedNodes() {
        int n = sorter.getRowCount();
        SyncFileNode [] ret = new SyncFileNode[n];
        for (int i = 0; i < n; i++) {
            ret[i] = nodes[sorter.modelIndex(i)]; 
        }
        return ret;
    }

    public JComponent getComponent() {
        return component;
    }
    
    /**
     * Sets visible columns in the Versioning table.
     * 
     * @param columns array of column names, they must be one of SyncFileNode.COLUMN_NAME_XXXXX constants.  
     */ 
    final void setColumns(String [] columns) {
        if (Arrays.equals(columns, tableColumns)) return;
        setModelProperties(columns);
        tableColumns = columns;
        for (int i = 0; i < tableColumns.length; i++) {
            sorter.setColumnComparator(i, null);
            sorter.setSortingStatus(i, TableSorter.NOT_SORTED);
            if (SyncFileNode.COLUMN_NAME_STATUS.equals(tableColumns[i])) {
                sorter.setSortingStatus(i, TableSorter.ASCENDING);
                break;
            }
        }
        setDefaultColumnSizes();        
    }
        
    private void setModelProperties(String [] columns) {
        Node.Property [] properties = new Node.Property[columns.length];
        for (int i = 0; i < columns.length; i++) {
            String column = columns[i];
            String [] labels = (String[]) columnLabels.get(column);
            properties[i] = new ColumnDescriptor(column, String.class, labels[0], labels[1]);  
        }
        tableModel.setProperties(properties);
    }

    void setTableModel(SyncFileNode [] nodes) {
        this.nodes = nodes;
        tableModel.setNodes(nodes);
    }

    void focus() {
        table.requestFocus();
    }

    private static class ColumnDescriptor extends ReadOnly {
        
        public ColumnDescriptor(String name, Class type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription);
        }

        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            return null;
        }
    }

    private void showPopup(final MouseEvent e) {
        int row = table.rowAtPoint(e.getPoint());
        if (row != -1) {
            boolean makeRowSelected = true;
            int [] selectedrows = table.getSelectedRows();

            for (int i = 0; i < selectedrows.length; i++) {
                if (row == selectedrows[i]) {
                    makeRowSelected = false;
                    break;
                }
            }
            if (makeRowSelected) {
                table.getSelectionModel().setSelectionInterval(row, row);
            }
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // invoke later so the selection on the table will be set first
                JPopupMenu menu = getPopup();         
                menu.show(table, e.getX(), e.getY());
            }
        });
    }

    private void showPopup(Point p) {
        JPopupMenu menu = getPopup();
        menu.show(table, p.x, p.y);
    }
    
    /**
     * Constructs contextual Menu:
     */
    private JPopupMenu getPopup() {
        File[] selectedFiles = getSelectedFiles();
        VCSContext selectedContext = VCSContext.forNodes(TopComponent.getRegistry().getActivatedNodes());
                
        JPopupMenu menu = new JPopupMenu();
        JMenuItem item;
        
        item = menu.add(new OpenInEditorAction(selectedFiles));
        Mnemonics.setLocalizedText(item, item.getText());
        menu.addSeparator();
    
        item = menu.add(new CheckoutAction(selectedContext));
        Mnemonics.setLocalizedText(item, item.getText());
        item = menu.add(new ReserveAction(selectedContext));
        Mnemonics.setLocalizedText(item, item.getText());
        item = menu.add(new AddAction(NbBundle.getMessage(SyncTable.class, "Popup_AddAction_Name"), selectedContext)); //NOI18N
        Mnemonics.setLocalizedText(item, item.getText());
        menu.addSeparator();
        
        item = menu.add(new DiffAction(NbBundle.getMessage(SyncTable.class, "Popup_DiffAction_Name"), selectedContext)); //NOI18N
        Mnemonics.setLocalizedText(item, item.getText());
        item = menu.add(new UpdateAction(NbBundle.getMessage(SyncTable.class, "Popup_UpdateAction_Name"), selectedContext)); //NOI18N
        Mnemonics.setLocalizedText(item, item.getText());
        item = menu.add(new CheckinAction(NbBundle.getMessage(SyncTable.class, "Popup_CheckinAction_Name"), selectedContext)); //NOI18N
        Mnemonics.setLocalizedText(item, item.getText());
        
        menu.addSeparator();
        item = menu.add(new AnnotateAction(selectedContext, Clearcase.getInstance().getAnnotationsProvider(selectedContext)));
        Mnemonics.setLocalizedText(item, item.getText());
        
        item = menu.add(new BrowseHistoryAction(NbBundle.getMessage(SyncTable.class, "Popup_BrowseHistoryAction_Name"), selectedContext)); //NOI18N
        Mnemonics.setLocalizedText(item, item.getText());
        item = menu.add(new BrowseVersionTreeAction(NbBundle.getMessage(SyncTable.class, "Popup_BrowseVersionTree_Name"), selectedContext)); //NOI18N
        Mnemonics.setLocalizedText(item, item.getText());
    
        menu.addSeparator();
        item = menu.add(new IgnoreAction(selectedContext));
        Mnemonics.setLocalizedText(item, item.getText());        
        item = menu.add(new ExcludeAction(selectedContext));      
        Mnemonics.setLocalizedText(item, item.getText());        
        
        menu.addSeparator();
        item = menu.add(new ShowPropertiesAction(NbBundle.getMessage(SyncTable.class, "Popup_ShowPropertiesAction_Name"), selectedContext)); //NOI18N
        Mnemonics.setLocalizedText(item, item.getText());
        
        return menu;
    }
    
    private File [] getSelectedFiles() {
        Node [] selectedNodes = TopComponent.getRegistry().getActivatedNodes();
        File [] files = VCSContext.forNodes(selectedNodes).getFiles().toArray(new File[selectedNodes.length]);
        return files;
    }
    
    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
            showPopup(e);
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            showPopup(e);
        }
    }

    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e) && MouseUtils.isDoubleClick(e)) {
            int row = table.rowAtPoint(e.getPoint());
            if (row == -1) return;
            row = sorter.modelIndex(row);
            Action action = nodes[row].getPreferredAction();
            if (action == null || !action.isEnabled()) action = new OpenInEditorAction(getSelectedFiles());
            if (action.isEnabled()) {
                action.actionPerformed(new ActionEvent(this, 0, "")); // NOI18N
            }
        } 
    }

    public void valueChanged(ListSelectionEvent e) {
        List<SyncFileNode> selectedNodes = new ArrayList<SyncFileNode>();
        ListSelectionModel selectionModel = table.getSelectionModel();
        final TopComponent tc = (TopComponent) SwingUtilities.getAncestorOfClass(TopComponent.class,  table);
        if (tc == null) return; // table is no longer in component hierarchy
        
        int min = selectionModel.getMinSelectionIndex();
        if (min != -1) {
            int max = selectionModel.getMaxSelectionIndex();
            for (int i = min; i <= max; i++) {
                if (selectionModel.isSelectedIndex(i)) {
                    int idx = sorter.modelIndex(i);
                    selectedNodes.add(nodes[idx]);
                }
            }
        }
        // this method may be called outside of AWT if a node fires change events from some other thread, see #79174
        final Node [] nodes = selectedNodes.toArray(new Node[selectedNodes.size()]);
        if (SwingUtilities.isEventDispatchThread()) {
            tc.setActivatedNodes(nodes);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    tc.setActivatedNodes(nodes);
                }
            });
        }
    }
    
    private class SyncTableCellRenderer extends DefaultTableCellRenderer {
        
        private FilePathCellRenderer pathRenderer = new FilePathCellRenderer();
        
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component renderer;
            int modelColumnIndex = table.convertColumnIndexToModel(column);
            if (modelColumnIndex == 0) {
                SyncFileNode node = nodes[sorter.modelIndex(row)];
                if (!isSelected) {
                    value = "<html>" + node.getHtmlDisplayName(); // NOI18N
                }
                if (ClearcaseModuleConfig.isExcludedFromCommit(node.getFile().getAbsolutePath())) {
                    String nodeName = node.getDisplayName();
                    if (isSelected) {
                        value = "<html><s>" + nodeName + "</s></html>"; // NOI18N
                    } else {
                        value = "<html><s>" + Clearcase.getInstance().getAnnotator().annotateNameHtml(nodeName, node.getInfo(), null) + "</s>"; // NOI18N
                    }
                }
            }
            if (modelColumnIndex == 2) {
                renderer = pathRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            } else {
                renderer = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
            if (renderer instanceof JComponent) {
                String path = nodes[sorter.modelIndex(row)].getFile().getAbsolutePath(); 
                ((JComponent) renderer).setToolTipText(path);
            }
            return renderer;
        }
    }
}
