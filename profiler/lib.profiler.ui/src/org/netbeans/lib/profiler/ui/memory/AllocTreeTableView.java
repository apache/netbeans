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
package org.netbeans.lib.profiler.ui.memory;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.RowFilter;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.filters.GenericFilter;
import org.netbeans.lib.profiler.results.CCTNode;
import org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNode;
import org.netbeans.lib.profiler.results.memory.AllocMemoryResultsDiff;
import org.netbeans.lib.profiler.results.memory.AllocMemoryResultsSnapshot;
import org.netbeans.lib.profiler.results.memory.MemoryCCTManager;
import org.netbeans.lib.profiler.results.memory.MemoryResultsSnapshot;
import org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode;
import org.netbeans.lib.profiler.ui.Formatters;
import static org.netbeans.lib.profiler.ui.memory.MemoryView.SEARCH_CLASSES_SCOPE;
import org.netbeans.lib.profiler.ui.swing.ExportUtils;
import org.netbeans.lib.profiler.ui.swing.PopupButton;
import org.netbeans.lib.profiler.ui.swing.ProfilerTable;
import org.netbeans.lib.profiler.ui.swing.ProfilerTableContainer;
import org.netbeans.lib.profiler.ui.swing.ProfilerTreeTable;
import org.netbeans.lib.profiler.ui.swing.ProfilerTreeTableModel;
import org.netbeans.lib.profiler.ui.swing.SearchUtils;
import org.netbeans.lib.profiler.ui.swing.renderer.CheckBoxRenderer;
import org.netbeans.lib.profiler.ui.swing.renderer.HideableBarRenderer;
import org.netbeans.lib.profiler.ui.swing.renderer.NumberPercentRenderer;
import org.netbeans.lib.profiler.utils.Wildcards;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.api.icons.ProfilerIcons;

/**
 *
 * @author Jiri Sedlacek
 */
abstract class AllocTreeTableView extends MemoryView {
    
    private AllocTreeTableModel treeTableModel;
    private ProfilerTreeTable treeTable;
    
    private Map<TreeNode, ClientUtils.SourceCodeSelection> nodesMap;
    private final Set<ClientUtils.SourceCodeSelection> selection;
    
    private boolean filterObjects = true;
    private boolean filterAllocations = false;
    private boolean searchObjects = true;
    private boolean searchAllocations = false;
    
    
    public AllocTreeTableView(Set<ClientUtils.SourceCodeSelection> selection) {
        this.selection = selection;
        
        initUI();
    }
    
    
    protected RowFilter getExcludesFilter() {
        return new RowFilter() { // Do not filter first level nodes
            public boolean include(RowFilter.Entry entry) {
                PresoObjAllocCCTNode node = (PresoObjAllocCCTNode)entry.getIdentifier();
                CCTNode parent = node.getParent();
                if (parent == null) return true;
                if (parent.getParent() == null) return !filterObjects;
                return !filterAllocations;
            }
        };
    }
    
    protected Component[] getFilterOptions() {
        PopupButton pb = new PopupButton (Icons.getIcon(ProfilerIcons.TAB_CALL_TREE)) {
            protected void populatePopup(JPopupMenu popup) {
                popup.add(new JCheckBoxMenuItem(FILTER_CLASSES_SCOPE, filterObjects) {
                    {
                        if (!filterAllocations) setEnabled(false);
                    }
                    protected void fireActionPerformed(ActionEvent e) {
                        super.fireActionPerformed(e);
                        filterObjects = !filterObjects;
                        enableFilter();
                    }
                });
                popup.add(new JCheckBoxMenuItem(FILTER_ALLOCATIONS_SCOPE, filterAllocations) {
                    {
                        if (!filterObjects) setEnabled(false);
                    }
                    protected void fireActionPerformed(ActionEvent e) {
                        super.fireActionPerformed(e);
                        filterAllocations = !filterAllocations;
                        enableFilter();
                    }
                });
            }
        };
        pb.setToolTipText(FILTER_SCOPE_TOOLTIP);
        return new Component[] { Box.createHorizontalStrut(5), pb };
    }
    
    protected SearchUtils.TreeHelper getSearchHelper() {
        return new SearchUtils.TreeHelper() {
            public int getNodeType(TreeNode tnode) {
                PresoObjAllocCCTNode node = (PresoObjAllocCCTNode)tnode;
                CCTNode parent = node.getParent();
                if (parent == null) return SearchUtils.TreeHelper.NODE_SKIP_DOWN; // invisible root
                
                if (parent.getParent() == null) {
                    if (searchObjects) {
                        return searchAllocations ? SearchUtils.TreeHelper.NODE_SEARCH_DOWN :
                                                   SearchUtils.TreeHelper.NODE_SEARCH_NEXT;
                    } else {
                        return searchAllocations ? SearchUtils.TreeHelper.NODE_SKIP_DOWN :
                                                   SearchUtils.TreeHelper.NODE_SKIP_NEXT;
                    }
                }
                
                return searchAllocations ? SearchUtils.TreeHelper.NODE_SEARCH_DOWN :
                                           SearchUtils.TreeHelper.NODE_SKIP_NEXT;
            }
        };
    }
    
    protected Component[] getSearchOptions() {
        PopupButton pb = new PopupButton (Icons.getIcon(ProfilerIcons.TAB_CALL_TREE)) {
            protected void populatePopup(JPopupMenu popup) {
                popup.add(new JCheckBoxMenuItem(SEARCH_CLASSES_SCOPE, searchObjects) {
                    {
                        if (!searchAllocations) setEnabled(false);
                    }
                    protected void fireActionPerformed(ActionEvent e) {
                        super.fireActionPerformed(e);
                        searchObjects = !searchObjects;
                    }
                });
                popup.add(new JCheckBoxMenuItem(SEARCH_ALLOCATIONS_SCOPE, searchAllocations) {
                    {
                        if (!searchObjects) setEnabled(false);
                    }
                    protected void fireActionPerformed(ActionEvent e) {
                        super.fireActionPerformed(e);
                        searchAllocations = !searchAllocations;
                    }
                });
            }
        };
        pb.setToolTipText(SEARCH_SCOPE_TOOLTIP);
        return new Component[] { Box.createHorizontalStrut(5), pb };
    }
    
    protected ProfilerTable getResultsComponent() { return treeTable; }
    
    
    public void setData(MemoryResultsSnapshot snapshot, GenericFilter filter, int aggregation) {
        final boolean includeEmpty = filter != null;
        final boolean diff = snapshot instanceof AllocMemoryResultsDiff;
        final AllocMemoryResultsSnapshot _snapshot = (AllocMemoryResultsSnapshot)snapshot;
        
        String[] _classNames = _snapshot.getClassNames();
        int[] _nTotalAllocObjects = _snapshot.getObjectsCounts();
        long[] _totalAllocObjectsSize = _snapshot.getObjectsSizePerClass();
        
        int _nTrackedItems = Math.min(_snapshot.getNProfiledClasses(), _classNames.length);
        _nTrackedItems = Math.min(_nTrackedItems, _nTotalAllocObjects.length);
        
        List<PresoObjAllocCCTNode> nodes = new ArrayList<>();
        final Map<TreeNode, ClientUtils.SourceCodeSelection> _nodesMap = new HashMap<>();
        
        long totalObjects = 0;
        long _totalObjects = 0;
        long totalBytes = 0;
        long _totalBytes = 0;
        
        for (int i = 0; i < _nTrackedItems; i++) {
            if (diff) {
                totalObjects = Math.max(totalObjects, _nTotalAllocObjects[i]);
                _totalObjects = Math.min(_totalObjects, _nTotalAllocObjects[i]);
                totalBytes = Math.max(totalBytes, _totalAllocObjectsSize[i]);
                _totalBytes = Math.min(_totalBytes, _totalAllocObjectsSize[i]);
            } else {
                totalObjects += _nTotalAllocObjects[i];
                totalBytes += _totalAllocObjectsSize[i];
            }
            
            final int _i = i;
            
            class Node extends PresoObjAllocCCTNode {
                Node(String className, int nTotalAllocObjects, long totalAllocObjectsSize) {
                    super(className, nTotalAllocObjects, totalAllocObjectsSize);
                }
                public CCTNode[] getChildren() {
                    if (children == null) {
                        MemoryCCTManager callGraphManager = new MemoryCCTManager(_snapshot, _i, true);
                        PresoObjAllocCCTNode root = callGraphManager.getRootNode();
                        setChildren(root == null ? new PresoObjAllocCCTNode[0] :
                                    (PresoObjAllocCCTNode[])root.getChildren());
                    }
                    return children;
                }
                public boolean isLeaf() {
                    if (children == null) return includeEmpty ? nCalls == 0 : false;
                    else return super.isLeaf();
                }   
                public int getChildCount() {
                    if (children == null) getChildren();
                    return super.getChildCount();
                }
            }
            
            if (!includeEmpty) { // old snapshot
                if (_nTotalAllocObjects[i] > 0) {
                    PresoObjAllocCCTNode node = new Node(_classNames[i], _nTotalAllocObjects[i], _totalAllocObjectsSize[i]);
                    nodes.add(node);
                    _nodesMap.put(node, new ClientUtils.SourceCodeSelection(_classNames[i], Wildcards.ALLWILDCARD, null));
                }
            } else if (filter.passes(_classNames[i].replace('.', '/'))) { // NOI18N
                PresoObjAllocCCTNode node = new Node(_classNames[i], _nTotalAllocObjects[i], _totalAllocObjectsSize[i]);
                nodes.add(node);
                _nodesMap.put(node, new ClientUtils.SourceCodeSelection(_classNames[i], Wildcards.ALLWILDCARD, null));
            }
        }
        
        final long __totalBytes = !diff ? totalBytes :
                Math.max(Math.abs(totalBytes), Math.abs(_totalBytes));
        final long __totalObjects = !diff ? totalObjects :
                Math.max(Math.abs(totalObjects), Math.abs(_totalObjects));
        final PresoObjAllocCCTNode root = PresoObjAllocCCTNode.rootNode(nodes.toArray(new PresoObjAllocCCTNode[0]));
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                nodesMap = _nodesMap;
                renderers[0].setMaxValue(__totalBytes);
                renderers[1].setMaxValue(__totalObjects);
                renderers[0].setDiffMode(diff);
                renderers[1].setDiffMode(diff);
                treeTableModel.setRoot(root);
            }
        });
    }
    
    public void resetData() {
        final PresoObjAllocCCTNode root = PresoObjAllocCCTNode.rootNode(new PresoObjAllocCCTNode[0]);
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                nodesMap = null;
                
                renderers[0].setMaxValue(0);
                renderers[1].setMaxValue(0);
                renderers[0].setDiffMode(false);
                renderers[1].setDiffMode(false);
                
                treeTableModel.setRoot(root);
            }
        });
    }
    
    
    public void showSelectionColumn() {
        treeTable.setColumnVisibility(0, true);
    }
    
    public void refreshSelection() {
        treeTableModel.dataChanged();
    }
    
    
    public ExportUtils.ExportProvider[] getExportProviders() {
        return treeTable.getRowCount() == 0 ? null : new ExportUtils.ExportProvider[] {
            new ExportUtils.CSVExportProvider(treeTable),
            new ExportUtils.HTMLExportProvider(treeTable, EXPORT_ALLOCATED),
            new ExportUtils.XMLExportProvider(treeTable, EXPORT_ALLOCATED),
            new ExportUtils.PNGExportProvider(treeTable)
        };
    }
    
    
    protected abstract void performDefaultAction(ClientUtils.SourceCodeSelection userValue);
    
    protected abstract void populatePopup(JPopupMenu popup, Object value, ClientUtils.SourceCodeSelection userValue);
    
    protected void popupShowing() {};
    
    protected void popupHidden()  {};
    
    
    private HideableBarRenderer[] renderers;
    
    private void initUI() {
        final int offset = selection == null ? -1 : 0;
        
        treeTableModel = new AllocTreeTableModel(PrestimeCPUCCTNode.EMPTY);
        
        treeTable = new ProfilerTreeTable(treeTableModel, true, true, new int[] { 1 + offset }) {
            public ClientUtils.SourceCodeSelection getUserValueForRow(int row) {
                return AllocTreeTableView.this.getUserValueForRow(row);
            }
            protected void populatePopup(JPopupMenu popup, Object value, Object userValue) {
                AllocTreeTableView.this.populatePopup(popup, value, (ClientUtils.SourceCodeSelection)userValue);
            }
            protected void popupShowing() {
                AllocTreeTableView.this.popupShowing();
            }
            protected void popupHidden() {
                AllocTreeTableView.this.popupHidden();
            }
        };
        
        treeTable.setColumnToolTips(selection == null ? new String[] {
                                        NAME_COLUMN_TOOLTIP,
                                        ALLOC_SIZE_COLUMN_TOOLTIP,
                                        ALLOC_COUNT_COLUMN_TOOLTIP
                                      } : new String[] {
                                        SELECTED_COLUMN_TOOLTIP,
                                        NAME_COLUMN_TOOLTIP,
                                        ALLOC_SIZE_COLUMN_TOOLTIP,
                                        ALLOC_COUNT_COLUMN_TOOLTIP
                                      });
        
        treeTable.providePopupMenu(true);
        treeTable.setDefaultAction(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                int row = treeTable.getSelectedRow();
                ClientUtils.SourceCodeSelection userValue = getUserValueForRow(row);
                if (userValue != null) performDefaultAction(userValue);
            }
        });
        
        treeTable.setRootVisible(false);
        treeTable.setShowsRootHandles(true);
        treeTable.makeTreeAutoExpandable(2);
        
        treeTable.setMainColumn(1 + offset);
        treeTable.setFitWidthColumn(1 + offset);
        
        treeTable.setSortColumn(2 + offset);
        treeTable.setDefaultSortOrder(1 + offset, SortOrder.ASCENDING);
        
        if (selection != null) treeTable.setColumnVisibility(0, false);
        
        renderers = new HideableBarRenderer[2];
        renderers[0] = new HideableBarRenderer(new NumberPercentRenderer(Formatters.bytesFormat()));
        renderers[1] = new HideableBarRenderer(new NumberPercentRenderer());
        
        renderers[0].setMaxValue(123456789);
        renderers[1].setMaxValue(12345678);
        
        if (selection != null) treeTable.setColumnRenderer(0, new CheckBoxRenderer() {
            private boolean visible;
            public void setValue(Object value, int row) {
                TreePath path = treeTable.getPathForRow(row);
                visible = nodesMap.containsKey((TreeNode)path.getLastPathComponent());
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
        treeTable.setTreeCellRenderer(new MemoryJavaNameRenderer());
        treeTable.setColumnRenderer(2 + offset, renderers[0]);
        treeTable.setColumnRenderer(3 + offset, renderers[1]);
        
        if (selection != null) {
            int w = new JLabel(treeTable.getColumnName(0)).getPreferredSize().width;
            treeTable.setDefaultColumnWidth(0, w + 15);
        }
        treeTable.setDefaultColumnWidth(2 + offset, renderers[0].getOptimalWidth());
        treeTable.setDefaultColumnWidth(3 + offset, renderers[1].getMaxNoBarWidth());
        
        ProfilerTableContainer tableContainer = new ProfilerTableContainer(treeTable, false, null);
        
        setLayout(new BorderLayout());
        add(tableContainer, BorderLayout.CENTER);
    }
    
    protected ClientUtils.SourceCodeSelection getUserValueForRow(int row) {
        PresoObjAllocCCTNode node = (PresoObjAllocCCTNode)treeTable.getValueForRow(row);
        if (node == null || node.isFiltered()) return null;
        String[] name = node.getMethodClassNameAndSig();
        return new ClientUtils.SourceCodeSelection(name[0], name[1], name[2]);
    }
    
    
    private class AllocTreeTableModel extends ProfilerTreeTableModel.Abstract {
        
        AllocTreeTableModel(TreeNode root) {
            super(root);
        }
        
        public String getColumnName(int columnIndex) {
            if (selection == null) columnIndex++;
            
            if (columnIndex == 1) {
                return COLUMN_NAME;
            } else if (columnIndex == 2) {
                return COLUMN_ALLOCATED_BYTES;
            } else if (columnIndex == 3) {
                return COLUMN_ALLOCATED_OBJECTS;
            } else if (columnIndex == 0) {
                return COLUMN_SELECTED;
            }
            return null;
        }

        public Class<?> getColumnClass(int columnIndex) {
            if (selection == null) columnIndex++;
            
            if (columnIndex == 1) {
                return JTree.class;
            } else if (columnIndex == 2) {
                return Long.class;
            } else if (columnIndex == 3) {
                return Integer.class;
            } else if (columnIndex == 0) {
                return Boolean.class;
            }
            return null;
        }

        public int getColumnCount() {
            return selection == null ? 3 : 4;
        }

        public Object getValueAt(TreeNode node, int columnIndex) {
            PresoObjAllocCCTNode allocNode = (PresoObjAllocCCTNode)node;
            
            if (selection == null) columnIndex++;
            
            if (columnIndex == 1) {
                return allocNode.getNodeName();
            } else if (columnIndex == 2) {
                return allocNode.totalObjSize;
            } else if (columnIndex == 3) {
                return allocNode.nCalls;
            } else if (columnIndex == 0) {
                if (selection.isEmpty()) return Boolean.FALSE;
                return selection.contains(nodesMap.get(node));
            }

            return null;
        }
        
        public void setValueAt(Object aValue, TreeNode node, int columnIndex) {
            if (selection == null) columnIndex++;
            
            if (columnIndex == 0) {
                if (Boolean.TRUE.equals(aValue)) selection.add(nodesMap.get(node));
                else selection.remove(nodesMap.get(node));
            }
        }

        public boolean isCellEditable(TreeNode node, int columnIndex) {
            if (selection == null) columnIndex++;
            if (columnIndex != 0) return false;
            return (nodesMap.containsKey(node));
        }
        
    }
    
}
