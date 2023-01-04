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
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
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
import org.netbeans.lib.profiler.ui.components.ProfilerToolbar;
import org.netbeans.lib.profiler.ui.results.DataView;
import org.netbeans.lib.profiler.ui.swing.ActionPopupButton;
import org.netbeans.lib.profiler.ui.swing.ExportUtils;
import org.netbeans.lib.profiler.ui.swing.FilterUtils;
import org.netbeans.lib.profiler.ui.swing.GrayLabel;
import org.netbeans.lib.profiler.ui.swing.MultiButtonGroup;
import org.netbeans.lib.profiler.ui.swing.ProfilerTable;
import org.netbeans.lib.profiler.ui.swing.ProfilerTreeTable;
import org.netbeans.lib.profiler.ui.swing.SearchUtils;
import org.netbeans.lib.profiler.utils.Wildcards;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.api.icons.ProfilerIcons;

/**
 *
 * @author Jiri Sedlacek
 */
public abstract class SnapshotCPUView extends JPanel {
    
    // -----
    // I18N String constants
    private static final ResourceBundle messages = ResourceBundle.getBundle("org.netbeans.lib.profiler.ui.cpu.Bundle"); // NOI18N
    private static final String TOOLBAR_VIEW = messages.getString("SnapshotCPUView_ToolbarView"); // NOI18N
    private static final String VIEW_FORWARD = messages.getString("SnapshotCPUView_ViewForward"); // NOI18N
    private static final String VIEW_HOTSPOTS = messages.getString("SnapshotCPUView_ViewHotSpots"); // NOI18N
    private static final String VIEW_REVERSE = messages.getString("SnapshotCPUView_ViewReverse"); // NOI18N
    private static final String TOOLBAR_AGGREGATION = messages.getString("SnapshotCPUView_ToolbarAggregation"); // NOI18N
    private static final String AGGREGATION_METHODS = messages.getString("SnapshotCPUView_AggregationMethods"); // NOI18N
    private static final String AGGREGATION_CLASSES = messages.getString("SnapshotCPUView_AggregationClasses"); // NOI18N
    private static final String AGGREGATION_PACKAGES = messages.getString("SnapshotCPUView_AggregationPackages"); // NOI18N
    private static final String COMPARE_SNAPSHOTS = messages.getString("SnapshotCPUView_CompareSnapshots"); // NOI18N
    private static final String RESET_COMPARE_SNAPSHOTS = messages.getString("SnapshotCPUView_ResetCompareSnapshots"); // NOI18N
    // -----
    
    private boolean sampled;
    private CPUResultsSnapshot snapshot;
    private CPUResultsSnapshot refSnapshot;
    
    private int aggregation;
    private boolean mergedThreads;
    private Collection<Integer> selectedThreads;
    
    private DataView lastFocused;
    private CPUTableView hotSpotsView;
    private CPUTreeTableView forwardCallsView;
    private CPUTreeTableView reverseCallsView;
    
    private JToggleButton[] toggles;
    private JToggleButton compareButton;
    
    private ThreadsSelector threadsSelector;
    
    private ExecutorService executor;
    
    
    public SnapshotCPUView(CPUResultsSnapshot snapshot, boolean sampled, Action saveAction, Action compareAction, Action infoAction, ExportUtils.Exportable exportProvider) {
        initUI(saveAction, compareAction, infoAction, exportProvider);
        registerActions();
        
        aggregation = CPUResultsSnapshot.METHOD_LEVEL_VIEW;
        setSnapshot(snapshot, sampled);
    }
    
    
    public void setRefSnapshot(CPUResultsSnapshot snapshot) {
        refSnapshot = snapshot;
        
        if (compareButton != null && snapshot != null) {
            compareButton.setSelected(true);
            compareButton.setToolTipText(RESET_COMPARE_SNAPSHOTS);
        }
        
        setAggregation(aggregation);
    }
    
    
    protected boolean profileMethodEnabled() { return true; }
    
    protected boolean profileMethodSupported() { return true; }
    
    protected boolean profileClassSupported() { return true; }
    
    
    protected abstract boolean showSourceSupported();
    
    protected abstract void showSource(ClientUtils.SourceCodeSelection value);
    
    protected abstract void selectForProfiling(ClientUtils.SourceCodeSelection value);
    
    
    protected void foundInForwardCalls() {
        toggles[0].setSelected(true);
    }
    protected void foundInHotSpots() {
        toggles[1].setSelected(true);
    }
    protected void foundInReverseCalls() {
        toggles[2].setSelected(true);
    }
    
    
    private void profileMethod(ClientUtils.SourceCodeSelection value) {
        selectForProfiling(value);
    }
    
    private void profileClass(ClientUtils.SourceCodeSelection value) {
        selectForProfiling(new ClientUtils.SourceCodeSelection(
                           value.getClassName(), Wildcards.ALLWILDCARD, null));
    }
    
    
    private void initUI(Action saveAction, final Action compareAction, Action infoAction, ExportUtils.Exportable exportProvider) {
        setLayout(new BorderLayout(0, 0));
        
        forwardCallsView = new CPUTreeTableView(null, false) {
            protected void performDefaultAction(ClientUtils.SourceCodeSelection value) {
                if (showSourceSupported()) showSource(value);
            }
            protected void populatePopup(JPopupMenu popup, Object value, ClientUtils.SourceCodeSelection userValue) {
                SnapshotCPUView.this.populatePopup(forwardCallsView, popup, value, userValue);
            }
        };
        forwardCallsView.notifyOnFocus(new Runnable() {
            public void run() { lastFocused = forwardCallsView; }
        });
        
        hotSpotsView = new CPUTableView(null) {
            protected void performDefaultAction(ClientUtils.SourceCodeSelection userValue) {
                if (showSourceSupported()) showSource(userValue);
            }
            protected void populatePopup(JPopupMenu popup, Object value, ClientUtils.SourceCodeSelection userValue) {
                SnapshotCPUView.this.populatePopup(hotSpotsView, popup, value, userValue);
            }
        };
        hotSpotsView.notifyOnFocus(new Runnable() {
            public void run() { lastFocused = hotSpotsView; }
        });
        
        reverseCallsView = new CPUTreeTableView(null, true) {
            protected void performDefaultAction(ClientUtils.SourceCodeSelection value) {
                if (showSourceSupported()) showSource(value);
            }
            protected void populatePopup(JPopupMenu popup, Object value, ClientUtils.SourceCodeSelection userValue) {
                SnapshotCPUView.this.populatePopup(reverseCallsView, popup, value, userValue);
            }
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
                        divider.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, c));
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
                        divider.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, c));
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
        
        ProfilerToolbar toolbar = ProfilerToolbar.create(true);
        
        if (saveAction != null) toolbar.add(saveAction);
        
        toolbar.add(ExportUtils.exportButton(this, CPUView.EXPORT_TOOLTIP, getExportables(exportProvider)));
        
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
        
        toolbar.addSpace(2);
        toolbar.addSeparator();
        toolbar.addSpace(5);
        
        GrayLabel viewL = new GrayLabel(TOOLBAR_VIEW);
        toolbar.add(viewL);
        
        toolbar.addSpace(5);
        
        MultiButtonGroup group = new MultiButtonGroup();
        toggles = new JToggleButton[3];
        
        toggles[0] = new JToggleButton(Icons.getIcon(ProfilerIcons.NODE_FORWARD)) {
            @Override
            protected void fireActionPerformed(ActionEvent e) {
                super.fireActionPerformed(e);
                setView(isSelected(), hotSpotsView.isVisible(), reverseCallsView.isVisible());
            }
        };
        toggles[0].setToolTipText(VIEW_FORWARD);
        group.add(toggles[0]);
        toolbar.add(toggles[0]);
        forwardCallsView.setVisible(true);
        toggles[0].setSelected(true);
        
        toggles[1] = new JToggleButton(Icons.getIcon(ProfilerIcons.TAB_HOTSPOTS)) {
            @Override
            protected void fireActionPerformed(ActionEvent e) {
                super.fireActionPerformed(e);
                setView(forwardCallsView.isVisible(), isSelected(), reverseCallsView.isVisible());
            }
        };
        toggles[1].setToolTipText(VIEW_HOTSPOTS);
        group.add(toggles[1]);
        toolbar.add(toggles[1]);
        hotSpotsView.setVisible(false);
        toggles[1].setSelected(false);
        
        toggles[2] = new JToggleButton(Icons.getIcon(ProfilerIcons.NODE_REVERSE)) {
            @Override
            protected void fireActionPerformed(ActionEvent e) {
                super.fireActionPerformed(e);
                setView(forwardCallsView.isVisible(), hotSpotsView.isVisible(), isSelected());
            }
        };
        toggles[2].setToolTipText(VIEW_REVERSE);
        group.add(toggles[2]);
        toolbar.add(toggles[2]);
        reverseCallsView.setVisible(false);
        toggles[2].setSelected(false);
        
//        Action aCallTree = new AbstractAction() {
//            { putValue(NAME, VIEW_CALLTREE); }
//            public void actionPerformed(ActionEvent e) { setView(true, false); }
//            
//        };
//        Action aHotSpots = new AbstractAction() {
//            { putValue(NAME, VIEW_HOTSPOTS); }
//            public void actionPerformed(ActionEvent e) { setView(false, true); }
//            
//        };
//        Action aCombined = new AbstractAction() {
//            { putValue(NAME, VIEW_COMBINED); }
//            public void actionPerformed(ActionEvent e) { setView(true, true); }
//            
//        };
//        toolbar.add(new ActionPopupButton(2, aCallTree, aHotSpots, aCombined));
        
        toolbar.addSpace(5);
        threadsSelector = new ThreadsSelector() {
            protected CPUResultsSnapshot getSnapshot() { return snapshot; }
            protected void selectionChanged(Collection<Integer> selected, boolean mergeThreads) {
                mergedThreads = mergeThreads;
                selectedThreads = selected;
                setAggregation(aggregation);
            }
            
        };
        toolbar.add(threadsSelector);
        
        toolbar.addSpace(2);
        toolbar.addSeparator();
        toolbar.addSpace(5);
        
//        GrayLabel threadsL = new GrayLabel("Threads:");
//        toolbar.add(threadsL);
//        
//        toolbar.addSpace(2);
//        
//        PopupButton threads = new PopupButton("All threads") {
//            protected void populatePopup(JPopupMenu popup) {
//                popup.add(new JRadioButtonMenuItem("All threads"));
//                popup.add(new JRadioButtonMenuItem("main"));
//                popup.add(new JRadioButtonMenuItem("AWT-EventQueue-0"));
//            }
//        };
//        toolbar.add(threads);
//        
//        toolbar.addSpace(2);
//        toolbar.addSeparator();
//        toolbar.addSpace(5);
        
        GrayLabel aggregationL = new GrayLabel(TOOLBAR_AGGREGATION);
        toolbar.add(aggregationL);
        
        toolbar.addSpace(2);
        
        Action aMethods = new AbstractAction() {
            { putValue(NAME, AGGREGATION_METHODS); }
            public void actionPerformed(ActionEvent e) { setAggregation(CPUResultsSnapshot.METHOD_LEVEL_VIEW); }
            
        };
        Action aClasses = new AbstractAction() {
            { putValue(NAME, AGGREGATION_CLASSES); }
            public void actionPerformed(ActionEvent e) { setAggregation(CPUResultsSnapshot.CLASS_LEVEL_VIEW); }
            
        };
        Action aPackages = new AbstractAction() {
            { putValue(NAME, AGGREGATION_PACKAGES); }
            public void actionPerformed(ActionEvent e) { setAggregation(CPUResultsSnapshot.PACKAGE_LEVEL_VIEW); }
            
        };
        
        ActionPopupButton aggregation = new ActionPopupButton(aMethods, aClasses, aPackages);
        toolbar.add(aggregation);
        
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
            if (forwardCallsView.isShowing()) lastFocused = forwardCallsView;
            else if (hotSpotsView.isShowing()) lastFocused = hotSpotsView;
            else if (reverseCallsView.isShowing()) lastFocused = reverseCallsView;
        }
        
        return lastFocused;
    }
    
    private void populatePopup(final DataView invoker, JPopupMenu popup, final Object value, final ClientUtils.SourceCodeSelection userValue) {
        if (showSourceSupported()) {
            popup.add(new JMenuItem(CPUView.ACTION_GOTOSOURCE) {
                { setEnabled(userValue != null && aggregation != CPUResultsSnapshot.PACKAGE_LEVEL_VIEW); setFont(getFont().deriveFont(Font.BOLD)); }

                @Override
                protected void fireActionPerformed(ActionEvent e) { showSource(userValue); }
            });
            popup.addSeparator();
        }
        
        if (profileMethodSupported()) popup.add(new JMenuItem(CPUView.ACTION_PROFILE_METHOD) {
            { setEnabled(profileMethodEnabled() && userValue != null && aggregation == CPUResultsSnapshot.METHOD_LEVEL_VIEW && CPUTableView.isSelectable(userValue)); }

            @Override
            protected void fireActionPerformed(ActionEvent e) { profileMethod(userValue); }
        });
        
        if (profileClassSupported()) popup.add(new JMenuItem(CPUView.ACTION_PROFILE_CLASS) {
            { setEnabled(userValue != null && aggregation != CPUResultsSnapshot.PACKAGE_LEVEL_VIEW); }

            @Override
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

                @Override
                protected void fireActionPerformed(ActionEvent e) {
                    ProfilerTable table = hotSpotsView.getResultsComponent();
                    if (SearchUtils.findString(table, searchString)) {
                        toggles[1].setSelected(true);
                        hotSpotsView.setVisible(true);
                        table.requestFocusInWindow();
                    }
                }
            });
            
            popup.add(new JMenuItem(CPUView.FIND_IN_REVERSECALLS) {
                { setEnabled(userValue != null); }

                @Override
                protected void fireActionPerformed(ActionEvent e) {
                    ProfilerTreeTable table = (ProfilerTreeTable)reverseCallsView.getResultsComponent();
                    if (SearchUtils.findString(table, searchString, true, true, createSearchHelper())) {
                        toggles[2].setSelected(true);
                        reverseCallsView.setVisible(true);
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

                @Override
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

                @Override
                protected void fireActionPerformed(ActionEvent e) {
                    PrestimeCPUCCTNode thread = (PrestimeCPUCCTNode)value;
                    threadsSelector.removeThread(thread.getThreadId());
                }
            });
            
            JMenu expand = new JMenu(CPUView.EXPAND_MENU);
            popup.add(expand);
            
            expand.add(new JMenuItem(CPUView.EXPAND_PLAIN_ITEM) {
                @Override
                protected void fireActionPerformed(ActionEvent e) {
                    ttable.expandPlainPath(ttable.getSelectedRow(), 2);
                }
            });
            
            expand.add(new JMenuItem(CPUView.EXPAND_TOPMOST_ITEM) {
                @Override
                protected void fireActionPerformed(ActionEvent e) {
                    ttable.expandFirstPath(ttable.getSelectedRow());
                }
            });
            
            expand.addSeparator();
            
            expand.add(new JMenuItem(CPUView.COLLAPSE_CHILDREN_ITEM) {
                @Override
                protected void fireActionPerformed(ActionEvent e) {
                    ttable.collapseChildren(ttable.getSelectedRow());
                }
            });
            
            expand.add(new JMenuItem(CPUView.COLLAPSE_ALL_ITEM) {
                @Override
                protected void fireActionPerformed(ActionEvent e) {
                    ttable.collapseAll();
                }
            });
        } else if (invoker == hotSpotsView) {
            // Ugly hack - there's a space between method name and parameters
            final String searchString = value.toString().replace("(", " ("); // NOI18N
            
            popup.add(new JMenuItem(CPUView.FIND_IN_FORWARDCALLS) {
                { setEnabled(userValue != null); }

                @Override
                protected void fireActionPerformed(ActionEvent e) {
                    ProfilerTable table = forwardCallsView.getResultsComponent();
                    if (SearchUtils.findString(table, searchString)) {
                        toggles[0].setSelected(true);
                        forwardCallsView.setVisible(true);
                        table.requestFocusInWindow();
                    }
                }
            });
            
            popup.add(new JMenuItem(CPUView.FIND_IN_REVERSECALLS) {
                { setEnabled(userValue != null); }

                @Override
                protected void fireActionPerformed(ActionEvent e) {
                    ProfilerTreeTable table = (ProfilerTreeTable)reverseCallsView.getResultsComponent();
                    if (SearchUtils.findString(table, searchString, true, true, createSearchHelper())) {
                        toggles[2].setSelected(true);
                        reverseCallsView.setVisible(true);
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

                @Override
                protected void fireActionPerformed(ActionEvent e) {
                    ProfilerTable table = forwardCallsView.getResultsComponent();
                    if (SearchUtils.findString(table, searchString)) {
                        toggles[0].setSelected(true);
                        forwardCallsView.setVisible(true);
                        table.requestFocusInWindow();
                    }
                }
            });
            
            popup.add(new JMenuItem(CPUView.FIND_IN_HOTSPOTS) {
                { setEnabled(userValue != null); }

                @Override
                protected void fireActionPerformed(ActionEvent e) {
                    ProfilerTable table = hotSpotsView.getResultsComponent();
                    if (SearchUtils.findString(table, searchString)) {
                        toggles[1].setSelected(true);
                        hotSpotsView.setVisible(true);
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

                @Override
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

                @Override
                protected void fireActionPerformed(ActionEvent e) {
                    PrestimeCPUCCTNode thread = (PrestimeCPUCCTNode)value;
                    threadsSelector.removeThread(thread.getThreadId());
                }
            });
            
            JMenu expand = new JMenu(CPUView.EXPAND_MENU);
            popup.add(expand);
            
            expand.add(new JMenuItem(CPUView.EXPAND_PLAIN_ITEM) {
                @Override
                protected void fireActionPerformed(ActionEvent e) {
                    ttable.expandPlainPath(ttable.getSelectedRow(), 1);
                }
            });
            
            expand.add(new JMenuItem(CPUView.EXPAND_TOPMOST_ITEM) {
                @Override
                protected void fireActionPerformed(ActionEvent e) {
                    ttable.expandFirstPath(ttable.getSelectedRow());
                }
            });
            
            expand.addSeparator();
            
            expand.add(new JMenuItem(CPUView.COLLAPSE_CHILDREN_ITEM) {
                @Override
                protected void fireActionPerformed(ActionEvent e) {
                    ttable.collapseChildren(ttable.getSelectedRow());
                }
            });
            
            expand.add(new JMenuItem(CPUView.COLLAPSE_ALL_ITEM) {
                @Override
                protected void fireActionPerformed(ActionEvent e) {
                    ttable.collapseAll();
                }
            });
        }
        
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
    
    private void setView(boolean forwardCalls, boolean hotSpots, boolean reverseCalls) {
        forwardCallsView.setVisible(forwardCalls);
        hotSpotsView.setVisible(hotSpots);
        reverseCallsView.setVisible(reverseCalls);
    }
    
    private void setAggregation(int _aggregation) {
        aggregation = _aggregation;
        
        getExecutor().submit(new Runnable() {
            public void run() {
                final CPUResultsSnapshot _snapshot = refSnapshot == null ? snapshot :
                                               snapshot.createDiff(refSnapshot);

                final FlatProfileContainer flatData = _snapshot.getFlatProfile(selectedThreads, aggregation);

                final Map<Integer, ClientUtils.SourceCodeSelection> idMap = _snapshot.getMethodIDMap(aggregation);
                
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        boolean diff = _snapshot instanceof CPUResultsDiff;
                        forwardCallsView.setData(_snapshot, idMap, aggregation, selectedThreads, mergedThreads, sampled, diff);
                        hotSpotsView.setData(flatData, idMap, sampled, diff);
                        reverseCallsView.setData(_snapshot, idMap, aggregation, selectedThreads, mergedThreads, sampled, diff);
                    }
                });
            }
        });
    }
    
    protected final void setSnapshot(CPUResultsSnapshot snapshot, boolean sampled) {
        this.snapshot = snapshot;
        this.sampled = sampled;
        
        setAggregation(aggregation);
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
                    return forwardCallsView.isVisible();
                }
                public String getName() {
                    return MessageFormat.format(CPUView.EXPORT_METHODS, CPUView.EXPORT_FORWARD_CALLS);
                }
                public ExportUtils.ExportProvider[] getProviders() {
                    return forwardCallsView.getExportProviders();
                }
            },
            new ExportUtils.Exportable() {
                public boolean isEnabled() {
                    return hotSpotsView.isVisible();
                }
                public String getName() {
                    return MessageFormat.format(CPUView.EXPORT_METHODS, CPUView.EXPORT_HOTSPOTS);
                }
                public ExportUtils.ExportProvider[] getProviders() {
                    return hotSpotsView.getExportProviders();
                }
            },
            new ExportUtils.Exportable() {
                public boolean isEnabled() {
                    return reverseCallsView.isVisible();
                }
                public String getName() {
                    return MessageFormat.format(CPUView.EXPORT_METHODS, CPUView.EXPORT_REVERSE_CALLS);
                }
                public ExportUtils.ExportProvider[] getProviders() {
                    return reverseCallsView.getExportProviders();
                }
            }
        };
    }
    
    private synchronized ExecutorService getExecutor() {
        if (executor == null) executor = Executors.newSingleThreadExecutor();
        return executor;
    }
    
}
