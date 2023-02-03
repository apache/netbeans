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

import org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode;
import org.netbeans.lib.profiler.ui.ResultsPanel;
import org.netbeans.lib.profiler.ui.components.JTreeTable;
import org.netbeans.lib.profiler.ui.components.table.CustomBarCellRenderer;
import org.netbeans.lib.profiler.ui.components.table.LabelBracketTableCellRenderer;
import org.netbeans.lib.profiler.ui.components.table.LabelTableCellRenderer;
import org.netbeans.lib.profiler.ui.components.tree.EnhancedTreeCellRenderer;
import org.netbeans.lib.profiler.ui.components.tree.MethodNameTreeCellRenderer;
import org.netbeans.lib.profiler.ui.components.treetable.ExtendedTreeTableModel;
import org.netbeans.lib.profiler.ui.components.treetable.JTreeTablePanel;
import java.awt.*;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.TreePath;
import org.netbeans.lib.profiler.ui.components.FilterComponent;
import org.netbeans.modules.profiler.api.GoToSource;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.api.icons.ProfilerIcons;


/**
 * A panel containing a reverse call graph for all allocations of instances of a given class
 *
 * @author Misha Dmitriev
 * @author Jiri Sedlacek
 */
public abstract class ReverseMemCallGraphPanel extends ResultsPanel {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // -----
    // I18N String constants
    private static final ResourceBundle messages = ResourceBundle.getBundle("org.netbeans.lib.profiler.ui.memory.Bundle"); // NOI18N
    private static final String METHOD_COLUMN_NAME = messages.getString("ReverseMemCallGraphPanel_MethodColumnName"); // NOI18N
    private static final String LIVE_BYTES_REL_COLUMN_NAME = messages.getString("ReverseMemCallGraphPanel_LiveBytesRelColumnName"); // NOI18N
    private static final String LIVE_BYTES_COLUMN_NAME = messages.getString("ReverseMemCallGraphPanel_LiveBytesColumnName"); // NOI18N
    private static final String LIVE_OBJECTS_COLUMN_NAME = messages.getString("ReverseMemCallGraphPanel_LiveObjectsColumnName"); // NOI18N
    private static final String ALLOC_OBJECTS_COLUMN_NAME = messages.getString("ReverseMemCallGraphPanel_AllocObjectsColumnName"); // NOI18N
    private static final String AVG_AGE_COLUMN_NAME = messages.getString("ReverseMemCallGraphPanel_AvgAgeColumnName"); // NOI18N
    private static final String SURVGEN_COLUMN_NAME = messages.getString("ReverseMemCallGraphPanel_SurvGenColumnName"); // NOI18N
    private static final String METHOD_COLUMN_TOOLTIP = messages.getString("ReverseMemCallGraphPanel_MethodColumnToolTip"); // NOI18N
    private static final String LIVE_BYTES_REL_COLUMN_TOOLTIP = messages.getString("ReverseMemCallGraphPanel_LiveBytesRelColumnToolTip"); // NOI18N
    private static final String LIVE_BYTES_COLUMN_TOOLTIP = messages.getString("ReverseMemCallGraphPanel_LiveBytesColumnToolTip"); // NOI18N
    private static final String LIVE_OBJECTS_COLUMN_TOOLTIP = messages.getString("ReverseMemCallGraphPanel_LiveObjectsColumnToolTip"); // NOI18N
    private static final String ALLOC_OBJECTS_COLUMN_TOOLTIP = messages.getString("ReverseMemCallGraphPanel_AllocObjectsColumnToolTip"); // NOI18N
    private static final String AVG_AGE_COLUMN_TOOLTIP = messages.getString("ReverseMemCallGraphPanel_AvgAgeColumnToolTip"); // NOI18N
    private static final String SURVGEN_COLUMN_TOOLTIP = messages.getString("ReverseMemCallGraphPanel_SurvGenColumnToolTip"); // NOI18N
    private static final String BYTES_ALLOC_REL_COLUMN_NAME = messages.getString("ReverseMemCallGraphPanel_BytesAllocRelColumnName"); // NOI18N
    private static final String BYTES_ALLOC_COLUMN_NAME = messages.getString("ReverseMemCallGraphPanel_BytesAllocColumnName"); // NOI18N
    private static final String OBJECTS_ALLOC_COLUMN_NAME = messages.getString("ReverseMemCallGraphPanel_ObjectsAllocColumnName"); // NOI18N
    private static final String BYTES_ALLOC_REL_COLUMN_TOOLTIP = messages.getString("ReverseMemCallGraphPanel_BytesAllocRelColumnToolTip"); // NOI18N
    private static final String BYTES_ALLOC_COLUMN_TOOLTIP = messages.getString("ReverseMemCallGraphPanel_BytesAllocColumnToolTip"); // NOI18N
    private static final String OBJECTS_ALLOC_COLUMN_TOOLTIP = messages.getString("ReverseMemCallGraphPanel_ObjectsAllocColumnToolTip"); // NOI18N
    private static final String GO_SOURCE_POPUP_ITEM = messages.getString("ReverseMemCallGraphPanel_GoSourcePopupItem"); // NOI18N
    private static final String FILTER_ITEM_NAME = messages.getString("AllocResultsPanel_FilterMenuItemName"); // NOI18N
                                                                                                                         // -----

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    protected ExtendedTreeTableModel treeTableModel;
    protected JButton cornerButton;
    protected JPopupMenu headerPopup;
    protected JPopupMenu popupMenu;
    protected JTreeTable treeTable;
    protected JTreeTablePanel treeTablePanel;
    protected FilterComponent filterComponent;
    protected JMenuItem popupShowSource;
    protected MemoryResUserActionsHandler actionsHandler;
    protected TreePath treePath;
    protected String[] columnNames;
    protected TableCellRenderer[] columnRenderers;
    protected String[] columnToolTips;
    protected int[] columnWidths;
    protected boolean extendedResults; // determines Alloc./Liveness results
    protected int columnCount = 0;
    protected int minNamesColumnWidth; // minimal width of classnames columns
    CustomBarCellRenderer customBarCellRenderer;
    private EnhancedTreeCellRenderer enhancedTreeCellRenderer = new MethodNameTreeCellRenderer();
    private Icon leafIcon = Icons.getIcon(ProfilerIcons.NODE_REVERSE);
    private Icon nodeIcon = Icons.getIcon(ProfilerIcons.NODE_REVERSE);

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public ReverseMemCallGraphPanel(MemoryResUserActionsHandler actionsHandler, boolean extendedResults) {
        super();
        this.extendedResults = extendedResults;
        this.actionsHandler = actionsHandler;

        enhancedTreeCellRenderer.setLeafIcon(leafIcon);
        enhancedTreeCellRenderer.setClosedIcon(nodeIcon);
        enhancedTreeCellRenderer.setOpenIcon(nodeIcon);

        minNamesColumnWidth = getFontMetrics(getFont()).charWidth('W') * 30; // NOI18N

        headerPopup = new JPopupMenu();
        cornerButton = createHeaderPopupCornerButton(headerPopup);

        popupMenu = initPopupMenu();

        initColumnsData();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    // --- Find functionality stuff
    public void setFindString(String findString) {
        if (treeTable != null) {
            treeTable.setFindParameters(findString, 0);
        }
    }

    public String getFindString() {
        if (treeTable == null) {
            return null;
        }

        return treeTable.getFindString();
    }

    public boolean isFindStringDefined() {
        if (treeTable == null) {
            return false;
        }

        return treeTable.isFindStringDefined();
    }

    public boolean findFirst() {
        if (treeTable == null) {
            return false;
        }

        return treeTable.findFirst();
    }

    public boolean findNext() {
        if (treeTable == null) {
            return false;
        }

        return treeTable.findNext();
    }

    public boolean findPrevious() {
        if (treeTable == null) {
            return false;
        }

        return treeTable.findPrevious();
    }

    public void requestFocus() {
        if (treeTable != null) {
            SwingUtilities.invokeLater(new Runnable() { // must be invoked lazily to override default focus of first component (top-right cornerButton)
                    public void run() {
                        treeTable.requestFocus();
                    }
                });
        }
    }

    protected void setColumnsData() {
        int index;
        TableColumnModel colModel = treeTable.getColumnModel();

        treeTable.setTreeCellRenderer(enhancedTreeCellRenderer);
        colModel.getColumn(0).setPreferredWidth(minNamesColumnWidth);

        for (int i = 0; i < treeTableModel.getColumnCount(); i++) {
            index = treeTableModel.getRealColumn(i);

            if (index != 0) {
                colModel.getColumn(i).setPreferredWidth(columnWidths[index - 1]);
                colModel.getColumn(i).setCellRenderer(columnRenderers[index]);
            }
        }
    }

    protected void initColumnSelectorItems() {
        headerPopup.removeAll();

        JCheckBoxMenuItem menuItem;
        boolean columnVisible;

        for (int i = 0; i < columnCount; i++) {
            menuItem = new JCheckBoxMenuItem(columnNames[i]);
            menuItem.setActionCommand(Integer.valueOf(i).toString());
            addMenuItemListener(menuItem);

            if (treeTable != null) {
                columnVisible = treeTableModel.isRealColumnVisible(i);
                menuItem.setState(treeTableModel.isRealColumnVisible(i));

                if (i == 0) {
                    menuItem.setEnabled(false);
                }
            } else {
                menuItem.setState(true);
            }

            headerPopup.add(menuItem);
        }
        
        headerPopup.addSeparator();

        JCheckBoxMenuItem filterMenuItem = new JCheckBoxMenuItem(FILTER_ITEM_NAME);
        filterMenuItem.setActionCommand("Filter"); // NOI18N
        addMenuItemListener(filterMenuItem);

        if (filterComponent == null) {
            filterMenuItem.setState(true);
        } else {
            filterMenuItem.setState(filterComponent.getComponent().isVisible());
        }
        
        headerPopup.add(filterMenuItem);

        headerPopup.pack();
    }

    protected JPopupMenu initPopupMenu() {
        JPopupMenu popup = new JPopupMenu();
        Font boldfont = popup.getFont().deriveFont(Font.BOLD);

        if (GoToSource.isAvailable()) {
            popupShowSource = new JMenuItem();
            popupShowSource.setFont(boldfont);
            popupShowSource.setText(GO_SOURCE_POPUP_ITEM);
            popupShowSource.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        if (treePath != null) {
                            performDefaultAction(treePath);
                        }
                    }
                });
            popup.add(popupShowSource);
        }

        return popup;
    }

    void performDefaultAction(TreePath path) {
        PresoObjAllocCCTNode node = (PresoObjAllocCCTNode) path.getLastPathComponent();
        if (node.isFiltered()) return;
        String[] classMethodAndSig = node.getMethodClassNameAndSig();
        if (node.getParent() == null) showSourceForClass(classMethodAndSig[0]);
        else actionsHandler.showSourceForMethod(classMethodAndSig[0], classMethodAndSig[1], classMethodAndSig[2]);
    }
    
    private void showSourceForClass(String className) {
        className = className.replace("[]", ""); // NOI18N
        actionsHandler.showSourceForMethod(className, null, null);
    }

    private void addMenuItemListener(JCheckBoxMenuItem menuItem) {
        menuItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (e.getActionCommand().equals("Filter")) { // NOI18N
                        filterComponent.getComponent().setVisible(!filterComponent.getComponent().isVisible());

                        return;
                    }
                    
                    boolean sortResults = false;
                    int column = Integer.parseInt(e.getActionCommand());
                    boolean sortOrder = treeTable.getSortingOrder();
                    int sortingColumn = treeTable.getSortingColumn();
                    int realSortingColumn = treeTableModel.getRealColumn(sortingColumn);
                    boolean isColumnVisible = treeTableModel.isRealColumnVisible(column);

                    // Current sorting column is going to be hidden
                    if ((isColumnVisible) && (column == realSortingColumn)) {
                        // Try to set next column as a sortingColumn. If currentSortingColumn is the last column, set previous
                        // column as a sorting Column (one column is always visible).
                        sortingColumn = ((sortingColumn + 1) == treeTableModel.getColumnCount()) ? (sortingColumn - 1)
                                                                                                 : (sortingColumn + 1);
                        realSortingColumn = treeTableModel.getRealColumn(sortingColumn);
                        sortResults = true;
                    }

                    treeTableModel.setRealColumnVisibility(column, !isColumnVisible);
                    treeTable.createDefaultColumnsFromModel();
                    treeTable.updateTreeTableHeader();
                    sortingColumn = treeTableModel.getVirtualColumn(realSortingColumn);

                    if (sortResults) {
                        sortOrder = treeTableModel.getInitialSorting(sortingColumn);
                        treeTableModel.sortByColumn(sortingColumn, sortOrder);
                        treeTable.updateTreeTable();
                    }

                    treeTable.setSortingColumn(sortingColumn);
                    treeTable.setSortingOrder(sortOrder);
                    treeTable.getTableHeader().repaint();
                    setColumnsData();

                    // TODO [ui-persistence]
                }
            });
    }

    private void initColumnsData() {
        if (extendedResults) {
            columnNames = new String[] {
                              METHOD_COLUMN_NAME, LIVE_BYTES_REL_COLUMN_NAME, LIVE_BYTES_COLUMN_NAME, LIVE_OBJECTS_COLUMN_NAME,
                              ALLOC_OBJECTS_COLUMN_NAME, AVG_AGE_COLUMN_NAME, SURVGEN_COLUMN_NAME
                          };
            columnToolTips = new String[] {
                                 METHOD_COLUMN_TOOLTIP, LIVE_BYTES_REL_COLUMN_TOOLTIP, LIVE_BYTES_COLUMN_TOOLTIP,
                                 LIVE_OBJECTS_COLUMN_TOOLTIP, ALLOC_OBJECTS_COLUMN_TOOLTIP, AVG_AGE_COLUMN_TOOLTIP,
                                 SURVGEN_COLUMN_TOOLTIP,
                             };
        } else {
            columnNames = new String[] {
                              METHOD_COLUMN_NAME, BYTES_ALLOC_REL_COLUMN_NAME, BYTES_ALLOC_COLUMN_NAME, OBJECTS_ALLOC_COLUMN_NAME
                          };
            columnToolTips = new String[] {
                                 METHOD_COLUMN_TOOLTIP, BYTES_ALLOC_REL_COLUMN_TOOLTIP, BYTES_ALLOC_COLUMN_TOOLTIP,
                                 OBJECTS_ALLOC_COLUMN_TOOLTIP,
                             };
        }

        columnCount = columnNames.length;

        columnWidths = new int[columnCount - 1]; // Width of the first column fits to width
        columnRenderers = new TableCellRenderer[columnCount];

        LabelBracketTableCellRenderer labelBracketTableCellRenderer = new LabelBracketTableCellRenderer(JLabel.TRAILING);
        LabelTableCellRenderer labelTableCellRenderer = new LabelTableCellRenderer(JLabel.TRAILING);

        int maxWidth; // initial width of data columns

        if (extendedResults) {
            maxWidth = getFontMetrics(getFont()).charWidth('W') * 10; // NOI18N
        } else {
            maxWidth = getFontMetrics(getFont()).charWidth('W') * 13; // NOI18N
        }

        columnRenderers[0] = null;

        columnWidths[1 - 1] = maxWidth;
        columnRenderers[1] = null;

        for (int i = 2; i < 4; i++) {
            columnWidths[i - 1] = maxWidth;
            columnRenderers[i] = labelBracketTableCellRenderer;
        }

        for (int i = 4; i < columnNames.length; i++) {
            columnWidths[i - 1] = maxWidth;
            columnRenderers[i] = labelTableCellRenderer;
        }
    }

}
