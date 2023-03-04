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

import org.netbeans.lib.profiler.global.CommonConstants;
import org.netbeans.lib.profiler.results.CCTNode;
import org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot;
import org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNode;
import org.netbeans.lib.profiler.ui.ResultsPanel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreePath;
import org.netbeans.lib.profiler.ui.AppearanceController;
import org.netbeans.modules.profiler.api.GoToSource;


/**
 * Common superclass for all results displays displaying CPU results.
 *
 * @author Misha Dmitriev
 * @author Ian Formanek
 * @author Jiri Sedlacek
 */
public abstract class CPUResultsPanel extends ResultsPanel implements CommonConstants {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // -----
    // I18N String constants
    private static final ResourceBundle messages = ResourceBundle.getBundle("org.netbeans.lib.profiler.ui.cpu.Bundle"); // NOI18N
    private static final String GO_TO_SOURCE_ITEM_NAME = messages.getString("CPUResultsPanel_GoToSourceItemName"); // NOI18N
    private static final String BACKTRACES_ITEM_NAME = messages.getString("CPUResultsPanel_BackTracesItemName"); // NOI18N
    private static final String SUBTREE_ITEM_NAME = messages.getString("CPUResultsPanel_SubtreeItemName"); // NOI18N
    private static final String ROOT_METHODS_ITEM_NAME = messages.getString("CPUResultsPanel_RootMethodsItemName"); // NOI18N
                                                                                                                    // -----

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    protected CPUResUserActionsHandler actionsHandler;
    protected JMenuItem popupAddToRoots;
    protected JMenuItem popupFind;
    protected JMenuItem popupShowReverse;
    protected JMenuItem popupShowSource;
    protected JMenuItem popupShowSubtree;
    protected JPopupMenu callGraphPopupMenu;
    protected JPopupMenu cornerPopup;
    protected TreePath popupPath;
    protected String[] columnNames;
    protected TableCellRenderer[] columnRenderers;
    protected String[] columnToolTips;
    protected int[] columnWidths;
    protected boolean[] columnsVisibility;
    protected int columnCount = 0;
    protected int currentView; // View AKA aggregation level: CPUResultsSnapshot.METHOD_LEVEL, CLASS_LEVEL or PACKAGE_LEVEL
    protected int methodId;
    private Boolean sampling;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public CPUResultsPanel(CPUResUserActionsHandler actionsHandler, Boolean sampling) {
        this.actionsHandler = actionsHandler;
        this.sampling = sampling;
        callGraphPopupMenu = createPopupMenu();

        if (popupFind != null) {
            popupFind.setVisible(false);
        }
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    /** Returns a meaningful value only for those subclasses that present data for a single thread */
    public abstract int getCurrentThreadId();

    public int getCurrentView() {
        return currentView;
    }

    public JMenuItem getPopupFindItem() {
        return popupFind;
    }

    // Should be overridden whenever possible
    public boolean getSortingOrder() {
        return false;
    }
    
    public Boolean isSampling() {
        return sampling;
    }

    /** Changes the aggregation level for the CPU Results
     *
     * @param view one of CPUResultsSnapshot.METHOD_LEVEL_VIEW, CPUResultsSnapshot.CLASS_LEVEL_VIEW, CPUResultsSnapshot.PACKAGE_LEVEL_VIEW
     *
     * @see CPUResultsSnapshot.METHOD_LEVEL_VIEW
     * @see CPUResultsSnapshot.CLASS_LEVEL_VIEW
     * @see CPUResultsSnapshot.PACKAGE_LEVEL_VIEW
     */
    public void changeView(int view) {
        if (currentView == view) {
            return;
        }

        currentView = view;

        if (popupShowSource != null) popupShowSource.setEnabled(isShowSourceAvailable());
        if (popupAddToRoots != null) {
            popupAddToRoots.setEnabled(isAddToRootsAvailable());
        }

        actionsHandler.viewChanged(view); // notify the actions handler about this
    }

    public abstract void reset();

    // Should be overridden whenever possible
    public int getSortingColumn() {
        return CommonConstants.SORTING_COLUMN_DEFAULT;
    }

    protected boolean isAddToRootsAvailable() {
        return (currentView == CPUResultsSnapshot.METHOD_LEVEL_VIEW);
    }

    protected abstract String[] getMethodClassNameAndSig(int methodId, int currentView);

    protected abstract String getSelectedMethodName();

    protected boolean isShowSourceAvailable() {
        return (currentView != CPUResultsSnapshot.PACKAGE_LEVEL_VIEW);
    }

    // ------------------------------------------------------------------
    // Popup menu behavior
    protected JPopupMenu createPopupMenu() {
        JPopupMenu popup = new JPopupMenu();
        if (GoToSource.isAvailable()) popupShowSource = new JMenuItem();
        if (AppearanceController.getDefault().isAddToRootsVisible()) {
            popupAddToRoots = new JMenuItem();
        }
        popupFind = new JMenuItem();

        Font boldfont = popup.getFont().deriveFont(Font.BOLD);

        ActionListener menuListener = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                menuActionPerformed(evt);
            }
        };

        boolean separator = true;
        if (popupShowSource != null) {
            popupShowSource.setFont(boldfont);
            popupShowSource.setText(GO_TO_SOURCE_ITEM_NAME);
            popup.add(popupShowSource);
            separator = false;
        }

        if (supportsSubtreeCallGraph()) {
            if (!separator) {
                popup.addSeparator();
                separator = true;
            }

            popupShowSubtree = new JMenuItem();
            popupShowSubtree.setText(SUBTREE_ITEM_NAME);
            popup.add(popupShowSubtree);
            popupShowSubtree.addActionListener(menuListener);
        }

        if (supportsReverseCallGraph()) {
            if (!separator) {
                popup.addSeparator();
                separator = true;
            }

            popupShowReverse = new JMenuItem();
            popupShowReverse.setText(BACKTRACES_ITEM_NAME);
            popup.add(popupShowReverse);
            popupShowReverse.addActionListener(menuListener);
        }

        popup.add(popupFind);

        if (popupAddToRoots != null) {
            popup.addSeparator();

            popupAddToRoots.setText(ROOT_METHODS_ITEM_NAME);
            popup.add(popupAddToRoots);
            popupAddToRoots.addActionListener(menuListener);
        }
        
        if (popupShowSource != null) popupShowSource.addActionListener(menuListener);
        popupFind.addActionListener(menuListener);

        return popup;
    }

    protected void performDefaultAction() {
        if (popupPath != null) {
            showSourceForMethod(popupPath);
        } else {
            showSourceForMethod(methodId);
        }
    }

    protected void showSourceForMethod(int methodId) {
        if (currentView != CPUResultsSnapshot.PACKAGE_LEVEL_VIEW) {
            boolean methodLevelView = (currentView == CPUResultsSnapshot.METHOD_LEVEL_VIEW);
            String[] classMethodAndSig = getMethodClassNameAndSig(methodId, currentView);
            actionsHandler.showSourceForMethod(classMethodAndSig[0], methodLevelView ? classMethodAndSig[1] : null,
                                               methodLevelView ? classMethodAndSig[2] : null);
        }
    }

    protected void showSourceForMethod(TreePath popupPath) {
        PrestimeCPUCCTNode node = (PrestimeCPUCCTNode)popupPath.getLastPathComponent();
        if (node.getMethodId() == 0 || node.isFiltered()) return;
        
        if (currentView != CPUResultsSnapshot.PACKAGE_LEVEL_VIEW) {
            boolean methodLevelView = (currentView == CPUResultsSnapshot.METHOD_LEVEL_VIEW);
            String[] classMethodAndSig = getMethodClassNameAndSig(node.getMethodId(), currentView);
            actionsHandler.showSourceForMethod(classMethodAndSig[0], methodLevelView ? classMethodAndSig[1] : null,
                                               methodLevelView ? classMethodAndSig[2] : null);
        }
    }

    protected abstract boolean supportsReverseCallGraph();

    protected abstract boolean supportsSubtreeCallGraph();

    protected void showReverseCallGraph(int threadId, int methodId, int currentView, int sortingColumn, boolean sortingOrder) {
        // do nothing, has to be overridden by classes that do support showing reverse call graphs and return
        // true from supportsReverseCallGraph
    }

    protected void showSubtreeCallGraph(final CCTNode node, int currentView, int sortingColumn, boolean sortingOrder) {
        // do nothing, has to be overridden by classes that do support showing subtree call graphs and return
        // true from supportsSubtreeCallGraph
    }
    
    public void setColumnsVisibility(boolean[] columnsVisibility) {}
    
    public boolean[] getColumnsVisibility() { return null; }

    void menuActionPerformed(ActionEvent evt) {
        Object src = evt.getSource();

        if (src == popupShowSource && popupShowSource != null) {
            performDefaultAction();
        } else if (src == popupShowReverse) {
            int threadId = 0;

            if (popupPath != null) {
                PrestimeCPUCCTNode selectedNode = (PrestimeCPUCCTNode) popupPath.getLastPathComponent();

                if (selectedNode.getParent() == null) {
                    return; // Nothing to do for root node
                }

                if (selectedNode.isSelfTimeNode()) {
                    selectedNode = (PrestimeCPUCCTNode) selectedNode.getParent();
                }

                if (selectedNode == null) {
                    return; // Nothing to do for root node
                }

                if (selectedNode.getMethodId() == 0) {
                    if (selectedNode.getNChildren() > 0) {
                        methodId = ((PrestimeCPUCCTNode) selectedNode.getChild(0)).getMethodId();
                    }
                } else {
                    methodId = selectedNode.getMethodId();
                }

                threadId = selectedNode.getThreadId();
            } else {
                // methodId is already set
                threadId = getCurrentThreadId(); // It's a flat profile window or something and we request a path for its single thread
            }

            showReverseCallGraph(threadId, methodId, currentView, getSortingColumn(), getSortingOrder());
        } else if (src == popupShowSubtree) {
            if (popupPath != null) {
                if (popupPath.getParentPath() == null) {
                    return; // Nothing to do for root node
                }

                PrestimeCPUCCTNode selectedNode = (PrestimeCPUCCTNode) popupPath.getLastPathComponent();

                if (selectedNode.isSelfTimeNode()) {
                    selectedNode = (PrestimeCPUCCTNode) selectedNode.getParent();
                }

                if (selectedNode == null) {
                    return; // Nothing to do for root node
                }

                showSubtreeCallGraph(selectedNode, currentView, getSortingColumn(), getSortingOrder());
            }
        } else if (src == popupAddToRoots) {
            if (popupPath != null) {
                PrestimeCPUCCTNode selectedNode = (PrestimeCPUCCTNode) popupPath.getLastPathComponent();

                if (selectedNode.getMethodId() == 0) {
                    if (selectedNode.getParent() instanceof PrestimeCPUCCTNode) {
                        methodId = ((PrestimeCPUCCTNode) selectedNode.getParent()).getMethodId();
                    }
                } else {
                    methodId = selectedNode.getMethodId();
                }
            } // else methodId is already set

            String[] methodClassNameAndSig = getMethodClassNameAndSig(methodId, currentView);
            actionsHandler.addMethodToRoots(methodClassNameAndSig[0], methodClassNameAndSig[1], methodClassNameAndSig[2]);
        } else if (src == popupFind) {
            actionsHandler.find(this, getSelectedMethodName());
        }
    }
}
