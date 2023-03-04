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

package org.netbeans.lib.profiler.ui.cpu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.tree.TreeNode;
import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.results.CCTNode;
import org.netbeans.lib.profiler.results.cpu.CPUResultsDiff;
import org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot;
import org.netbeans.lib.profiler.results.cpu.FlatProfileContainer;
import org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNode;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.lib.profiler.ui.components.JExtendedSplitPane;
import org.netbeans.lib.profiler.ui.results.DataView;
import org.netbeans.lib.profiler.ui.swing.FilterUtils;
import org.netbeans.lib.profiler.ui.swing.ProfilerTable;
import org.netbeans.lib.profiler.ui.swing.ProfilerTreeTable;
import org.netbeans.lib.profiler.ui.swing.SearchUtils;
import org.netbeans.lib.profiler.utils.Wildcards;

/**
 *
 * @author Jiri Sedlacek
 */
public abstract class LiveCPUView extends JPanel {
    
    private CPUResultsSnapshot snapshot;
    private CPUResultsSnapshot refSnapshot;
    private boolean sampled;
    private boolean mergedThreads;
    private Collection<Integer> selectedThreads;
    
    private DataView lastFocused;
    private CPUTableView hotSpotsView;
    private CPUTreeTableView forwardCallsView;
    private CPUTreeTableView reverseCallsView;
    
    private ThreadsSelector threadsSelector;
    
    private long lastupdate;
    private volatile boolean refreshIsRunning;
    
    private ExecutorService executor;
    
    
    public LiveCPUView(Set<ClientUtils.SourceCodeSelection> selection) {
        initUI(selection);
        registerActions();
    }
    
    
    public void setView(boolean forwardCalls, boolean hotSpots, boolean reverseCalls) {
        forwardCallsView.setVisible(forwardCalls);
        hotSpotsView.setVisible(hotSpots);
        reverseCallsView.setVisible(reverseCalls);
    }
    
    public ThreadsSelector createThreadSelector() {
        threadsSelector = new ThreadsSelector() {
            protected CPUResultsSnapshot getSnapshot() { return snapshot; }
            protected void selectionChanged(Collection<Integer> selected, boolean mergeThreads) {
                mergedThreads = mergeThreads;
                selectedThreads = selected;
                setData();
            }
            void reset() {
                super.reset();
                mergedThreads = false;
                selectedThreads = null;
            }
        };
        return threadsSelector;
    }
    
    public boolean isRefreshRunning() {
        return refreshIsRunning;
    }
    
    public long getLastUpdate() {
        return lastupdate;
    }

    public void setData(final CPUResultsSnapshot snapshotData, final boolean sampledData) {
        if (refreshIsRunning) return;
        refreshIsRunning = true;
        
        UIUtils.runInEventDispatchThread(new Runnable() {
            public void run() {
                snapshot = snapshotData;
                sampled = sampledData;

                setData();
            }
        });
    }
    
    private void setData() {
        if (snapshot == null) {
            resetData();
            refreshIsRunning = false;
        } else {
            getExecutor().submit(new Runnable() {
                public void run() {
                    final CPUResultsSnapshot _snapshot = refSnapshot == null ? snapshot :
                                                         refSnapshot.createDiff(snapshot);

                    final FlatProfileContainer flatData = _snapshot.getFlatProfile(selectedThreads, CPUResultsSnapshot.METHOD_LEVEL_VIEW);

                    final Map<Integer, ClientUtils.SourceCodeSelection> idMap = _snapshot.getMethodIDMap(CPUResultsSnapshot.METHOD_LEVEL_VIEW);

                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            try {
                                boolean diff = _snapshot instanceof CPUResultsDiff;
                                forwardCallsView.setData(_snapshot, idMap, CPUResultsSnapshot.METHOD_LEVEL_VIEW, selectedThreads, mergedThreads, sampled, diff);
                                hotSpotsView.setData(flatData, idMap, sampled, diff);
                                reverseCallsView.setData(_snapshot, idMap, CPUResultsSnapshot.METHOD_LEVEL_VIEW, selectedThreads, mergedThreads, sampled, diff);
                            } finally {
                                refreshIsRunning = false;
                                lastupdate = System.currentTimeMillis();
                            }
                        }
                    });
                }
            });
        }
    }
    
    public boolean setDiffView(final boolean diff) {
        if (snapshot == null) return false;
        UIUtils.runInEventDispatchThread(new Runnable() {
            public void run() {
                refSnapshot = diff ? snapshot : null;
                setData();
            }
        });
        return true;
    }
    
    public void resetData() {
        UIUtils.runInEventDispatchThread(new Runnable() {
            public void run() {
                forwardCallsView.resetData();
                hotSpotsView.resetData();
                reverseCallsView.resetData();
                snapshot = null;
                refSnapshot = null;
                sampled = true;
                if (threadsSelector != null) threadsSelector.reset();
            }
        });
    }
    
    
    public void showSelectionColumn() {
        forwardCallsView.showSelectionColumn();
        hotSpotsView.showSelectionColumn();
        reverseCallsView.showSelectionColumn();
    }
    
    public void refreshSelection() {
        forwardCallsView.refreshSelection();
        hotSpotsView.refreshSelection();
        reverseCallsView.refreshSelection();
    }
    
    
    protected boolean profileMethodSupported() { return true; }
    
    protected boolean profileClassSupported() { return true; }
    
    
    protected abstract boolean showSourceSupported();
    
    protected abstract void showSource(ClientUtils.SourceCodeSelection value);
    
    protected abstract void selectForProfiling(ClientUtils.SourceCodeSelection value);
    
    protected void popupShowing() {};
    
    protected void popupHidden() {};
    
    
    protected void foundInForwardCalls() {}
    
    protected void foundInHotSpots() {}
    
    protected void foundInReverseCalls() {}
    
    
    private void profileMethod(ClientUtils.SourceCodeSelection value) {
        selectForProfiling(value);
    }
    
    private void profileClass(ClientUtils.SourceCodeSelection value) {
        selectForProfiling(new ClientUtils.SourceCodeSelection(
                           value.getClassName(), Wildcards.ALLWILDCARD, null));
    }
    
    
    private void initUI(Set<ClientUtils.SourceCodeSelection> selection) {
        setLayout(new BorderLayout(0, 0));
        
        forwardCallsView = new CPUTreeTableView(selection, false) {
            protected void performDefaultAction(ClientUtils.SourceCodeSelection userValue) {
                if (showSourceSupported()) showSource(userValue);
            }
            protected void populatePopup(JPopupMenu popup, Object value, ClientUtils.SourceCodeSelection userValue) {
                LiveCPUView.this.populatePopup(forwardCallsView, popup, value, userValue);
            }
            protected void popupShowing() { LiveCPUView.this.popupShowing(); }
            protected void popupHidden()  { LiveCPUView.this.popupHidden(); }
            protected boolean hasBottomFilterFindMargin() { return true; }
        };
        forwardCallsView.notifyOnFocus(new Runnable() {
            public void run() { lastFocused = forwardCallsView; }
        });
        
        hotSpotsView = new CPUTableView(selection) {
            protected void performDefaultAction(ClientUtils.SourceCodeSelection userValue) {
                if (showSourceSupported()) showSource(userValue);
            }
            protected void populatePopup(JPopupMenu popup, Object value, ClientUtils.SourceCodeSelection userValue) {
                LiveCPUView.this.populatePopup(hotSpotsView, popup, value, userValue);
            }
            protected void popupShowing() { LiveCPUView.this.popupShowing(); }
            protected void popupHidden()  { LiveCPUView.this.popupHidden(); }
            protected boolean hasBottomFilterFindMargin() { return true; }
        };
        hotSpotsView.notifyOnFocus(new Runnable() {
            public void run() { lastFocused = hotSpotsView; }
        });
        
        reverseCallsView = new CPUTreeTableView(selection, true) {
            protected void performDefaultAction(ClientUtils.SourceCodeSelection userValue) {
                if (showSourceSupported()) showSource(userValue);
            }
            protected void populatePopup(JPopupMenu popup, Object value, ClientUtils.SourceCodeSelection userValue) {
                LiveCPUView.this.populatePopup(reverseCallsView, popup, value, userValue);
            }
            protected void popupShowing() { LiveCPUView.this.popupShowing(); }
            protected void popupHidden()  { LiveCPUView.this.popupHidden(); }
            protected boolean hasBottomFilterFindMargin() { return true; }
        };
        reverseCallsView.notifyOnFocus(new Runnable() {
            public void run() { lastFocused = reverseCallsView; }
        });
        
        JSplitPane upperSplit = new JExtendedSplitPane(JSplitPane.VERTICAL_SPLIT) {
            {
                setBorder(null);
                setDividerSize(5);

                if (getUI() instanceof BasicSplitPaneUI) {
                    BasicSplitPaneDivider divider = ((BasicSplitPaneUI)getUI()).getDivider();
                    if (divider != null) {
                        Color c = UIUtils.isNimbus() || UIUtils.isAquaLookAndFeel() ?
                                  UIUtils.getDisabledLineColor() : new JSeparator().getForeground();
                        divider.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, c));
                    }
                }
            }
        };
        upperSplit.setBorder(BorderFactory.createEmptyBorder());
        upperSplit.setTopComponent(forwardCallsView);
        upperSplit.setBottomComponent(hotSpotsView);
        upperSplit.setDividerLocation(0.5d);
        upperSplit.setResizeWeight(0.5d);
        
        JSplitPane lowerSplit = new JExtendedSplitPane(JSplitPane.VERTICAL_SPLIT) {
            {
                setBorder(null);
                setDividerSize(5);

                if (getUI() instanceof BasicSplitPaneUI) {
                    BasicSplitPaneDivider divider = ((BasicSplitPaneUI)getUI()).getDivider();
                    if (divider != null) {
                        Color c = UIUtils.isNimbus() || UIUtils.isAquaLookAndFeel() ?
                                  UIUtils.getDisabledLineColor() : new JSeparator().getForeground();
                        divider.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, c));
                    }
                }
            }
        };
        lowerSplit.setBorder(BorderFactory.createEmptyBorder());
        lowerSplit.setTopComponent(upperSplit);
        lowerSplit.setBottomComponent(reverseCallsView);
        lowerSplit.setDividerLocation(0.66d);
        lowerSplit.setResizeWeight(0.66d);
        
        add(lowerSplit, BorderLayout.CENTER);
        
//        // TODO: read last state?
//        setView(true, false);
    }
    
    private void registerActions() {
        ActionMap map = getActionMap();
        
        map.put(FilterUtils.FILTER_ACTION_KEY, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                DataView active = getLastFocused();
                if (active != null) active.activateFilter();
            }
        });
        
        map.put(SearchUtils.FIND_ACTION_KEY, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                DataView active = getLastFocused();
                if (active != null) active.activateSearch();
            }
        });
    }
    
    private DataView getLastFocused() {
        if (lastFocused != null && !lastFocused.isShowing()) lastFocused = null;
        
        if (lastFocused == null) {
            if (forwardCallsView.isShowing()) lastFocused = forwardCallsView;
            else if (hotSpotsView.isShowing()) lastFocused = hotSpotsView;
            else if (reverseCallsView.isShowing()) lastFocused = reverseCallsView;
        }
        
        return lastFocused;
    }
    
    private void populatePopup(final DataView invoker, JPopupMenu popup, final Object value, final ClientUtils.SourceCodeSelection userValue) {
        if (showSourceSupported()) {
            popup.add(new JMenuItem(CPUView.ACTION_GOTOSOURCE) {
                { setEnabled(userValue != null); setFont(getFont().deriveFont(Font.BOLD)); }
                protected void fireActionPerformed(ActionEvent e) { showSource(userValue); }
            });
            popup.addSeparator();
        }
        
        if (profileMethodSupported()) popup.add(new JMenuItem(CPUView.ACTION_PROFILE_METHOD) {
            { setEnabled(userValue != null && CPUTableView.isSelectable(userValue)); }
            protected void fireActionPerformed(ActionEvent e) { profileMethod(userValue); }
        });
        
        if (profileClassSupported()) popup.add(new JMenuItem(CPUView.ACTION_PROFILE_CLASS) {
            { setEnabled(userValue != null); }
            protected void fireActionPerformed(ActionEvent e) { profileClass(userValue); }
        });
        
        if (profileMethodSupported() || profileClassSupported()) popup.addSeparator();
        
        JMenuItem[] customItems = invoker.createCustomMenuItems(this, value, userValue);
        if (customItems != null) {
            for (JMenuItem customItem : customItems) popup.add(customItem);
            popup.addSeparator();
        }
        
        customizeNodePopup(invoker, popup, value, userValue);
        
        if (invoker == forwardCallsView) {
            final ProfilerTreeTable ttable = (ProfilerTreeTable)forwardCallsView.getResultsComponent();
            int column = ttable.convertColumnIndexToView(ttable.getMainColumn());
            final String searchString = ttable.getStringValue((TreeNode)value, column);
            
            popup.add(new JMenuItem(CPUView.FIND_IN_HOTSPOTS) {
                { setEnabled(userValue != null); }
                protected void fireActionPerformed(ActionEvent e) {
                    ProfilerTable table = hotSpotsView.getResultsComponent();
                    if (SearchUtils.findString(table, searchString)) {
                        hotSpotsView.setVisible(true);
                        foundInHotSpots();
                        table.requestFocusInWindow();
                    }
                }
            });
            
            popup.add(new JMenuItem(CPUView.FIND_IN_REVERSECALLS) {
                { setEnabled(userValue != null); }
                protected void fireActionPerformed(ActionEvent e) {
                    ProfilerTreeTable table = (ProfilerTreeTable)reverseCallsView.getResultsComponent();
                    if (SearchUtils.findString(table, searchString, true, true, createSearchHelper())) {
                        reverseCallsView.setVisible(true);
                        foundInReverseCalls();
                        table.requestFocusInWindow();
                    }
                }
            });
            
            popup.addSeparator();
            
            JMenu threads = new JMenu(CPUView.SHOW_MENU);
            popup.add(threads);
            
            threads.add(new JMenuItem(CPUView.SHOW_THREAD_ITEM) {
                {
                    setEnabled(!mergedThreads && threadsSelector != null && value instanceof PrestimeCPUCCTNode &&
                               snapshot.getNThreads() > 1 && (selectedThreads == null || selectedThreads.size() > 1));
                }
                protected void fireActionPerformed(ActionEvent e) {
                    PrestimeCPUCCTNode thread = (PrestimeCPUCCTNode)value;
                    threadsSelector.addThread(thread.getThreadId(), true);
                }
            });
            
            threads.add(new JMenuItem(CPUView.HIDE_THREAD_ITEM) {
                {
                    setEnabled(!mergedThreads && threadsSelector != null && value instanceof PrestimeCPUCCTNode &&
                               snapshot.getNThreads() > 1 && (selectedThreads == null || selectedThreads.size() > 1));
                }
                protected void fireActionPerformed(ActionEvent e) {
                    PrestimeCPUCCTNode thread = (PrestimeCPUCCTNode)value;
                    threadsSelector.removeThread(thread.getThreadId());
                }
            });
            
            JMenu expand = new JMenu(CPUView.EXPAND_MENU);
            popup.add(expand);
            
            expand.add(new JMenuItem(CPUView.EXPAND_PLAIN_ITEM) {
                protected void fireActionPerformed(ActionEvent e) {
                    ttable.expandPlainPath(ttable.getSelectedRow(), 2);
                }
            });
            
            expand.add(new JMenuItem(CPUView.EXPAND_TOPMOST_ITEM) {
                protected void fireActionPerformed(ActionEvent e) {
                    ttable.expandFirstPath(ttable.getSelectedRow());
                }
            });
            
            expand.addSeparator();
            
            expand.add(new JMenuItem(CPUView.COLLAPSE_CHILDREN_ITEM) {
                protected void fireActionPerformed(ActionEvent e) {
                    ttable.collapseChildren(ttable.getSelectedRow());
                }
            });
            
            expand.add(new JMenuItem(CPUView.COLLAPSE_ALL_ITEM) {
                protected void fireActionPerformed(ActionEvent e) {
                    ttable.collapseAll();
                }
            });
        } else if (invoker == hotSpotsView) {
            // Ugly hack - there's a space between method name and parameters
            final String searchString = value.toString().replace("(", " ("); // NOI18N
            
            popup.add(new JMenuItem(CPUView.FIND_IN_FORWARDCALLS) {
                { setEnabled(userValue != null); }
                protected void fireActionPerformed(ActionEvent e) {
                    ProfilerTable table = forwardCallsView.getResultsComponent();
                    if (SearchUtils.findString(table, searchString)) {
                        forwardCallsView.setVisible(true);
                        foundInForwardCalls();
                        table.requestFocusInWindow();
                    }
                }
            });
            
            popup.add(new JMenuItem(CPUView.FIND_IN_REVERSECALLS) {
                { setEnabled(userValue != null); }
                protected void fireActionPerformed(ActionEvent e) {
                    ProfilerTreeTable table = (ProfilerTreeTable)reverseCallsView.getResultsComponent();
                    if (SearchUtils.findString(table, searchString, true, true, createSearchHelper())) {
                        reverseCallsView.setVisible(true);
                        foundInReverseCalls();
                        table.requestFocusInWindow();
                    }
                }
            });
        } else if (invoker == reverseCallsView) {
            final ProfilerTreeTable ttable = (ProfilerTreeTable)reverseCallsView.getResultsComponent();
            int column = ttable.convertColumnIndexToView(ttable.getMainColumn());
            final String searchString = ttable.getStringValue((TreeNode)value, column);
            
            popup.add(new JMenuItem(CPUView.FIND_IN_FORWARDCALLS) {
                { setEnabled(userValue != null); }
                protected void fireActionPerformed(ActionEvent e) {
                    ProfilerTable table = forwardCallsView.getResultsComponent();
                    if (SearchUtils.findString(table, searchString)) {
                        forwardCallsView.setVisible(true);
                        foundInForwardCalls();
                        table.requestFocusInWindow();
                    }
                }
            });
            
            popup.add(new JMenuItem(CPUView.FIND_IN_HOTSPOTS) {
                { setEnabled(userValue != null); }
                protected void fireActionPerformed(ActionEvent e) {
                    ProfilerTable table = hotSpotsView.getResultsComponent();
                    if (SearchUtils.findString(table, searchString)) {
                        hotSpotsView.setVisible(true);
                        foundInHotSpots();
                        table.requestFocusInWindow();
                    }
                }
            });
            
            popup.addSeparator();
            
            JMenu threads = new JMenu(CPUView.SHOW_MENU);
            popup.add(threads);
            
            threads.add(new JMenuItem(CPUView.SHOW_THREAD_ITEM) {
                {
                    setEnabled(!mergedThreads && threadsSelector != null && value instanceof PrestimeCPUCCTNode &&
                               snapshot.getNThreads() > 1 && (selectedThreads == null || selectedThreads.size() > 1));
                }
                protected void fireActionPerformed(ActionEvent e) {
                    PrestimeCPUCCTNode thread = (PrestimeCPUCCTNode)value;
                    threadsSelector.addThread(thread.getThreadId(), true);
                }
            });
            
            threads.add(new JMenuItem(CPUView.HIDE_THREAD_ITEM) {
                {
                    setEnabled(!mergedThreads && threadsSelector != null && value instanceof PrestimeCPUCCTNode &&
                               snapshot.getNThreads() > 1 && (selectedThreads == null || selectedThreads.size() > 1));
                }
                protected void fireActionPerformed(ActionEvent e) {
                    PrestimeCPUCCTNode thread = (PrestimeCPUCCTNode)value;
                    threadsSelector.removeThread(thread.getThreadId());
                }
            });
            
            JMenu expand = new JMenu(CPUView.EXPAND_MENU);
            popup.add(expand);
            
            expand.add(new JMenuItem(CPUView.EXPAND_PLAIN_ITEM) {
                protected void fireActionPerformed(ActionEvent e) {
                    ttable.expandPlainPath(ttable.getSelectedRow(), 1);
                }
            });
            
            expand.add(new JMenuItem(CPUView.EXPAND_TOPMOST_ITEM) {
                protected void fireActionPerformed(ActionEvent e) {
                    ttable.expandFirstPath(ttable.getSelectedRow());
                }
            });
            
            expand.addSeparator();
            
            expand.add(new JMenuItem(CPUView.COLLAPSE_CHILDREN_ITEM) {
                protected void fireActionPerformed(ActionEvent e) {
                    ttable.collapseChildren(ttable.getSelectedRow());
                }
            });
            
            expand.add(new JMenuItem(CPUView.COLLAPSE_ALL_ITEM) {
                protected void fireActionPerformed(ActionEvent e) {
                    ttable.collapseAll();
                }
            });
        }
        
        popup.addSeparator();
        popup.add(invoker.createCopyMenuItem());
        
        popup.addSeparator();
        popup.add(new JMenuItem(FilterUtils.ACTION_FILTER) {
            protected void fireActionPerformed(ActionEvent e) { invoker.activateFilter(); }
        });
        popup.add(new JMenuItem(SearchUtils.ACTION_FIND) {
            protected void fireActionPerformed(ActionEvent e) { invoker.activateSearch(); }
        });
    }
    
    private static SearchUtils.TreeHelper createSearchHelper() {
        return new SearchUtils.TreeHelper() {
            public int getNodeType(TreeNode tnode) {
                PrestimeCPUCCTNode node = (PrestimeCPUCCTNode)tnode;
                CCTNode parent = node.getParent();
                if (parent == null) return SearchUtils.TreeHelper.NODE_SKIP_DOWN; // invisible root
                
                if (node.isThreadNode()) return SearchUtils.TreeHelper.NODE_SKIP_DOWN; // thread node
                if (node.isSelfTimeNode()) return SearchUtils.TreeHelper.NODE_SKIP_NEXT; // self time node
                
                if (((PrestimeCPUCCTNode)parent).isThreadNode() || // toplevel method node (children of thread)
                    parent.getParent() == null) {                  // toplevel method node (merged threads)
                    return SearchUtils.TreeHelper.NODE_SEARCH_NEXT;
                }
                
                return SearchUtils.TreeHelper.NODE_SKIP_NEXT; // reverse call tree node
            }
        };
    }
    
    protected void customizeNodePopup(DataView invoker, JPopupMenu popup, Object value, ClientUtils.SourceCodeSelection userValue) {}
    
    
    private synchronized ExecutorService getExecutor() {
        if (executor == null) executor = Executors.newSingleThreadExecutor();
        return executor;
    }
    
}
