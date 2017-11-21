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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import org.netbeans.lib.profiler.ProfilerClient;
import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.results.RuntimeCCTNode;
import org.netbeans.lib.profiler.results.jdbc.JdbcCCTProvider;
import org.netbeans.lib.profiler.results.jdbc.JdbcResultsDiff;
import org.netbeans.lib.profiler.results.jdbc.JdbcResultsSnapshot;
import org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode;
import org.netbeans.lib.profiler.ui.UIUtils;
import static org.netbeans.lib.profiler.ui.jdbc.JDBCTreeTableView.isSQL;
import org.netbeans.lib.profiler.ui.memory.LiveMemoryView;
import org.netbeans.lib.profiler.ui.results.DataView;
import org.netbeans.lib.profiler.ui.swing.FilterUtils;
import org.netbeans.lib.profiler.ui.swing.ProfilerTable;
import org.netbeans.lib.profiler.ui.swing.ProfilerTreeTable;
import org.netbeans.lib.profiler.ui.swing.SearchUtils;
import org.netbeans.lib.profiler.utils.Wildcards;
import org.openide.util.Lookup;

/**
 *
 * @author Jiri Sedlacek
 */
public abstract class LiveJDBCView extends JPanel {

    private static final int MIN_UPDATE_DIFF = 900;
    private static final int MAX_UPDATE_DIFF = 1400;
    
    private ResultsMonitor rm;
    
    private JdbcResultsSnapshot snapshot;
    private JdbcResultsSnapshot refSnapshot;
    
    private DataView lastFocused;
    private JDBCTreeTableView jdbcCallsView;
    
    private long lastupdate;
    private volatile boolean paused;
    private volatile boolean forceRefresh;
    private volatile boolean refreshIsRunning;
    
    private ExecutorService executor;
    
    
    private final class ResultsMonitor implements JdbcCCTProvider.Listener {
        
        @Override
        public void cctEstablished(RuntimeCCTNode appRootNode, boolean empty) {
            if (!empty) {
                try {
                    LiveJDBCView.this.refreshData(appRootNode);
                } catch (ClientUtils.TargetAppOrVMTerminated ex) {
                    Logger.getLogger(LiveMemoryView.class.getName()).log(Level.FINE, null, ex);
                }
            }
        }

        @Override
        public void cctReset() {
            LiveJDBCView.this.resetData();
        }
    }
    
    
    public LiveJDBCView(Set<ClientUtils.SourceCodeSelection> selection) {
        initUI(selection);
        registerActions();        
    }
    
    
    public void setView(boolean forwardCalls, boolean hotSpots, boolean reverseCalls) {
        jdbcCallsView.setVisible(forwardCalls);
    }
    
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public void setForceRefresh(boolean forceRefresh) {
        this.forceRefresh = forceRefresh;
    }

    private void refreshData(RuntimeCCTNode appRootNode) throws ClientUtils.TargetAppOrVMTerminated {
        if ((lastupdate + MIN_UPDATE_DIFF > System.currentTimeMillis() || paused) && !forceRefresh) return;
        if (refreshIsRunning) return;
        refreshIsRunning = true;
        try {
            ProfilerClient client = getProfilerClient();
            final JdbcResultsSnapshot snapshotData =
                    client.getStatus().getInstrMethodClasses() == null ?
                    null : client.getJdbcProfilingResultsSnapshot(false);
            UIUtils.runInEventDispatchThread(new Runnable() {
                public void run() {
                    snapshot = snapshotData;
                    setData();
                    lastupdate = System.currentTimeMillis();
                    forceRefresh = false;
                }
            });
        } catch (Throwable t) {
            refreshIsRunning = false;
            if (t instanceof ClientUtils.TargetAppOrVMTerminated) {
                throw ((ClientUtils.TargetAppOrVMTerminated)t);
            } else {
                Logger.getLogger(LiveJDBCView.class.getName()).log(Level.SEVERE, null, t);
            }
        }
    }
    
    private void setData() {
        UIUtils.runInEventDispatchThread(new Runnable() {
            public void run() {
                if (snapshot == null) {
                    resetData();
                    refreshIsRunning = false;
                } else {
                    getExecutor().submit(new Runnable() {
                        public void run() {
                            final JdbcResultsSnapshot _snapshot = refSnapshot == null ? snapshot :
                                                                 refSnapshot.createDiff(snapshot);

                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    try {
                                        boolean diff = _snapshot instanceof JdbcResultsDiff;
                                        jdbcCallsView.setData(_snapshot, null, -1, null, false, false, diff);
                                    } finally {
                                        refreshIsRunning = false;
                                    }
                                }
                            });
                        }
                    });
                }
            }
        });
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
    
    public void refreshData() throws ClientUtils.TargetAppOrVMTerminated {
        if ((lastupdate + MAX_UPDATE_DIFF < System.currentTimeMillis() && !paused) || forceRefresh) {
            getProfilerClient().forceObtainedResultsDump(true);
        }        
    }
    
    public void resetData() {
        UIUtils.runInEventDispatchThread(new Runnable() {
            public void run() {
                jdbcCallsView.resetData();
                snapshot = null;
                refSnapshot = null;
            }
        });
    }
    
    
    public void showSelectionColumn() {
        jdbcCallsView.showSelectionColumn();
    }
    
    public void refreshSelection() {
        jdbcCallsView.refreshSelection();
    }
    
    
    public void cleanup() {
    }
    
    
    public void profilingSessionStarted() {
        if (rm == null) {
            rm = new ResultsMonitor();
            Collection<? extends JdbcCCTProvider> jdbcCCTProviders = Lookup.getDefault().lookupAll(JdbcCCTProvider.class);
            assert !jdbcCCTProviders.isEmpty();
            for (JdbcCCTProvider provider : jdbcCCTProviders) {
                provider.addListener(rm);
            }
        }    
    }
    
    public void profilingSessionFinished() {
        if (rm != null) {
            Collection<? extends JdbcCCTProvider> jdbcCCTProviders = Lookup.getDefault().lookupAll(JdbcCCTProvider.class);
            assert !jdbcCCTProviders.isEmpty();
            for (JdbcCCTProvider provider : jdbcCCTProviders) {
                provider.removeListener(rm);
            }
            rm = null;
        }
    }
    
    
    protected abstract ProfilerClient getProfilerClient();
    
    
    protected boolean profileMethodSupported() { return true; }
    
    
    protected abstract boolean showSourceSupported();
    
    protected abstract void showSource(ClientUtils.SourceCodeSelection value);
    
    protected abstract void showSQLQuery(String query, String htmlQuery);
    
    protected abstract void selectForProfiling(ClientUtils.SourceCodeSelection value);
    
    protected void popupShowing() {};
    
    protected void popupHidden() {};
    
    
    private void profileMethod(ClientUtils.SourceCodeSelection value) {
        selectForProfiling(value);
    }
    
    private void profileClass(ClientUtils.SourceCodeSelection value) {
        selectForProfiling(new ClientUtils.SourceCodeSelection(
                           value.getClassName(), Wildcards.ALLWILDCARD, null));
    }
    
    
    private void initUI(Set<ClientUtils.SourceCodeSelection> selection) {
        setLayout(new BorderLayout(0, 0));
        
        jdbcCallsView = new JDBCTreeTableView(selection, false) {
            protected void installDefaultAction() {
                getResultsComponent().setDefaultAction(new AbstractAction() {
                    public void actionPerformed(ActionEvent e) {
                        ProfilerTable t = getResultsComponent();
                        int row = t.getSelectedRow();
                        PresoObjAllocCCTNode node = (PresoObjAllocCCTNode)t.getValueForRow(row);
                        if (isSQL(node)) {
                            showQueryImpl(node);
                        } else {
                            ClientUtils.SourceCodeSelection userValue = getUserValueForRow(row);
                            if (userValue != null) performDefaultAction(userValue);
                        }
                    }
                });
            }
            protected void performDefaultAction(ClientUtils.SourceCodeSelection userValue) {
                if (showSourceSupported()) showSource(userValue);
            }
            protected void populatePopup(JPopupMenu popup, Object value, ClientUtils.SourceCodeSelection userValue) {
                LiveJDBCView.this.populatePopup(jdbcCallsView, popup, value, userValue);
            }
            protected void popupShowing() { LiveJDBCView.this.popupShowing(); }
            protected void popupHidden()  { LiveJDBCView.this.popupHidden(); }
            protected boolean hasBottomFilterFindMargin() { return true; }
        };
        jdbcCallsView.notifyOnFocus(new Runnable() {
            public void run() { lastFocused = jdbcCallsView; }
        });
        
        add(jdbcCallsView, BorderLayout.CENTER);
        
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
            if (jdbcCallsView.isShowing()) lastFocused = jdbcCallsView;
        }
        
        return lastFocused;
    }
    
    private void showQueryImpl(PresoObjAllocCCTNode node) {
        showSQLQuery(node.getNodeName(), ((JDBCTreeTableView.SQLQueryNode)node).htmlName);
    }
    
    private void populatePopup(final DataView invoker, JPopupMenu popup, final Object value, final ClientUtils.SourceCodeSelection userValue) {
        final PresoObjAllocCCTNode node = (PresoObjAllocCCTNode)value;
        if (JDBCTreeTableView.isSQL(node)) {
            popup.add(new JMenuItem(JDBCView.ACTION_VIEWSQLQUERY) {
                { setFont(getFont().deriveFont(Font.BOLD)); }
                protected void fireActionPerformed(ActionEvent e) { showQueryImpl((JDBCTreeTableView.SQLQueryNode)value); }
            });
            popup.addSeparator();
        } else if (showSourceSupported()) {
            popup.add(new JMenuItem(JDBCView.ACTION_GOTOSOURCE) {
                { setEnabled(userValue != null); setFont(getFont().deriveFont(Font.BOLD)); }
                protected void fireActionPerformed(ActionEvent e) { showSource(userValue); }
            });
            popup.addSeparator();
        }
        
        popup.add(new JMenuItem(JDBCView.ACTION_PROFILE_METHOD) {
            { setEnabled(userValue != null && JDBCTreeTableView.isSelectable(node)); }
            protected void fireActionPerformed(ActionEvent e) { profileMethod(userValue); }
        });
        
        popup.add(new JMenuItem(JDBCView.ACTION_PROFILE_CLASS) {
            { setEnabled(userValue != null); }
            protected void fireActionPerformed(ActionEvent e) { profileClass(userValue); }
        });
        
        popup.addSeparator();
        
        final ProfilerTreeTable ttable = (ProfilerTreeTable)jdbcCallsView.getResultsComponent();
        JMenu expand = new JMenu(JDBCView.EXPAND_MENU);
        popup.add(expand);

        expand.add(new JMenuItem(JDBCView.EXPAND_PLAIN_ITEM) {
            protected void fireActionPerformed(ActionEvent e) {
                ttable.expandPlainPath(ttable.getSelectedRow(), 1);
            }
        });

        expand.add(new JMenuItem(JDBCView.EXPAND_TOPMOST_ITEM) {
            protected void fireActionPerformed(ActionEvent e) {
                ttable.expandFirstPath(ttable.getSelectedRow());
            }
        });
        
        expand.addSeparator();
            
        expand.add(new JMenuItem(JDBCView.COLLAPSE_CHILDREN_ITEM) {
            protected void fireActionPerformed(ActionEvent e) {
                ttable.collapseChildren(ttable.getSelectedRow());
            }
        });

        expand.add(new JMenuItem(JDBCView.COLLAPSE_ALL_ITEM) {
            protected void fireActionPerformed(ActionEvent e) {
                ttable.collapseAll();
            }
        });
        
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
    
    private synchronized ExecutorService getExecutor() {
        if (executor == null) executor = Executors.newSingleThreadExecutor();
        return executor;
    }
    
}
