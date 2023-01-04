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

package org.netbeans.lib.profiler.ui.cpu;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.swing.Box;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.RowFilter;
import javax.swing.SortOrder;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.results.CCTNode;
import org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot;
import org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNode;
import org.netbeans.lib.profiler.ui.swing.ExportUtils;
import org.netbeans.lib.profiler.ui.swing.PopupButton;
import org.netbeans.lib.profiler.ui.swing.ProfilerTable;
import org.netbeans.lib.profiler.ui.swing.ProfilerTableContainer;
import org.netbeans.lib.profiler.ui.swing.ProfilerTreeTable;
import org.netbeans.lib.profiler.ui.swing.ProfilerTreeTableModel;
import org.netbeans.lib.profiler.ui.swing.SearchUtils;
import org.netbeans.lib.profiler.ui.swing.renderer.CheckBoxRenderer;
import org.netbeans.lib.profiler.ui.swing.renderer.HideableBarRenderer;
import org.netbeans.lib.profiler.ui.swing.renderer.McsTimeRenderer;
import org.netbeans.lib.profiler.ui.swing.renderer.NumberPercentRenderer;
import org.netbeans.lib.profiler.ui.swing.renderer.NumberRenderer;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.api.icons.ProfilerIcons;

/**
 *
 * @author Jiri Sedlacek
 */
abstract class CPUTreeTableView extends CPUView {
    
    private CPUTreeTableModel treeTableModel;
    private ProfilerTreeTable treeTable;
    
    private Map<Integer, ClientUtils.SourceCodeSelection> idMap;
    private final Set<ClientUtils.SourceCodeSelection> selection;
    
    private final boolean reverse;
    
    private boolean sampled = true;
    private boolean twoTimeStamps;
    
    private boolean hitsVisible = false;
    private boolean invocationsVisible = true;
    
    private boolean filterTopMethods = true;
    private boolean filterCallerMethods = false;
    private boolean searchTopMethods = true;
    private boolean searchCallerMethods = false;
    
    
    public CPUTreeTableView(Set<ClientUtils.SourceCodeSelection> selection, boolean reverse) {
        this.selection = selection;
        this.reverse = reverse;
        
        initUI();
    }
    
    
    void setData(final CPUResultsSnapshot newData, final Map<Integer, ClientUtils.SourceCodeSelection> newIdMap, final int aggregation, final Collection<Integer> selectedThreads, final boolean mergeThreads, final boolean _sampled, final boolean _diff) {
        boolean structureChange = sampled != _sampled;
        sampled = _sampled;
        boolean _twoTimeStamps = twoTimeStamps;
        twoTimeStamps = newData == null ? !_twoTimeStamps : newData.isCollectingTwoTimeStamps();
        idMap = newIdMap;
        renderers[0].setDiffMode(_diff);
        renderers[1].setDiffMode(_diff);
        renderers[2].setDiffMode(_diff);
        if (treeTableModel != null) {
            treeTableModel.setRoot(newData == null ? PrestimeCPUCCTNode.EMPTY :
                                   !reverse ? newData.getRootNode(aggregation, selectedThreads, mergeThreads):
                                   newData.getReverseRootNode(aggregation, selectedThreads, mergeThreads));
        }
        if (structureChange) {
            // Resolve Hits/Invocations column
            int col = treeTable.convertColumnIndexToView(selection == null ? 3 : 4);
            String colN = treeTableModel.getColumnName(selection == null ? 3 : 4);

            // Persist current Hits/Invocations column visibility
            if (sampled) invocationsVisible = treeTable.isColumnVisible(col);
            else hitsVisible = treeTable.isColumnVisible(col);

            // Update Hits/Invocations column name
            treeTable.getColumnModel().getColumn(col).setHeaderValue(colN);

            // Set new Hits/Invocations column visibility
            treeTable.setColumnVisibility(col, sampled ? hitsVisible : invocationsVisible);

            setToolTips();

            repaint();
        }

        if (newData != null && twoTimeStamps != _twoTimeStamps) {
            int column = selection == null ? 2 : 3;
            boolean cpuColumnVisible = treeTable.isColumnVisible(column);
            if (twoTimeStamps && !cpuColumnVisible) treeTable.setColumnVisibility(column, true);
            else if (!twoTimeStamps && cpuColumnVisible) treeTable.setColumnVisibility(column, false);
        }
    }
    
    public void resetData() {
        setData(null, null, -1, null, false, sampled, false);
    }
    
    
    public void showSelectionColumn() {
        treeTable.setColumnVisibility(0, true);
    }
    
    public void refreshSelection() {
        treeTableModel.dataChanged();
    }
    
    
    ExportUtils.ExportProvider[] getExportProviders() {
        final String name = reverse ? EXPORT_REVERSE_CALLS : EXPORT_FORWARD_CALLS;
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
        treeTableModel = new CPUTreeTableModel(PrestimeCPUCCTNode.EMPTY);
        
        int offset = selection == null ? -1 : 0;
        
        treeTable = new ProfilerTreeTable(treeTableModel, true, true, new int[] { 1 + offset }) {
            public ClientUtils.SourceCodeSelection getUserValueForRow(int row) {
                return CPUTreeTableView.this.getUserValueForRow(row);
            }
            protected void populatePopup(JPopupMenu popup, Object value, Object userValue) {
                CPUTreeTableView.this.populatePopup(popup, value, (ClientUtils.SourceCodeSelection)userValue);
            }
            protected void popupShowing() {
                CPUTreeTableView.this.popupShowing();
            }
            protected void popupHidden() {
                CPUTreeTableView.this.popupHidden();
            }
        };
        
        setToolTips();
        
        treeTable.providePopupMenu(true);
        installDefaultAction();
        
        treeTable.setRootVisible(false);
        treeTable.setShowsRootHandles(true);
        treeTable.makeTreeAutoExpandable(2);
        
        treeTable.setMainColumn(1 + offset);
        treeTable.setFitWidthColumn(1 + offset);
        
        treeTable.setSortColumn(2 + offset);
        treeTable.setDefaultSortOrder(1 + offset, SortOrder.ASCENDING);
        
        if (selection != null) treeTable.setColumnVisibility(0, false);
        treeTable.setColumnVisibility(4 + offset, false);
        
        renderers = new HideableBarRenderer[3];
        
        renderers[0] = new HideableBarRenderer(new NumberPercentRenderer(new McsTimeRenderer())) {
            public void setValue(Object value, int row) {
                super.setMaxValue(getMaxValue(row, 0));
                super.setValue(value, row);
            }
        };
        renderers[1] = new HideableBarRenderer(new NumberPercentRenderer(new McsTimeRenderer())) {
            public void setValue(Object value, int row) {
                super.setMaxValue(getMaxValue(row, 1));
                super.setValue(value, row);
            }
        };
        renderers[2] = new HideableBarRenderer(new NumberRenderer()) {
            public void setValue(Object value, int row) {
                super.setMaxValue(getMaxValue(row, 2));
                super.setValue(value, row);
            }
        };
        
        long refTime = 123456;
        renderers[0].setMaxValue(refTime);
        renderers[1].setMaxValue(refTime);
        renderers[2].setMaxValue(refTime);
        
        if (selection != null) treeTable.setColumnRenderer(0, new CheckBoxRenderer() {
            private boolean visible;
            public void setValue(Object value, int row) {
                TreePath path = treeTable.getPathForRow(row);
                visible = isSelectable((PrestimeCPUCCTNode)path.getLastPathComponent());
                if (visible) super.setValue(value, row);
            }
            public void paint(Graphics g) {
                if (visible) {
                    super.paint(g);
                } else {
                    g.setColor(getBackground());
                    g.fillRect(0, 0, size.width, size.height);
                }
            }
        });
        treeTable.setTreeCellRenderer(new CPUJavaNameRenderer(reverse ? ProfilerIcons.NODE_REVERSE : ProfilerIcons.NODE_FORWARD));
        treeTable.setColumnRenderer(2 + offset, renderers[0]);
        treeTable.setColumnRenderer(3 + offset, renderers[1]);
        treeTable.setColumnRenderer(4 + offset, renderers[2]);
        
        int w;
        if (selection != null) {
            w = new JLabel(treeTable.getColumnName(0)).getPreferredSize().width;
            treeTable.setDefaultColumnWidth(0, w + 15);
        }
        treeTable.setDefaultColumnWidth(2 + offset, renderers[0].getOptimalWidth());
        treeTable.setDefaultColumnWidth(3 + offset, renderers[1].getMaxNoBarWidth());
        
        sampled = !sampled;
        w = new JLabel(treeTable.getColumnName(4 + offset)).getPreferredSize().width;
        sampled = !sampled;
        w = Math.max(w, new JLabel(treeTable.getColumnName(4 + offset)).getPreferredSize().width);
        treeTable.setDefaultColumnWidth(4 + offset, Math.max(renderers[2].getNoBarWidth(), w + 15));
        
        ProfilerTableContainer tableContainer = new ProfilerTableContainer(treeTable, false, null);
        
        setLayout(new BorderLayout());
        add(tableContainer, BorderLayout.CENTER);
    }
    
    private void setToolTips() {
        treeTable.setColumnToolTips(selection == null ? new String[] {
                                        NAME_COLUMN_TOOLTIP,
                                        TOTAL_TIME_COLUMN_TOOLTIP,
                                        TOTAL_TIME_CPU_COLUMN_TOOLTIP,
                                        sampled ? HITS_COLUMN_TOOLTIP :
                                                  INVOCATIONS_COLUMN_TOOLTIP
                                      } : new String[] {
                                        SELECTED_COLUMN_TOOLTIP,
                                        NAME_COLUMN_TOOLTIP,
                                        TOTAL_TIME_COLUMN_TOOLTIP,
                                        TOTAL_TIME_CPU_COLUMN_TOOLTIP,
                                        sampled ? HITS_COLUMN_TOOLTIP :
                                                  INVOCATIONS_COLUMN_TOOLTIP
                                      });
    }
    
    
    protected RowFilter getExcludesFilter() {
        if (!reverse) return new RowFilter() { // Do not filter threads and self time nodes
            public boolean include(RowFilter.Entry entry) {
                PrestimeCPUCCTNode node = (PrestimeCPUCCTNode)entry.getIdentifier();
                return node.isThreadNode() || node.isSelfTimeNode();
            }
        }; else return new RowFilter() { // Do not filter threads and self time nodes
            public boolean include(RowFilter.Entry entry) {
                PrestimeCPUCCTNode node = (PrestimeCPUCCTNode)entry.getIdentifier();
                CCTNode parent = node.getParent();
                if (parent == null) return true; // invisible root
                
                if (node.isThreadNode() || node.isSelfTimeNode()) return true; // thread or self time node
                
                if (((PrestimeCPUCCTNode)parent).isThreadNode() || // toplevel method node (children of thread)
                    parent.getParent() == null) // toplevel method node (merged threads)
                        return !filterTopMethods;
                
                return !filterCallerMethods; // reverse call tree node
            }
        };
    }
    
    protected Component[] getFilterOptions() {
        if (!reverse) return super.getFilterOptions();
        
        PopupButton pb = new PopupButton (Icons.getIcon(ProfilerIcons.TAB_CALL_TREE)) {
            protected void populatePopup(JPopupMenu popup) {
                popup.add(new JCheckBoxMenuItem(FILTER_CALLEES_SCOPE, filterTopMethods) {
                    {
                        if (!filterCallerMethods) setEnabled(false);
                    }

                    @Override
                    protected void fireActionPerformed(ActionEvent e) {
                        super.fireActionPerformed(e);
                        filterTopMethods = !filterTopMethods;
                        enableFilter();
                    }
                });
                popup.add(new JCheckBoxMenuItem(FILTER_CALLERS_SCOPE, filterCallerMethods) {
                    {
                        if (!filterTopMethods) setEnabled(false);
                    }

                    @Override
                    protected void fireActionPerformed(ActionEvent e) {
                        super.fireActionPerformed(e);
                        filterCallerMethods = !filterCallerMethods;
                        enableFilter();
                    }
                });
            }
        };
        pb.setToolTipText(FILTER_SCOPE_TOOLTIP);
        return new Component[] { Box.createHorizontalStrut(5), pb };
    }
    
    protected SearchUtils.TreeHelper getSearchHelper() {
        if (!reverse) return super.getSearchHelper();
        
        return new SearchUtils.TreeHelper() {
            public int getNodeType(TreeNode tnode) {
                PrestimeCPUCCTNode node = (PrestimeCPUCCTNode)tnode;
                CCTNode parent = node.getParent();
                if (parent == null) return SearchUtils.TreeHelper.NODE_SKIP_DOWN; // invisible root
                
                if (node.isThreadNode()) return SearchUtils.TreeHelper.NODE_SKIP_DOWN; // thread node
                if (node.isSelfTimeNode()) return SearchUtils.TreeHelper.NODE_SKIP_NEXT; // self time node
                
                if (((PrestimeCPUCCTNode)parent).isThreadNode() || // toplevel method node (children of thread)
                    parent.getParent() == null) {                  // toplevel method node (merged threads)
                    if (searchTopMethods) {
                        return searchCallerMethods ? SearchUtils.TreeHelper.NODE_SEARCH_DOWN :
                                                     SearchUtils.TreeHelper.NODE_SEARCH_NEXT;
                    } else {
                        return searchCallerMethods ? SearchUtils.TreeHelper.NODE_SKIP_DOWN :
                                                     SearchUtils.TreeHelper.NODE_SKIP_NEXT;
                    }
                }
                
                return searchCallerMethods ? // reverse call tree node
                       SearchUtils.TreeHelper.NODE_SEARCH_DOWN :
                       SearchUtils.TreeHelper.NODE_SKIP_NEXT;
            }
        };
    }
    
    protected Component[] getSearchOptions() {
        if (!reverse) return super.getSearchOptions();
        
        PopupButton pb = new PopupButton (Icons.getIcon(ProfilerIcons.TAB_CALL_TREE)) {
            protected void populatePopup(JPopupMenu popup) {
                popup.add(new JCheckBoxMenuItem(SEARCH_CALLEES_SCOPE, searchTopMethods) {
                    {
                        if (!searchCallerMethods) setEnabled(false);
                    }

                    @Override
                    protected void fireActionPerformed(ActionEvent e) {
                        super.fireActionPerformed(e);
                        searchTopMethods = !searchTopMethods;
                    }
                });
                popup.add(new JCheckBoxMenuItem(SEARCH_CALLERS_SCOPE, searchCallerMethods) {
                    {
                        if (!searchTopMethods) setEnabled(false);
                    }

                    @Override
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
    
    
    private long getMaxValue(int row, int val) {
        TreePath path = treeTable.getPathForRow(row);
        if (path == null) return Long.MIN_VALUE; // TODO: prevents NPE from export but doesn't provide the actual value!
        if (path.getPathCount() < 2) return 1;
        
        PrestimeCPUCCTNode node = (PrestimeCPUCCTNode)path.getPathComponent(1);
        if (val == 0) return Math.abs(node.getTotalTime0());
        else if (val == 1) return Math.abs(node.getTotalTime1());
        else return Math.abs(node.getNCalls());
    }
    
    protected ClientUtils.SourceCodeSelection getUserValueForRow(int row) {
        PrestimeCPUCCTNode node = (PrestimeCPUCCTNode)treeTable.getValueForRow(row);
        if (node == null) return null;
        else if (node.isThreadNode() || node.isFiltered() || node.isSelfTimeNode()) return null;
//        else return selectionForId(node.getMethodId());
        else return idMap.get(node.getMethodId());
    }
    
//    private ClientUtils.SourceCodeSelection selectionForId(int methodId) {
//        ProfilingSessionStatus sessionStatus = client.getStatus();
//        sessionStatus.beginTrans(false);
//        try {
//            String className = sessionStatus.getInstrMethodClasses()[methodId];
//            String methodName = sessionStatus.getInstrMethodNames()[methodId];
//            String methodSig = sessionStatus.getInstrMethodSignatures()[methodId];
//            return new ClientUtils.SourceCodeSelection(className, methodName, methodSig);
//        } finally {
//            sessionStatus.endTrans();
//        }
//    }
    
    private static boolean isSelectable(PrestimeCPUCCTNode node) {
        if (node.isThreadNode() || node.isFiltered() || node.isSelfTimeNode()) return false;
        if (node.getMethodClassNameAndSig()[1].endsWith("[native]")) return false; // NOI18N
        return true;
    }
    
    
    private class CPUTreeTableModel extends ProfilerTreeTableModel.Abstract {
        
        CPUTreeTableModel(TreeNode root) {
            super(root);
        }
        
        public String getColumnName(int columnIndex) {
            if (selection == null) columnIndex++;
            
            if (columnIndex == 1) {
                return COLUMN_NAME;
            } else if (columnIndex == 2) {
                return COLUMN_TOTALTIME;
            } else if (columnIndex == 3) {
                return COLUMN_TOTALTIME_CPU;
            } else if (columnIndex == 4) {
                return sampled ? COLUMN_HITS : COLUMN_INVOCATIONS;
            } else if (columnIndex == 0) {
                return COLUMN_SELECTED;
            }
            return null;
        }

        public Class<?> getColumnClass(int columnIndex) {
            if (selection == null) columnIndex++;
            
            if (columnIndex == 1) {
                return JTree.class;
            } else if (columnIndex == 4) {
                return Integer.class;
            } else if (columnIndex == 0) {
                return Boolean.class;
            } else {
                return Long.class;
            }
        }

        public int getColumnCount() {
            return selection == null ? 4 : 5;
        }

        public Object getValueAt(TreeNode node, int columnIndex) {
            PrestimeCPUCCTNode cpuNode = (PrestimeCPUCCTNode)node;
            
            if (selection == null) columnIndex++;
            
            if (columnIndex == 1) {
                return cpuNode.getNodeName();
            } else if (columnIndex == 2) {
                return cpuNode.getTotalTime0();
            } else if (columnIndex == 3) {
                return twoTimeStamps ? cpuNode.getTotalTime1() : 0;
            } else if (columnIndex == 4) {
                return cpuNode.getNCalls();
            } else if (columnIndex == 0) {
                if (selection.isEmpty()) return Boolean.FALSE;
                return selection.contains(idMap.get(cpuNode.getMethodId()));
            }

            return null;
        }
        
        public void setValueAt(Object aValue, TreeNode node, int columnIndex) {
            if (selection == null) columnIndex++;
            
            if (columnIndex == 0) {
                PrestimeCPUCCTNode cpuNode = (PrestimeCPUCCTNode)node;
                int methodId = cpuNode.getMethodId();
                if (Boolean.TRUE.equals(aValue)) selection.add(idMap.get(methodId));
                else selection.remove(idMap.get(methodId));
            }
        }

        public boolean isCellEditable(TreeNode node, int columnIndex) {
            if (selection == null) columnIndex++;
            if (columnIndex != 0) return false;
            return (isSelectable((PrestimeCPUCCTNode)node));
        }
        
    }
    
}
