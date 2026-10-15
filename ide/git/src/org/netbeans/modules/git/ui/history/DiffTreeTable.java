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
package org.netbeans.modules.git.ui.history;

import java.awt.Component;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;
import org.openide.ErrorManager;

import javax.swing.*;
import java.util.*;
import java.beans.PropertyVetoException;
import java.io.CharConversionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.netbeans.swing.etable.ETableColumnModel;
import org.openide.explorer.view.OutlineView;
import org.openide.xml.XMLUtil;

/**
 * Treetable to show results of Search History action.
 * 
 * @author Maros Sandor
 */
class DiffTreeTable extends OutlineView {
    
    private RevisionsRootNode rootNode;
    private List<RepositoryRevision> results;
    private final SearchHistoryPanel master;

    @NbBundle.Messages("LBL_DiffView.TreeColumnLabel=Revision")
    public DiffTreeTable(SearchHistoryPanel master) {
        super(Bundle.LBL_DiffView_TreeColumnLabel());
        this.master = master;
        getOutline().setShowHorizontalLines(true);
        getOutline().setShowVerticalLines(false);
        getOutline().setRootVisible(false);
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        setupColumns();
    }
    
    @SuppressWarnings("unchecked")
    private void setupColumns() {
        ResourceBundle loc = NbBundle.getBundle(DiffTreeTable.class);
        setPropertyColumns(RevisionNode.COLUMN_NAME_PATH, loc.getString("LBL_DiffTree_Column_Path"),
                RevisionNode.COLUMN_NAME_DATE, loc.getString("LBL_DiffTree_Column_Time"),
                RevisionNode.COLUMN_NAME_USERNAME, loc.getString("LBL_DiffTree_Column_Username"),
                RevisionNode.COLUMN_NAME_MESSAGE, loc.getString("LBL_DiffTree_Column_Message"));
        setPropertyColumnDescription(RevisionNode.COLUMN_NAME_PATH, loc.getString("LBL_DiffTree_Column_Path_Desc"));
        setPropertyColumnDescription(RevisionNode.COLUMN_NAME_DATE, loc.getString("LBL_DiffTree_Column_Time_Desc"));
        setPropertyColumnDescription(RevisionNode.COLUMN_NAME_USERNAME, loc.getString("LBL_DiffTree_Column_Username_Desc"));
        setPropertyColumnDescription(RevisionNode.COLUMN_NAME_MESSAGE, loc.getString("LBL_DiffTree_Column_Message_Desc"));
        TableColumn msgColumn = getOutline().getColumn(loc.getString("LBL_DiffTree_Column_Message"));
        msgColumn.setCellRenderer(new MessageRenderer(getOutline().getCellRenderer(0, msgColumn.getModelIndex())));
        TableColumnModel model = getOutline().getColumnModel();
        if (model instanceof ETableColumnModel) {
            ((ETableColumnModel) model).setColumnHidden(model.getColumn(1), true);
        }
        setDefaultColumnSizes();
    }
    
    private void setDefaultColumnSizes() {
        SwingUtilities.invokeLater(() -> {
            if (getOutline().getColumnCount() == 4) {
                int width1 = getWidth();
                getOutline().getColumnModel().getColumn(0).setPreferredWidth(width1 * 25 / 100);
                getOutline().getColumnModel().getColumn(1).setPreferredWidth(width1 * 15 / 100);
                getOutline().getColumnModel().getColumn(2).setPreferredWidth(width1 * 10 / 100);
                getOutline().getColumnModel().getColumn(3).setPreferredWidth(width1 * 50 / 100);
            }
        });
    }

    void setSelection(int idx) {
        getOutline().getSelectionModel().setValueIsAdjusting(false);
        getOutline().scrollRectToVisible(getOutline().getCellRect(idx, 1, true));
        getOutline().getSelectionModel().setSelectionInterval(idx, idx);
    }

    void setSelection(RepositoryRevision container) {
        RevisionNode node = (RevisionNode) getNode(rootNode, container);
        if (node == null) return;
        ExplorerManager em = ExplorerManager.find(this);
        try {
            em.setSelectedNodes(new Node [] { node });
        } catch (PropertyVetoException e) {
            ErrorManager.getDefault().notify(e);
        }
    }

    void setSelection (RepositoryRevision.Event... events) {
        List<Node> nodes = new ArrayList<>(events.length);
        for (RepositoryRevision.Event event : events) {
            RevisionNode node = (RevisionNode) getNode(rootNode, event);
            if (node != null) {
                nodes.add(node);
            }
        }
        ExplorerManager em = ExplorerManager.find(this);
        try {
            em.setSelectedNodes(nodes.toArray(Node[]::new));
        } catch (PropertyVetoException e) {
            ErrorManager.getDefault().notify(e);
        }
    }

    private Node getNode(Node node, Object obj) {
        Object object = node.getLookup().lookup(obj.getClass());
        if (obj.equals(object)) return node;
        Enumeration children = node.getChildren().nodes();
        while (children.hasMoreElements()) {
            Node child = (Node) children.nextElement();
            Node result = getNode(child, obj);
            if (result != null) return result;
        }
        return null;
    }

    public int [] getSelection() {
        return getOutline().getSelectedRows();
    }

    public int getRowCount() {
        return getOutline().getRowCount();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        ExplorerManager em = ExplorerManager.find(this);
        em.setRootContext(rootNode);
    }

    public void setResults(List<RepositoryRevision> results) {
        this.results = results;
        rootNode = new RevisionsRootNode();
        ExplorerManager em = ExplorerManager.find(this);
        if (em != null) {
            em.setRootContext(rootNode);
        }
    }

    public void refreshResults (List<RepositoryRevision> results) {
        this.results = results;
        ((RevisionsRootNodeChildren) rootNode.getChildren()).refreshKeys();
    }

    private static class MessageRenderer implements TableCellRenderer {
        private final TableCellRenderer delegate;
        private final Map<String, String> tooltips = new HashMap<>();

        public MessageRenderer (TableCellRenderer delegate) {
            this.delegate = delegate;
        }

        @Override
        public Component getTableCellRendererComponent (JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component comp = delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (comp instanceof JComponent) {
                JComponent c = (JComponent) comp;
                if (value == null) {
                    c.setToolTipText(null);
                } else {
                    String val = value.toString();
                    String tooltip = tooltips.get(val);
                    if (tooltip == null) {
                        tooltip = val.replace("\r\n", "\n").replace("\r", "\n"); //NOI18N
                        boolean highlighterActive = tooltip.startsWith("<html>");
                        if (!highlighterActive) {
                            try {
                                tooltip = XMLUtil.toElementContent(tooltip);
                            } catch (CharConversionException e1) {
                                Logger.getLogger(DiffTreeTable.class.getName()).log(Level.INFO, "Can not HTML escape: " + tooltip);  //NOI18N
                            }
                        }
                        if (tooltip.contains("\n")) {
                            tooltip = tooltip.replace("\n", "<br>");
                            if (!highlighterActive) {
                                tooltip = "<html><body><p>" + tooltip + "</p></body></html>"; //NOI18N
                            }
                            c.setToolTipText(tooltip);
                        }
                        tooltips.put(val, tooltip);
                    }
                    c.setToolTipText(tooltip);
                }
            }
            return comp;
        }
    }
    
    private class RevisionsRootNode extends AbstractNode {
    
        public RevisionsRootNode() {
            super(new RevisionsRootNodeChildren());
        }

        @Override
        public String getName() {
            return "revision"; // NOI18N
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(DiffTreeTable.class, "LBL_DiffTree_Column_Name"); // NOI18N
        }

        @Override
        public String getShortDescription() {
            return NbBundle.getMessage(DiffTreeTable.class, "LBL_DiffTree_Column_Name_Desc"); // NOI18N
        }
    }

    private class RevisionsRootNodeChildren extends Children.Keys<RepositoryRevision> {
    
        private RevisionsRootNodeChildren() {}

        @Override
        protected void addNotify() {
            refreshKeys();
        }
        
        @Override
        protected void removeNotify() {
            setKeys(Collections.emptySet());
        }
    
        private void refreshKeys() {
            setKeys(results);
            repaint();
        }
    
        @Override
        protected Node[] createNodes(RepositoryRevision key) {
            return new Node[] { new RevisionNode((RepositoryRevision) key, master) };
        }
    }
}
