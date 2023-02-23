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

package org.netbeans.lib.profiler.ui.jdbc;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.swing.Box;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.RowFilter;
import javax.swing.SortOrder;
import javax.swing.tree.TreeNode;
import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.results.CCTNode;
import org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNode;
import org.netbeans.lib.profiler.results.jdbc.JdbcCCTProvider;
import org.netbeans.lib.profiler.results.jdbc.JdbcResultsSnapshot;
import org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode;
import org.netbeans.lib.profiler.ui.swing.ExportUtils;
import org.netbeans.lib.profiler.ui.swing.PopupButton;
import org.netbeans.lib.profiler.ui.swing.ProfilerTable;
import org.netbeans.lib.profiler.ui.swing.ProfilerTableContainer;
import org.netbeans.lib.profiler.ui.swing.ProfilerTreeTable;
import org.netbeans.lib.profiler.ui.swing.ProfilerTreeTableModel;
import org.netbeans.lib.profiler.ui.swing.SearchUtils;
import org.netbeans.lib.profiler.ui.swing.renderer.HideableBarRenderer;
import org.netbeans.lib.profiler.ui.swing.renderer.LabelRenderer;
import org.netbeans.lib.profiler.ui.swing.renderer.McsTimeRenderer;
import org.netbeans.lib.profiler.ui.swing.renderer.NumberPercentRenderer;
import org.netbeans.lib.profiler.ui.swing.renderer.NumberRenderer;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.api.icons.ProfilerIcons;

/**
 *
 * @author Jiri Sedlacek
 */
abstract class JDBCTreeTableView extends JDBCView {
    
    private Set<String> commands;
    private Set<String> tables;
    
    private JDBCTreeTableModel treeTableModel;
    private ProfilerTreeTable treeTable;
    
    private JPanel bottomPanel;
    private SQLFilterPanel sqlFilter;
    
    private JdbcResultsSnapshot currentData;
    
    private boolean searchQueries = true;
    private boolean searchCallerMethods = false;
    
    
    public JDBCTreeTableView(Set<ClientUtils.SourceCodeSelection> selection, boolean reverse) {
        initUI();
    }
    
    
    void setData(final JdbcResultsSnapshot newData, final Map<Integer, ClientUtils.SourceCodeSelection> newIdMap, final int aggregation, final Collection<Integer> selectedThreads, final boolean mergeThreads, final boolean _sampled, final boolean diff) {
        
        String[] _names = newData.getSelectNames();
        long[] _nTotalAllocObjects = newData.getInvocationsPerSelectId();
        long[] _totalAllocObjectsSize = newData.getTimePerSelectId();
        
        List<PresoObjAllocCCTNode> nodes = new ArrayList<>();
        
        long totalObjects = 0;
        long _totalObjects = 0;
        long totalBytes = 0;
        long _totalBytes = 0;
        
        if (commands == null) commands = new HashSet();
        commands.clear();
        if (tables == null) tables = new HashSet();
        tables.clear();
        
        for (int i = 1; i < _names.length; i++) {
            if (diff) {
                totalObjects = Math.max(totalObjects, _nTotalAllocObjects[i]);
                _totalObjects = Math.min(_totalObjects, _nTotalAllocObjects[i]);
                totalBytes = Math.max(totalBytes, _totalAllocObjectsSize[i]);
                _totalBytes = Math.min(_totalBytes, _totalAllocObjectsSize[i]);
            } else {
                totalObjects += _nTotalAllocObjects[i];
                totalBytes += _totalAllocObjectsSize[i];
            }
            
            int statementType = newData.getTypeForSelectId()[i];
            int commandType = newData.getCommandTypeForSelectId()[i];
            String commandString = commandString(commandType);
            String[] sqlTables = newData.getTablesForSelectId()[i];
            
            commands.add(commandString.toUpperCase(Locale.ENGLISH));
            tables.addAll(Arrays.asList(sqlTables));
            
            if (sqlFilter.passes(_names[i], commandString, sqlTables, statementType)) {
                final int _i = i;
                nodes.add(new SQLQueryNode(_names[i], _nTotalAllocObjects[i], _totalAllocObjectsSize[i], statementType, commandType, sqlTables) {
                    PresoObjAllocCCTNode computeChildren() { return newData.createPresentationCCT(_i, false); }
                });
            }
        }
        
        long __totalBytes = !diff ? totalBytes : Math.max(Math.abs(totalBytes), Math.abs(_totalBytes));
        long __totalObjects = !diff ? totalObjects : Math.max(Math.abs(totalObjects), Math.abs(_totalObjects));
        
        renderers[0].setMaxValue(__totalBytes);
        renderers[1].setMaxValue(__totalObjects);
        renderers[0].setDiffMode(diff);
        renderers[1].setDiffMode(diff);
        treeTableModel.setRoot(PresoObjAllocCCTNode.rootNode(nodes.toArray(new PresoObjAllocCCTNode[0])));

        currentData = newData;
    }
    
    public void resetData() {
        renderers[0].setMaxValue(0);
        renderers[1].setMaxValue(0);
        renderers[0].setDiffMode(false);
        renderers[1].setDiffMode(false);

        treeTableModel.setRoot(PresoObjAllocCCTNode.rootNode(new PresoObjAllocCCTNode[0]));

        if (commands != null) commands.clear();
        if (tables != null) tables.clear();

        currentData = null;
    }
    
    
    public void showSelectionColumn() {
        treeTable.setColumnVisibility(0, true);
    }
    
    public void refreshSelection() {
        treeTableModel.dataChanged();
    }
    
    
    ExportUtils.ExportProvider[] getExportProviders() {
        final String name = EXPORT_QUERIES;
        return treeTable.getRowCount() == 0 ? null : new ExportUtils.ExportProvider[] {
            new ExportUtils.CSVExportProvider(treeTable),
            new ExportUtils.HTMLExportProvider(treeTable, name),
            new ExportUtils.XMLExportProvider(treeTable, name),
            new ExportUtils.PNGExportProvider(treeTable)
        };
    }
    
    
    protected abstract void populatePopup(JPopupMenu popup, Object value, ClientUtils.SourceCodeSelection userValue);
    
    protected void popupShowing() {};
    
    protected void popupHidden()  {};
    
    
    private HideableBarRenderer[] renderers;
    
    private void initUI() {
        treeTableModel = new JDBCTreeTableModel(PrestimeCPUCCTNode.EMPTY);
        
        treeTable = new ProfilerTreeTable(treeTableModel, true, true, new int[] { 0 }) {
            public ClientUtils.SourceCodeSelection getUserValueForRow(int row) {
                return JDBCTreeTableView.this.getUserValueForRow(row);
            }
            protected void populatePopup(JPopupMenu popup, Object value, Object userValue) {
                JDBCTreeTableView.this.populatePopup(popup, value, (ClientUtils.SourceCodeSelection)userValue);
            }
            protected void popupShowing() {
                JDBCTreeTableView.this.popupShowing();
            }
            protected void popupHidden() {
                JDBCTreeTableView.this.popupHidden();
            }
        };
        
        setToolTips();
        
        treeTable.providePopupMenu(true);
        installDefaultAction();
        
        treeTable.setRootVisible(false);
        treeTable.setShowsRootHandles(true);
        treeTable.makeTreeAutoExpandable(2);
        
        treeTable.setMainColumn(0);
        treeTable.setFitWidthColumn(0);
        
        treeTable.setSortColumn(1);
        treeTable.setDefaultSortOrder(1, SortOrder.DESCENDING);
        
        renderers = new HideableBarRenderer[2];
        renderers[0] = new HideableBarRenderer(new NumberPercentRenderer(new McsTimeRenderer()));
        renderers[1] = new HideableBarRenderer(new NumberRenderer());
        
        long refTime = 123456;
        renderers[0].setMaxValue(refTime);
        renderers[1].setMaxValue(refTime);
        
        treeTable.setTreeCellRenderer(new JDBCJavaNameRenderer());
        treeTable.setColumnRenderer(1, renderers[0]);
        treeTable.setColumnRenderer(2, renderers[1]);
        
        treeTable.setDefaultColumnWidth(1, renderers[0].getOptimalWidth());
        treeTable.setDefaultColumnWidth(2, renderers[1].getMaxNoBarWidth());
        
        // Debug columns
        LabelRenderer lr = new LabelRenderer();
        lr.setHorizontalAlignment(LabelRenderer.TRAILING);
        lr.setValue("XStatement TypeX", -1);
        
        treeTable.setColumnRenderer(3, lr);
        treeTable.setDefaultSortOrder(3, SortOrder.ASCENDING);
        treeTable.setDefaultColumnWidth(3, lr.getPreferredSize().width);
        treeTable.setColumnVisibility(3, false);
        
        treeTable.setColumnRenderer(4, lr);
        treeTable.setDefaultSortOrder(4, SortOrder.ASCENDING);
        treeTable.setDefaultColumnWidth(4, lr.getPreferredSize().width);
        treeTable.setColumnVisibility(4, false);
        
        treeTable.setColumnRenderer(5, lr);
        treeTable.setDefaultSortOrder(5, SortOrder.ASCENDING);
        treeTable.setDefaultColumnWidth(5, lr.getPreferredSize().width);
        treeTable.setColumnVisibility(5, false);
        
        ProfilerTableContainer tableContainer = new ProfilerTableContainer(treeTable, false, null);
        
        setLayout(new BorderLayout());
        add(tableContainer, BorderLayout.CENTER);
        
        sqlFilter = new SQLFilterPanel() {
            Set<String> getCommands() {
                if (commands == null) commands = new HashSet();
                return commands;
            }
            
            Set<String> getTables() {
                if (tables == null) tables = new HashSet();
                return tables;
            }
            
            void applyFilter() {
                if (currentData != null) setData(currentData, null, -1, null, false, false, false);
            }
        };
        
        bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.add(sqlFilter, BorderLayout.NORTH);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    protected void addFilterFindPanel(JComponent comp) {
        bottomPanel.add(comp, BorderLayout.SOUTH);
    }
    
    private void setToolTips() {
        treeTable.setColumnToolTips(new String[] {
                                        NAME_COLUMN_TOOLTIP,
                                        TOTAL_TIME_COLUMN_TOOLTIP,
                                        INVOCATIONS_COLUMN_TOOLTIP,
                                        COMMANDS_COLUMN_TOOLTIP,
                                        TABLES_COLUMN_TOOLTIP,
                                        STATEMENTS_COLUMN_TOOLTIP
                                    });
    }
    
    
    protected RowFilter getExcludesFilter() {
        return new RowFilter() { // Do not filter SQL commands
            public boolean include(RowFilter.Entry entry) {
                PresoObjAllocCCTNode node = (PresoObjAllocCCTNode)entry.getIdentifier();
                return isSQL(node);
            }
        };
    }
    
    protected SearchUtils.TreeHelper getSearchHelper() {
        return new SearchUtils.TreeHelper() {
            public int getNodeType(TreeNode tnode) {
                PresoObjAllocCCTNode node = (PresoObjAllocCCTNode)tnode;
                CCTNode parent = node.getParent();
                if (parent == null) return SearchUtils.TreeHelper.NODE_SKIP_DOWN; // invisible root
                
                if (isSQL(node)) {
                    if (searchQueries) {
                        return searchCallerMethods ? SearchUtils.TreeHelper.NODE_SEARCH_DOWN :
                                                     SearchUtils.TreeHelper.NODE_SEARCH_NEXT;
                    } else {
                        return searchCallerMethods ? SearchUtils.TreeHelper.NODE_SKIP_DOWN :
                                                     SearchUtils.TreeHelper.NODE_SKIP_NEXT;
                    }
                }
                
                return searchCallerMethods ?
                       SearchUtils.TreeHelper.NODE_SEARCH_DOWN :
                       SearchUtils.TreeHelper.NODE_SKIP_NEXT;
            }
        };
    }
    
    protected Component[] getSearchOptions() {
        PopupButton pb = new PopupButton (Icons.getIcon(ProfilerIcons.TAB_CALL_TREE)) {
            protected void populatePopup(JPopupMenu popup) {
                popup.add(new JCheckBoxMenuItem(SEARCH_QUERIES_SCOPE, searchQueries) {
                    {
                        if (!searchCallerMethods) setEnabled(false);
                    }
                    protected void fireActionPerformed(ActionEvent e) {
                        super.fireActionPerformed(e);
                        searchQueries = !searchQueries;
                    }
                });
                popup.add(new JCheckBoxMenuItem(SEARCH_CALLERS_SCOPE, searchCallerMethods) {
                    {
                        if (!searchQueries) setEnabled(false);
                    }
                    protected void fireActionPerformed(ActionEvent e) {
                        super.fireActionPerformed(e);
                        searchCallerMethods = !searchCallerMethods;
                    }
                });
            }
        };
        pb.setToolTipText(SEARCH_SCOPE_TOOLTIP);
        return new Component[] { Box.createHorizontalStrut(5), pb };
    }
    
    protected ProfilerTable getResultsComponent() {
        return treeTable;
    }
    
    
    protected ClientUtils.SourceCodeSelection getUserValueForRow(int row) {
        PresoObjAllocCCTNode node = (PresoObjAllocCCTNode)treeTable.getValueForRow(row);
        if (node == null || isSQL(node) || node.isFiltered()) return null;
        String[] name = node.getMethodClassNameAndSig();
        return new ClientUtils.SourceCodeSelection(name[0], name[1], name[2]);
    }
    
    static boolean isSQL(PresoObjAllocCCTNode node) {
        return node instanceof SQLQueryNode;
    }
    
    static boolean isSelectable(PresoObjAllocCCTNode node) {
        if (isSQL(node)) return false;
        String methodName = node.getMethodClassNameAndSig()[1];
        if (methodName == null || methodName.endsWith("[native]")) return false; // NOI18N
        return true;
    }
    
    
    private class JDBCTreeTableModel extends ProfilerTreeTableModel.Abstract {
        
        JDBCTreeTableModel(TreeNode root) {
            super(root);
        }
        
        public String getColumnName(int columnIndex) {
            if (columnIndex == 0) {
                return COLUMN_NAME;
            } else if (columnIndex == 1) {
                return COLUMN_TOTALTIME;
            } else if (columnIndex == 2) {
                return COLUMN_INVOCATIONS;
            }  else if (columnIndex == 3) {
                return COLUMN_COMMANDS;
            } else if (columnIndex == 4) {
                return COLUMN_TABLES;
            } else if (columnIndex == 5) {
                return COLUMN_STATEMENTS;
            }
            return null;
        }

        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0) {
                return JTree.class;
            } else if (columnIndex == 1) {
                return Long.class;
            } else if (columnIndex == 2) {
                return Integer.class;
            } else if (columnIndex == 3) {
                return String.class;
            } else if (columnIndex == 4) {
                return String.class;
            } else if (columnIndex == 5) {
                return String.class;
            }
            return Long.class;
        }

        public int getColumnCount() {
            return 6;
        }

        public Object getValueAt(TreeNode node, int columnIndex) {
            PresoObjAllocCCTNode jdbcNode = (PresoObjAllocCCTNode)node;
            if (columnIndex == 0) {
                return jdbcNode;
            } else if (columnIndex == 1) {
                return jdbcNode.totalObjSize;
            } else if (columnIndex == 2) {
                return jdbcNode.nCalls;
            } else if (columnIndex == 3) {
                if (jdbcNode instanceof SQLQueryNode) {
                    return commandString(((SQLQueryNode)jdbcNode).getCommandType());
                } else {
                    return "-"; // NOI18N
                }
                
            } else if (columnIndex == 4) {
                if (jdbcNode instanceof SQLQueryNode) {
                    return formatTables(((SQLQueryNode)jdbcNode).getTables());
                } else {
                    return "-"; // NOI18N
                }
            } else if (columnIndex == 5) {
                if (jdbcNode instanceof SQLQueryNode) {
                    switch (((SQLQueryNode)jdbcNode).getStatementType()) {
                        case JdbcCCTProvider.SQL_PREPARED_STATEMENT: return STATEMENT_PREPARED;
                        case JdbcCCTProvider.SQL_CALLABLE_STATEMENT: return STATEMENT_CALLABLE;
                        default: return STATEMENT_REGULAR;
                    }
                } else {
                    return "-";
                }
            }
            return null;
        }
        
        public void setValueAt(Object aValue, TreeNode node, int columnIndex) {}

        public boolean isCellEditable(TreeNode node, int columnIndex) {
            return false;
        }
        
        private String formatTables(String[] tables) {
            int count = tables.length - 1;
            if (count == -1) return "-"; // NOI18N
            
            StringBuilder b = new StringBuilder();
            for (int i = 0; ; i++) {
                b.append(tables[i]);
                if (i == count) return b.toString();
                b.append(", "); // NOI18N
            }
        }
        
    }
    
    abstract class SQLQueryNode extends PresoObjAllocCCTNode {
        String htmlName;
        private final int statementType;
        private final int commandType;
        private final String[] tables;
        SQLQueryNode(String className, long nTotalAllocObjects, long totalAllocObjectsSize, int statementType, int commandType, String[] tables) {
            super(className, nTotalAllocObjects, totalAllocObjectsSize);
            this.statementType = statementType;
            this.commandType = commandType;
            this.tables = tables;
        }
        public CCTNode[] getChildren() {
            if (children == null) {
                PresoObjAllocCCTNode root = computeChildren();
                setChildren(root == null ? new PresoObjAllocCCTNode[0] :
                            (PresoObjAllocCCTNode[])root.getChildren());
            }
            return children;
        }
        public boolean isLeaf() {
            if (children == null) return /*includeEmpty ? nCalls == 0 :*/ false;
            else return super.isLeaf();
        }   
        public int getChildCount() {
            if (children == null) getChildren();
            return super.getChildCount();
        }
        abstract PresoObjAllocCCTNode computeChildren();
        int getStatementType() { return statementType; }
        int getCommandType() { return commandType; }
        String[] getTables() { return tables; }
    }
    
}
