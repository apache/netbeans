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

package org.netbeans.lib.profiler.ui.memory;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.filters.GenericFilter;
import org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot;
import org.netbeans.lib.profiler.results.memory.AllocMemoryResultsSnapshot;
import org.netbeans.lib.profiler.results.memory.LivenessMemoryResultsSnapshot;
import org.netbeans.lib.profiler.results.memory.MemoryResultsSnapshot;
import org.netbeans.lib.profiler.results.memory.SampledMemoryResultsSnapshot;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.lib.profiler.ui.results.DataView;
import org.netbeans.lib.profiler.ui.swing.FilterUtils;
import org.netbeans.lib.profiler.ui.swing.ProfilerTreeTable;
import org.netbeans.lib.profiler.ui.swing.SearchUtils;

/**
 *
 * @author Jiri Sedlacek
 */
public abstract class LiveMemoryView extends JPanel {
    
    private MemoryView dataView;
    
    private long lastupdate;
    private volatile boolean refreshIsRunning;
    
    private final Set<ClientUtils.SourceCodeSelection> selection;
    
    private MemoryResultsSnapshot snapshot;
    private MemoryResultsSnapshot refSnapshot;
    
    private GenericFilter filter;
    
    
    
       
    public LiveMemoryView(Set<ClientUtils.SourceCodeSelection> selection) {
        this.selection = selection;
        initUI();
    }
    
    
    
    public boolean isRefreshRunning() {
        return refreshIsRunning;
    }
    
    public long getLastUpdate() {
        return lastupdate;
    }
    
    public void setData(final MemoryResultsSnapshot snapshotData, final GenericFilter ifilter) {
        if (refreshIsRunning) return;
        refreshIsRunning = true;
        
        // class names in VM format
//        MemoryView.userFormClassNames(snapshotData);
        
        UIUtils.runInEventDispatchThread(new Runnable() {
            public void run() {
                try {
                    updateDataView(snapshotData);

                    snapshot = snapshotData;
                    filter = ifilter;

                    if (dataView != null && snapshot != null) {
                        if (refSnapshot == null) dataView.setData(snapshot, filter, CPUResultsSnapshot.CLASS_LEVEL_VIEW);
                        else dataView.setData(refSnapshot.createDiff(snapshot), filter, CPUResultsSnapshot.CLASS_LEVEL_VIEW);
                    }
                } finally {
                    refreshIsRunning = false;
                    lastupdate = System.currentTimeMillis();
                }
            }
        });
    }
    
//    private void refreshData(RuntimeCCTNode appRootNode) throws ClientUtils.TargetAppOrVMTerminated {
//        if ((lastupdate + MIN_UPDATE_DIFF > System.currentTimeMillis() || paused) && !forceRefresh) return;
//        if (refreshIsRunning) return;
//        refreshIsRunning = true;
//        try {
//            ProfilerClient client = getProfilerClient();
//            final MemoryResultsSnapshot _snapshot = client.getMemoryProfilingResultsSnapshot(false);
//
//            // class names in VM format
//            MemoryView.userFormClassNames(_snapshot);
//
//            // class names in VM format
//            final GenericFilter ifilter = client.getSettings().getInstrumentationFilter();
//            // --- TODO: rewrite down to all usages
////            String[] _ifilter = ifilter == null ? null : ifilter.getUserFilterStrings();
////            final Collection<String> _filter = _ifilter == null ? Collections.EMPTY_LIST :
////                                               Arrays.asList(_ifilter);
//            // ---
////            final Collection<String> _filter = Arrays.asList(ifilter.getValues()); // Actually wrong, cuts trailing *
//            
//            
////            if (_ifilter != null) for (String s : _ifilter)
////                    _filter.add(StringUtils.userFormClassName(s));
//
//            SwingUtilities.invokeLater(new Runnable() {
//                public void run() {
//                    try {
//                        refreshDataImpl(_snapshot, ifilter);
//                    } finally {
//                        refreshIsRunning = false;
//                    }
//                }
//            });
//        } catch (RuntimeException ex) {
//            refreshIsRunning = false;
//            throw ex;
//        } catch (ClientUtils.TargetAppOrVMTerminated ex) {
//            refreshIsRunning = false;
//            throw ex;            
//        }
//        
//        lastupdate = System.currentTimeMillis();
//        forceRefresh = false;
//    }
    
//    private void refreshDataImpl(MemoryResultsSnapshot _snapshot, GenericFilter _filter) {
//        assert SwingUtilities.isEventDispatchThread();
//        
//        updateDataView(_snapshot);
//        
//        snapshot = _snapshot;
//        filter = _filter;
//        
//        if (dataView != null && snapshot != null) {
//            if (refSnapshot == null) dataView.setData(snapshot, filter, CPUResultsSnapshot.CLASS_LEVEL_VIEW);
//            else dataView.setData(refSnapshot.createDiff(snapshot), filter, CPUResultsSnapshot.CLASS_LEVEL_VIEW);
//        }
//    }
    
    public boolean setDiffView(boolean diff) {
        if (snapshot == null) return false;
        refSnapshot = diff ? snapshot : null;
        setData(snapshot, filter);
        return true;
    }

//    public void refreshData() throws ClientUtils.TargetAppOrVMTerminated {
//        if (paused && !forceRefresh) return;
//        
//        ProfilerClient client = getProfilerClient();
//        switch (client.getCurrentInstrType()) {
//            case CommonConstants.INSTR_NONE_MEMORY_SAMPLING:
//                refreshData(null);
//                break;
//            case CommonConstants.INSTR_OBJECT_LIVENESS:
//            case CommonConstants.INSTR_OBJECT_ALLOCATIONS:
//                if (lastupdate + MAX_UPDATE_DIFF < System.currentTimeMillis()) {
//                    client.forceObtainedResultsDump(true);
//                }
//                break;
//            default:
//                throw new IllegalArgumentException("Invalid profiling instr. type: " + client.getCurrentInstrType()); // NOI18N
//        }
//    }
    
    public void resetData() {
        if (dataView != null) dataView.resetData();
        snapshot = null;
        refSnapshot = null;
        filter = null;
    }

    
    public void showSelectionColumn() {
        if (dataView != null) dataView.showSelectionColumn();
    }
    
    public void refreshSelection() {
        if (dataView != null) dataView.showSelectionColumn();
    }
    
    
//    public void cleanup() {
//        if (rm.view == this) rm.view = null;
//    }
    
    
//    protected abstract ProfilerClient getProfilerClient();
    
    protected boolean profileClassSupported() { return true; }
    
    
    protected abstract boolean showSourceSupported();
    
    protected abstract void showSource(ClientUtils.SourceCodeSelection value);
    
    protected abstract void selectForProfiling(ClientUtils.SourceCodeSelection value);
    
    protected void popupShowing() {};
    
    protected void popupHidden() {};
    
    
    private void updateDataView(MemoryResultsSnapshot snapshot) {
        if (snapshot == null || snapshot instanceof SampledMemoryResultsSnapshot) {
            if (dataView instanceof SampledTableView) return;
            
            dataView = new SampledTableView(selection) {
                protected void performDefaultAction(ClientUtils.SourceCodeSelection userValue) {
                    if (showSourceSupported()) showSource(userValue);
                }
                protected void populatePopup(JPopupMenu popup, Object value, ClientUtils.SourceCodeSelection userValue) {
                    LiveMemoryView.this.populatePopup(dataView, popup, value, userValue);
                }
                protected void popupShowing() { LiveMemoryView.this.popupShowing(); }
                protected void popupHidden()  { LiveMemoryView.this.popupHidden(); }
                protected boolean hasBottomFilterFindMargin() { return true; }
            };
        } else if (snapshot instanceof AllocMemoryResultsSnapshot) {
            if (snapshot.containsStacks()) {
                if (dataView instanceof AllocTreeTableView) return;
                
                dataView = new AllocTreeTableView(selection) {
                    protected void performDefaultAction(ClientUtils.SourceCodeSelection userValue) {
                        if (showSourceSupported()) showSource(userValue);
                    }
                    protected void populatePopup(JPopupMenu popup, Object value, ClientUtils.SourceCodeSelection userValue) {
                        LiveMemoryView.this.populatePopup(dataView, popup, value, userValue);
                    }
                    protected void popupShowing() { LiveMemoryView.this.popupShowing(); }
                    protected void popupHidden()  { LiveMemoryView.this.popupHidden(); }
                    protected boolean hasBottomFilterFindMargin() { return true; }
                };
            } else {
                if (dataView instanceof AllocTableView) return;
                
                dataView = new AllocTableView(selection) {
                    protected void performDefaultAction(ClientUtils.SourceCodeSelection userValue) {
                        if (showSourceSupported()) showSource(userValue);
                    }
                    protected void populatePopup(JPopupMenu popup, Object value, ClientUtils.SourceCodeSelection userValue) {
                        LiveMemoryView.this.populatePopup(dataView, popup, value, userValue);
                    }
                    protected void popupShowing() { LiveMemoryView.this.popupShowing(); }
                    protected void popupHidden()  { LiveMemoryView.this.popupHidden(); }
                    protected boolean hasBottomFilterFindMargin() { return true; }
                };
            }
        } else if (snapshot instanceof LivenessMemoryResultsSnapshot) {
            if (snapshot.containsStacks()) {
                if (dataView instanceof LivenessTreeTableView) return;
                
                dataView = new LivenessTreeTableView(selection, false) {
                    protected void performDefaultAction(ClientUtils.SourceCodeSelection userValue) {
                        if (showSourceSupported()) showSource(userValue);
                    }
                    protected void populatePopup(JPopupMenu popup, Object value, ClientUtils.SourceCodeSelection userValue) {
                        LiveMemoryView.this.populatePopup(dataView, popup, value, userValue);
                    }
                    protected void popupShowing() { LiveMemoryView.this.popupShowing(); }
                    protected void popupHidden()  { LiveMemoryView.this.popupHidden(); }
                    protected boolean hasBottomFilterFindMargin() { return true; }
                };
            } else {
                if (dataView instanceof LivenessTableView) return;
                
                dataView = new LivenessTableView(selection, false) {
                    protected void performDefaultAction(ClientUtils.SourceCodeSelection userValue) {
                        if (showSourceSupported()) showSource(userValue);
                    }
                    protected void populatePopup(JPopupMenu popup, Object value, ClientUtils.SourceCodeSelection userValue) {
                        LiveMemoryView.this.populatePopup(dataView, popup, value, userValue);
                    }
                    protected void popupShowing() { LiveMemoryView.this.popupShowing(); }
                    protected void popupHidden()  { LiveMemoryView.this.popupHidden(); }
                    protected boolean hasBottomFilterFindMargin() { return true; }
                };
            }
        } else {
            dataView = null;
        }
        
        removeAll();
        resetData();
        if (dataView != null) add(dataView, BorderLayout.CENTER);
        revalidate();
        repaint();
    }
    
    private void populatePopup(final DataView invoker, JPopupMenu popup, Object value, final ClientUtils.SourceCodeSelection userValue) {
        if (showSourceSupported()) {
            popup.add(new JMenuItem(MemoryView.ACTION_GOTOSOURCE) {
                { setEnabled(userValue != null); setFont(getFont().deriveFont(Font.BOLD)); }
                protected void fireActionPerformed(ActionEvent e) { showSource(userValue); }
            });
            popup.addSeparator();
        }
        
        if (profileClassSupported()) {
            popup.add(new JMenuItem(MemoryView.ACTION_PROFILE_CLASS) {
                { setEnabled(userValue != null); }
                protected void fireActionPerformed(ActionEvent e) { selectForProfiling(userValue); }
            });
        }
        
        if (profileClassSupported()) popup.addSeparator();
        
        JMenuItem[] customItems = invoker.createCustomMenuItems(this, value, userValue);
        if (customItems != null) {
            for (JMenuItem customItem : customItems) popup.add(customItem);
            popup.addSeparator();
        }
        
        customizeNodePopup(invoker, popup, value, userValue);
        
        if (snapshot.containsStacks()) {
            final ProfilerTreeTable ttable = (ProfilerTreeTable)dataView.getResultsComponent();
            JMenu expand = new JMenu(MemoryView.EXPAND_MENU);
            popup.add(expand);

            expand.add(new JMenuItem(MemoryView.EXPAND_PLAIN_ITEM) {
                protected void fireActionPerformed(ActionEvent e) {
                    ttable.expandPlainPath(ttable.getSelectedRow(), 1);
                }
            });

            expand.add(new JMenuItem(MemoryView.EXPAND_TOPMOST_ITEM) {
                protected void fireActionPerformed(ActionEvent e) {
                    ttable.expandFirstPath(ttable.getSelectedRow());
                }
            });
            
            expand.addSeparator();
            
            expand.add(new JMenuItem(MemoryView.COLLAPSE_CHILDREN_ITEM) {
                protected void fireActionPerformed(ActionEvent e) {
                    ttable.collapseChildren(ttable.getSelectedRow());
                }
            });
            
            expand.add(new JMenuItem(MemoryView.COLLAPSE_ALL_ITEM) {
                protected void fireActionPerformed(ActionEvent e) {
                    ttable.collapseAll();
                }
            });
            
            popup.addSeparator();
        }
        
        popup.add(invoker.createCopyMenuItem());
        popup.addSeparator();
        
        popup.add(new JMenuItem(FilterUtils.ACTION_FILTER) {
            protected void fireActionPerformed(ActionEvent e) { invoker.activateFilter(); }
        });
        popup.add(new JMenuItem(SearchUtils.ACTION_FIND) {
            protected void fireActionPerformed(ActionEvent e) { invoker.activateSearch(); }
        });
    }
    
    protected void customizeNodePopup(DataView invoker, JPopupMenu popup, Object value, ClientUtils.SourceCodeSelection userValue) {}
    
    
    private void initUI() {
        setLayout(new BorderLayout(0, 0));
        
        // TODO: read last state?
        updateDataView(null);
        
        registerActions();
    }
    
    private void registerActions() {
        ActionMap map = getActionMap();
        
        map.put(FilterUtils.FILTER_ACTION_KEY, new AbstractAction() {
            public void actionPerformed(ActionEvent e) { dataView.activateFilter(); }
        });
        
        map.put(SearchUtils.FIND_ACTION_KEY, new AbstractAction() {
            public void actionPerformed(ActionEvent e) { dataView.activateSearch(); }
        });
    }
    
}
