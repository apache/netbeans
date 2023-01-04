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
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.results.jdbc.JdbcResultsDiff;
import org.netbeans.lib.profiler.results.jdbc.JdbcResultsSnapshot;
import org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode;
import org.netbeans.lib.profiler.ui.components.ProfilerToolbar;
import org.netbeans.lib.profiler.ui.results.DataView;
import org.netbeans.lib.profiler.ui.swing.ExportUtils;
import org.netbeans.lib.profiler.ui.swing.FilterUtils;
import org.netbeans.lib.profiler.ui.swing.ProfilerTable;
import org.netbeans.lib.profiler.ui.swing.ProfilerTreeTable;
import org.netbeans.lib.profiler.ui.swing.SearchUtils;
import org.netbeans.lib.profiler.utils.Wildcards;

/**
 *
 * @author Jiri Sedlacek
 */
public abstract class SnapshotJDBCView extends JPanel {
    
    // -----
    // I18N String constants
    private static final ResourceBundle messages = ResourceBundle.getBundle("org.netbeans.lib.profiler.ui.jdbc.Bundle"); // NOI18N
    private static final String COMPARE_SNAPSHOTS = messages.getString("SnapshotJDBCView_CompareSnapshots"); // NOI18N
    private static final String RESET_COMPARE_SNAPSHOTS = messages.getString("SnapshotJDBCView_ResetCompareSnapshots"); // NOI18N
    // -----
    
    private JdbcResultsSnapshot snapshot;
    private JdbcResultsSnapshot refSnapshot;
    
//    private int aggregation;
    
    private DataView lastFocused;
    private JDBCTreeTableView jdbcCallsView;
    
    private JToggleButton compareButton;
    
    private ExecutorService executor;
    
    
    public SnapshotJDBCView(JdbcResultsSnapshot snapshot, Action saveAction, Action compareAction, Action infoAction, ExportUtils.Exportable exportProvider) {
        initUI(saveAction, compareAction, infoAction, exportProvider);
        registerActions();
        
//        aggregation = CPUResultsSnapshot.METHOD_LEVEL_VIEW;
        setSnapshot(snapshot);
    }
    
    
    public void setRefSnapshot(JdbcResultsSnapshot snapshot) {
        refSnapshot = snapshot;
        
        if (compareButton != null && snapshot != null) {
            compareButton.setSelected(true);
            compareButton.setToolTipText(RESET_COMPARE_SNAPSHOTS);
        }
        
        setData();
    }
    
    
    protected abstract boolean showSourceSupported();
    
    protected abstract void showSource(ClientUtils.SourceCodeSelection value);
    
    protected abstract void showSQLQuery(String query, String htmlQuery);
    
    protected abstract void selectForProfiling(ClientUtils.SourceCodeSelection value);
    
    
    private void profileMethod(ClientUtils.SourceCodeSelection value) {
        selectForProfiling(value);
    }
    
    private void profileClass(ClientUtils.SourceCodeSelection value) {
        selectForProfiling(new ClientUtils.SourceCodeSelection(
                           value.getClassName(), Wildcards.ALLWILDCARD, null));
    }
    
    
    private void initUI(Action saveAction, final Action compareAction, Action infoAction, ExportUtils.Exportable exportProvider) {
        setLayout(new BorderLayout(0, 0));
        
        jdbcCallsView = new JDBCTreeTableView(null, false) {
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
            protected void performDefaultAction(ClientUtils.SourceCodeSelection value) {
                if (showSourceSupported()) showSource(value);
            }
            protected void populatePopup(JPopupMenu popup, Object value, ClientUtils.SourceCodeSelection userValue) {
                SnapshotJDBCView.this.populatePopup(jdbcCallsView, popup, value, userValue);
            }
        };
        jdbcCallsView.notifyOnFocus(new Runnable() {
            public void run() { lastFocused = jdbcCallsView; }
        });
        
        add(jdbcCallsView, BorderLayout.CENTER);
        
        ProfilerToolbar toolbar = ProfilerToolbar.create(true);
        
        if (saveAction != null) toolbar.add(saveAction);
        
        toolbar.add(ExportUtils.exportButton(this, JDBCView.EXPORT_TOOLTIP, getExportables(exportProvider)));
        
        if (compareAction != null) {
            toolbar.addSpace(2);
            toolbar.addSeparator();
            toolbar.addSpace(2);
        
            Icon icon = (Icon)compareAction.getValue(Action.SMALL_ICON);
            compareButton = new JToggleButton(icon) {
                @Override
                protected void fireActionPerformed(ActionEvent e) {
                    boolean sel = isSelected();
                    if (sel) {
                        compareAction.actionPerformed(e);
                        if (refSnapshot == null) setSelected(false);
                    } else {
                        setRefSnapshot(null);
                    }
                    setToolTipText(isSelected() ? RESET_COMPARE_SNAPSHOTS :
                                                  COMPARE_SNAPSHOTS);
                }
            };
            compareButton.setToolTipText(COMPARE_SNAPSHOTS);
            toolbar.add(compareButton);
        }
//        
//        toolbar.addSpace(2);
//        toolbar.addSeparator();
//        toolbar.addSpace(5);
        
//        GrayLabel aggregationL = new GrayLabel(TOOLBAR_AGGREGATION);
//        toolbar.add(aggregationL);
//        
//        toolbar.addSpace(2);
        
//        Action aMethods = new AbstractAction() {
//            { putValue(NAME, AGGREGATION_METHODS); }
//            public void actionPerformed(ActionEvent e) { setAggregation(CPUResultsSnapshot.METHOD_LEVEL_VIEW); }
//            
//        };
//        Action aClasses = new AbstractAction() {
//            { putValue(NAME, AGGREGATION_CLASSES); }
//            public void actionPerformed(ActionEvent e) { setAggregation(CPUResultsSnapshot.CLASS_LEVEL_VIEW); }
//            
//        };
//        Action aPackages = new AbstractAction() {
//            { putValue(NAME, AGGREGATION_PACKAGES); }
//            public void actionPerformed(ActionEvent e) { setAggregation(CPUResultsSnapshot.PACKAGE_LEVEL_VIEW); }
//            
//        };
//        
//        ActionPopupButton aggregation = new ActionPopupButton(aMethods, aClasses, aPackages);
//        toolbar.add(aggregation);
//        
        if (infoAction != null) {
            toolbar.addFiller();
            toolbar.add(infoAction);
        }
        
        add(toolbar.getComponent(), BorderLayout.NORTH);
        
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

                @Override
                protected void fireActionPerformed(ActionEvent e) { showQueryImpl((JDBCTreeTableView.SQLQueryNode)value); }
            });
            popup.addSeparator();
        } else if (showSourceSupported()) {
            popup.add(new JMenuItem(JDBCView.ACTION_GOTOSOURCE) {
                { setEnabled(userValue != null); setFont(getFont().deriveFont(Font.BOLD)); }

                @Override
                protected void fireActionPerformed(ActionEvent e) { showSource(userValue); }
            });
            popup.addSeparator();
        }
        
        popup.add(new JMenuItem(JDBCView.ACTION_PROFILE_METHOD) {
            { setEnabled(userValue != null && JDBCTreeTableView.isSelectable(node)); }

            @Override
            protected void fireActionPerformed(ActionEvent e) { profileMethod(userValue); }
        });
        
        popup.add(new JMenuItem(JDBCView.ACTION_PROFILE_CLASS) {
            { setEnabled(userValue != null); }

            @Override
            protected void fireActionPerformed(ActionEvent e) { profileClass(userValue); }
        });
        
        customizeNodePopup(invoker, popup, value, userValue);
        
        popup.addSeparator();
        
        final ProfilerTreeTable ttable = (ProfilerTreeTable)jdbcCallsView.getResultsComponent();
        JMenu expand = new JMenu(JDBCView.EXPAND_MENU);
        popup.add(expand);

        expand.add(new JMenuItem(JDBCView.EXPAND_PLAIN_ITEM) {
            @Override
            protected void fireActionPerformed(ActionEvent e) {
                ttable.expandPlainPath(ttable.getSelectedRow(), 1);
            }
        });

        expand.add(new JMenuItem(JDBCView.EXPAND_TOPMOST_ITEM) {
            @Override
            protected void fireActionPerformed(ActionEvent e) {
                ttable.expandFirstPath(ttable.getSelectedRow());
            }
        });
        
        expand.addSeparator();
            
        expand.add(new JMenuItem(JDBCView.COLLAPSE_CHILDREN_ITEM) {
            @Override
            protected void fireActionPerformed(ActionEvent e) {
                ttable.collapseChildren(ttable.getSelectedRow());
            }
        });

        expand.add(new JMenuItem(JDBCView.COLLAPSE_ALL_ITEM) {
            @Override
            protected void fireActionPerformed(ActionEvent e) {
                ttable.collapseAll();
            }
        });
        
        popup.addSeparator();
        popup.add(invoker.createCopyMenuItem());
        
        popup.addSeparator();
        popup.add(new JMenuItem(FilterUtils.ACTION_FILTER) {
            @Override
            protected void fireActionPerformed(ActionEvent e) { invoker.activateFilter(); }
        });
        popup.add(new JMenuItem(SearchUtils.ACTION_FIND) {
            @Override
            protected void fireActionPerformed(ActionEvent e) { invoker.activateSearch(); }
        });
        
    }
    
    protected void customizeNodePopup(DataView invoker, JPopupMenu popup, Object value, ClientUtils.SourceCodeSelection userValue) {}
    
    private void setData() {
        getExecutor().submit(new Runnable() {
            public void run() {
                final JdbcResultsSnapshot _snapshot = refSnapshot == null ? snapshot :
                                               snapshot.createDiff(refSnapshot);
                
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        boolean diff = _snapshot instanceof JdbcResultsDiff;
                        jdbcCallsView.setData(_snapshot, null, -1, null, false, false, diff);
                    }
                });
            }
        });
    }
    
    protected final void setSnapshot(JdbcResultsSnapshot snapshot) {
        this.snapshot = snapshot;
        setData();
    }
    
    private ExportUtils.Exportable[] getExportables(final ExportUtils.Exportable snapshotExporter) {
        return new ExportUtils.Exportable[] {
            new ExportUtils.Exportable() {
                public boolean isEnabled() {
                    return refSnapshot == null && snapshotExporter.isEnabled();
                }
                public String getName() {
                    return snapshotExporter.getName();
                }
                public ExportUtils.ExportProvider[] getProviders() {
                    return snapshotExporter.getProviders();
                }
            },
            new ExportUtils.Exportable() {
                public boolean isEnabled() {
                    return jdbcCallsView.isVisible();
                }
                public String getName() {
                    return MessageFormat.format(JDBCView.EXPORT_LBL, JDBCView.EXPORT_QUERIES);
                }
                public ExportUtils.ExportProvider[] getProviders() {
                    return jdbcCallsView.getExportProviders();
                }
            }
        };
    }
    
    private synchronized ExecutorService getExecutor() {
        if (executor == null) executor = Executors.newSingleThreadExecutor();
        return executor;
    }
    
}
