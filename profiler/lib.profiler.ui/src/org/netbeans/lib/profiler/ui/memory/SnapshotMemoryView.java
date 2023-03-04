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
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.filters.GenericFilter;
import org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot;
import org.netbeans.lib.profiler.results.memory.AllocMemoryResultsSnapshot;
import org.netbeans.lib.profiler.results.memory.LivenessMemoryResultsSnapshot;
import org.netbeans.lib.profiler.results.memory.MemoryResultsSnapshot;
import org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode;
import org.netbeans.lib.profiler.results.memory.SampledMemoryResultsSnapshot;
import org.netbeans.lib.profiler.ui.components.ProfilerToolbar;
import org.netbeans.lib.profiler.ui.results.DataView;
import org.netbeans.lib.profiler.ui.swing.ExportUtils;
import org.netbeans.lib.profiler.ui.swing.FilterUtils;
import org.netbeans.lib.profiler.ui.swing.ProfilerTreeTable;
import org.netbeans.lib.profiler.ui.swing.SearchUtils;
import org.netbeans.lib.profiler.utils.Wildcards;

/**
 *
 * @author Jiri Sedlacek
 */
public abstract class SnapshotMemoryView extends JPanel {
    
    // -----
    // I18N String constants
    private static final ResourceBundle messages = ResourceBundle.getBundle("org.netbeans.lib.profiler.ui.memory.Bundle"); // NOI18N
    private static final String COMPARE_SNAPSHOTS = messages.getString("SnapshotMemoryView_CompareSnapshots"); // NOI18N
    private static final String RESET_COMPARE_SNAPSHOTS = messages.getString("SnapshotMemoryView_ResetCompareSnapshots"); // NOI18N
//    private static final String TOOLBAR_AGGREGATION = messages.getString("SnapshotMemoryView_ToolbarAggregation"); // NOI18N
//    private static final String AGGREGATION_CLASSES = messages.getString("SnapshotMemoryView_AggregationClasses"); // NOI18N
//    private static final String AGGREGATION_PACKAGES = messages.getString("SnapshotMemoryView_AggregationPackages"); // NOI18N
    // -----
    
    private final MemoryView dataView;
    
    private int aggregation;
    private final GenericFilter filter;
    private final MemoryResultsSnapshot snapshot;
    private MemoryResultsSnapshot refSnapshot;
    
    private JToggleButton compareButton;
    
    
    public SnapshotMemoryView(MemoryResultsSnapshot snapshot, GenericFilter filter, Action saveAction, final Action compareAction, Action infoAction, ExportUtils.Exportable exportProvider) {
        this.filter = filter;
        this.snapshot = snapshot;
        
        // class names in VM format
        MemoryView.userFormClassNames(snapshot);
        
        setLayout(new BorderLayout());
        
//        boolean supportsPackageAggregation = true;
        
        if (snapshot instanceof SampledMemoryResultsSnapshot) {
            dataView = new SampledTableView(null) {
                protected void performDefaultAction(ClientUtils.SourceCodeSelection userValue) {
                    if (showSourceSupported()) showSource(userValue);
                }
                protected void populatePopup(JPopupMenu popup, Object value, ClientUtils.SourceCodeSelection userValue) {
                    SnapshotMemoryView.this.populatePopup(this, popup, value, userValue);
                }
            };
        } else if (snapshot instanceof AllocMemoryResultsSnapshot) {
            if (snapshot.containsStacks()) {
                dataView = new AllocTreeTableView(null) {
                    protected void performDefaultAction(ClientUtils.SourceCodeSelection userValue) {
                        if (showSourceSupported()) showSource(userValue);
                    }
                    protected void populatePopup(JPopupMenu popup, Object value, ClientUtils.SourceCodeSelection userValue) {
                        SnapshotMemoryView.this.populatePopup(this, popup, value, userValue);
                    }
                };
//                supportsPackageAggregation = false;
            } else {
                dataView = new AllocTableView(null) {
                    protected void performDefaultAction(ClientUtils.SourceCodeSelection userValue) {
                        if (showSourceSupported()) showSource(userValue);
                    }
                    protected void populatePopup(JPopupMenu popup, Object value, ClientUtils.SourceCodeSelection userValue) {
                        SnapshotMemoryView.this.populatePopup(this, popup, value, userValue);
                    }
                };
            }
        } else if (snapshot instanceof LivenessMemoryResultsSnapshot) {
            if (snapshot.containsStacks()) {
                dataView = new LivenessTreeTableView(null, filter == null) {
                    protected void performDefaultAction(ClientUtils.SourceCodeSelection userValue) {
                        if (showSourceSupported()) showSource(userValue);
                    }
                    protected void populatePopup(JPopupMenu popup, Object value, ClientUtils.SourceCodeSelection userValue) {
                        SnapshotMemoryView.this.populatePopup(this, popup, value, userValue);
                    }
                };
//                supportsPackageAggregation = false;
            } else {
                dataView = new LivenessTableView(null, filter == null) {
                    protected void performDefaultAction(ClientUtils.SourceCodeSelection userValue) {
                        if (showSourceSupported()) showSource(userValue);
                    }
                    protected void populatePopup(JPopupMenu popup, Object value, ClientUtils.SourceCodeSelection userValue) {
                        SnapshotMemoryView.this.populatePopup(this, popup, value, userValue);
                    }
                };
            }
        } else {
            dataView = null;
        }
        
        ProfilerToolbar toolbar = ProfilerToolbar.create(true);
        
        if (saveAction != null) toolbar.add(saveAction);
        
        toolbar.add(ExportUtils.exportButton(this, MemoryView.EXPORT_TOOLTIP, getExportables(exportProvider)));
        
        if (compareAction != null) {
            toolbar.addSpace(2);
            toolbar.addSeparator();
            toolbar.addSpace(2);
        
            Icon icon = (Icon)compareAction.getValue(Action.SMALL_ICON);
            compareButton = new JToggleButton(icon) {
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
        
//        toolbar.addSpace(2);
//        toolbar.addSeparator();
//        toolbar.addSpace(5);
        
//        GrayLabel aggregationL = new GrayLabel(TOOLBAR_AGGREGATION);
//        toolbar.add(aggregationL);
//        
//        toolbar.addSpace(2);
//        
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
//        ActionPopupButton aggregation = new ActionPopupButton(0, aClasses, aPackages);
//        aggregation.setEnabled(supportsPackageAggregation);
//        toolbar.add(aggregation);
        
        if (infoAction != null) {
            toolbar.addFiller();
            toolbar.add(infoAction);
        }
        
        if (dataView != null) add(dataView, BorderLayout.CENTER);
        add(toolbar.getComponent(), BorderLayout.NORTH);
        
        setAggregation(CPUResultsSnapshot.CLASS_LEVEL_VIEW);
        
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
    
    
    public void setRefSnapshot(MemoryResultsSnapshot snapshot) {
        // class names in VM format
        MemoryView.userFormClassNames(snapshot);
        
        refSnapshot = snapshot;
        if (compareButton != null && snapshot != null) {
            compareButton.setSelected(true);
            compareButton.setToolTipText(RESET_COMPARE_SNAPSHOTS);
        }
        
        setAggregation(aggregation);
    }
    
    
    protected boolean profileMethodSupported() { return true; }
    
    protected boolean profileClassSupported() { return true; }
    
    
    protected abstract boolean showSourceSupported();
    
    protected abstract void showSource(ClientUtils.SourceCodeSelection value);
    
    protected abstract void selectForProfiling(ClientUtils.SourceCodeSelection value);
    
    
    private void profileMethod(ClientUtils.SourceCodeSelection value) {
        selectForProfiling(value);
    }
    
    private void profileClass(ClientUtils.SourceCodeSelection value) {
        selectForProfiling(new ClientUtils.SourceCodeSelection(
                           value.getClassName(), Wildcards.ALLWILDCARD, null));
    }
    
    
    // TODO: implement isSelectable()
//    // Check if primitive type/array
//    if ((methodName == null && methodSig == null) && (VMUtils.isVMPrimitiveType(className) ||
//         VMUtils.isPrimitiveType(className))) ProfilerDialogs.displayWarning(CANNOT_SHOW_PRIMITIVE_SRC_MSG);
    static boolean isSelectable(ClientUtils.SourceCodeSelection value, boolean method) {
        String className = value.getClassName();
        String methodName = value.getMethodName();
        
        if (method && methodName.endsWith("[native]")) return false; // NOI18N
        
        if (PresoObjAllocCCTNode.VM_ALLOC_CLASS.equals(className) && PresoObjAllocCCTNode.VM_ALLOC_METHOD.equals(methodName)) return false;
        
        return true;
    }
    
    private void populatePopup(final DataView invoker, JPopupMenu popup, Object value, final ClientUtils.SourceCodeSelection userValue) {
        if (showSourceSupported()) {
            popup.add(new JMenuItem(MemoryView.ACTION_GOTOSOURCE) {
                { setEnabled(userValue != null && aggregation != CPUResultsSnapshot.PACKAGE_LEVEL_VIEW); setFont(getFont().deriveFont(Font.BOLD)); }
                protected void fireActionPerformed(ActionEvent e) { showSource(userValue); }
            });
            popup.addSeparator();
        }
        
        if (profileMethodSupported()) {
            if (userValue == null || !Wildcards.ALLWILDCARD.equals(userValue.getMethodName())) {
                popup.add(new JMenuItem(MemoryView.ACTION_PROFILE_METHOD) {
                    { setEnabled(userValue != null && isSelectable(userValue, true)); }
                    protected void fireActionPerformed(ActionEvent e) { profileMethod(userValue); }
                });
            }
        }
        
        if (profileClassSupported()) popup.add(new JMenuItem(MemoryView.ACTION_PROFILE_CLASS) {
            { setEnabled(userValue != null && aggregation != CPUResultsSnapshot.PACKAGE_LEVEL_VIEW && isSelectable(userValue, false)); }
            protected void fireActionPerformed(ActionEvent e) { profileClass(userValue); }
        });
        
        if (profileMethodSupported() || profileClassSupported()) popup.addSeparator();
        
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
    
    private void setAggregation(int aggregation) {
        this.aggregation = aggregation;
        if (dataView != null) {
            if (refSnapshot == null) dataView.setData(snapshot, filter, aggregation);
            else dataView.setData(snapshot.createDiff(refSnapshot), filter, aggregation);
        }
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
                    return true;
                }
                public String getName() {
                    return MemoryView.EXPORT_OBJECTS;
                }
                public ExportUtils.ExportProvider[] getProviders() {
                    return dataView.getExportProviders();
                }
            }
        };
    }
    
}
