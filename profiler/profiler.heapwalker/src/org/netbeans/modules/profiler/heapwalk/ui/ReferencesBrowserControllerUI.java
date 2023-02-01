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

package org.netbeans.modules.profiler.heapwalk.ui;


import org.netbeans.lib.profiler.heap.GCRoot;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.JavaFrameGCRoot;
import org.netbeans.lib.profiler.ui.UIConstants;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.lib.profiler.ui.components.HTMLTextArea;
import org.netbeans.lib.profiler.ui.components.JTitledPanel;
import org.netbeans.lib.profiler.ui.components.JTreeTable;
import org.netbeans.lib.profiler.ui.components.table.LabelTableCellRenderer;
import org.netbeans.lib.profiler.ui.components.treetable.AbstractTreeTableModel;
import org.netbeans.lib.profiler.ui.components.treetable.ExtendedTreeTableModel;
import org.netbeans.lib.profiler.ui.components.treetable.JTreeTablePanel;
import org.netbeans.lib.profiler.ui.components.treetable.TreeTableModel;
import org.netbeans.modules.profiler.heapwalk.ReferencesBrowserController;
import org.netbeans.modules.profiler.heapwalk.model.HeapWalkerFieldNode;
import org.netbeans.modules.profiler.heapwalk.model.HeapWalkerInstanceNode;
import org.netbeans.modules.profiler.heapwalk.model.HeapWalkerNode;
import org.netbeans.modules.profiler.heapwalk.model.InstanceNode;
import org.openide.util.NbBundle;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.TreePath;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.results.CCTNode;
import org.netbeans.lib.profiler.utils.VMUtils;
import org.netbeans.modules.profiler.api.icons.GeneralIcons;
import org.netbeans.modules.profiler.api.GoToSource;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.heapwalk.HeapFragmentWalker;
import org.netbeans.modules.profiler.heapwalk.HeapFragmentWalker.StateEvent;
import org.netbeans.modules.profiler.heapwalk.model.*;
import org.netbeans.modules.profiler.heapwalk.ui.icons.HeapWalkerIcons;
import org.openide.util.Lookup;


/**
 *
 * @author Tomas Hurka
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "ReferencesBrowserControllerUI_ViewTitleReferences=References",
    "ReferencesBrowserControllerUI_NoInstanceSelectedMsg=<b>No instance selected.</b><br><br>To view references to instance, select an instance in {0}&nbsp;Instances list.",
    "ReferencesBrowserControllerUI_ShowLoopItemText=Show Loop Origin",
    "ReferencesBrowserControllerUI_ShowInstanceItemText=Show Instance",
    "ReferencesBrowserControllerUI_ShowInClassesItemText=Show in Classes View",
    "ReferencesBrowserControllerUI_ShowGcRootItemText=Show Nearest GC Root",
    "ReferencesBrowserControllerUI_CopyPathFromRoot=Copy Path From Root",
    "ReferencesBrowserControllerUI_GoToSourceItemText=Go To Source",
    "ReferencesBrowserControllerUI_ShowInThreadsItemText=Show In Threads",
    "ReferencesBrowserControllerUI_ShowHideColumnsString=Show or hide columns",
    "ReferencesBrowserControllerUI_FieldColumnName=Field",
    "ReferencesBrowserControllerUI_FieldColumnDescr=Name of field referencing the instance",
    "ReferencesBrowserControllerUI_TypeColumnName=Type",
    "ReferencesBrowserControllerUI_TypeColumnDescr=Type of owning instance",
    "ReferencesBrowserControllerUI_FullTypeColumnName=Full Type",
    "ReferencesBrowserControllerUI_FullTypeColumnDescr=Fully qualified type of owning instance",
    "ReferencesBrowserControllerUI_ValueColumnName=Value",
    "ReferencesBrowserControllerUI_ValueColumnDescr=Number of owning instance",
    "ReferencesBrowserControllerUI_SizeColumnName=Size",
    "ReferencesBrowserControllerUI_SizeColumnDescr=Size of owning instance",
    "ReferencesBrowserControllerUI_RetainedSizeColumnName=Retained",
    "ReferencesBrowserControllerUI_RetainedSizeColumnDescr=Retained size of owning instance"
})
public class ReferencesBrowserControllerUI extends JTitledPanel {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    private class FieldsListTableKeyListener extends KeyAdapter {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        public void keyPressed(KeyEvent e) {
            if ((e.getKeyCode() == KeyEvent.VK_CONTEXT_MENU)
                    || ((e.getKeyCode() == KeyEvent.VK_F10) && (e.getModifiers() == InputEvent.SHIFT_MASK))) {
                int selectedRow = fieldsListTable.getSelectedRow();

                if (selectedRow != -1) {
                    showPopupMenu(selectedRow, -1, -1);
                }
            }
        }
    }

    // --- Listeners -------------------------------------------------------------
    private class FieldsListTableMouseListener extends MouseAdapter {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        private void updateSelection(int row) {
            fieldsListTable.requestFocusInWindow();
            if (row != -1) fieldsListTable.setRowSelectionInterval(row, row);
            else fieldsListTable.clearSelection();
        }

        public void mousePressed(final MouseEvent e) {
            final int row = fieldsListTable.rowAtPoint(e.getPoint());
            updateSelection(row);
            if (e.isPopupTrigger()) showPopupMenu(row, e.getX(), e.getY());
        }

        public void mouseReleased(MouseEvent e) {
            int row = fieldsListTable.rowAtPoint(e.getPoint());
            updateSelection(row);
            if (e.isPopupTrigger()) showPopupMenu(row, e.getX(), e.getY());
        }

        public void mouseClicked(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
                int row = fieldsListTable.rowAtPoint(e.getPoint());
                if (e.getX() >= fieldsListTable.getTree().getRowBounds(row).x -
                                fieldsListTable.getTreeCellOffsetX() && row != -1) {
                    HeapWalkerNode node = getSelectedNode(row);
                    if (node instanceof HeapWalkerInstanceNode)
                            performDefaultAction();
                }
            }
        }
    }

    // --- Table model -----------------------------------------------------------
    private class FieldsListTreeTableModel extends AbstractTreeTableModel {
        //~ Constructors ---------------------------------------------------------------------------------------------------------

        private FieldsListTreeTableModel() {
            super(ReferencesBrowserController.EMPTY_INSTANCE_NODE);
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        public Class getColumnClass(int column) {
            if (column == 0) {
                return TreeTableModel.class;
            } else {
                return Object.class;
            }
        }

        public int getColumnCount() {
            return columnCount;
        }

        public String getColumnName(int columnIndex) {
            return columnNames[columnIndex];
        }

        public String getColumnToolTipText(int col) {
            return columnToolTips[col];
        }

        public boolean isLeaf(Object node) {
            return ((HeapWalkerNode) node).isLeaf();
        }

        public Object getValueAt(Object object, int columnIndex) {
            HeapWalkerNode fieldNode = (HeapWalkerNode) object;

            switch (columnIndex) {
                case 0:
                    return fieldNode;
                case 1:
                    return fieldNode.getSimpleType();
                case 2:
                    return fieldNode.getType();
                case 3:
                    return fieldNode;
                case 4:
                    return fieldNode.getSize();
                case 5:
                    return fieldNode.getRetainedSize();
                default:
                    return null;
            }
        }
    }

    private static Icon ICON_FIELDS = Icons.getIcon(HeapWalkerIcons.INCOMING_REFERENCES);

    // --- UI definition ---------------------------------------------------------
    private static final String DATA = "Data"; // NOI18N
    private static final String NO_DATA = "No data"; // NOI18N
    private int columnCount;

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private CardLayout contents;
    private FieldTreeCellRenderer treeCellRenderer = new FieldTreeCellRenderer(false);
    private FieldsListTreeTableModel realFieldsListTableModel;
    private ExtendedTreeTableModel fieldsListTableModel;
//    private JMenuItem showClassItem;
    private JMenuItem showGcRootItem;
    private JMenuItem showInstanceItem;
    private JMenuItem showLoopOriginItem;
    private JMenuItem copyPathFromRootItem;
    private JMenuItem showSourceItem;
    private JMenuItem showInThreadsItem;
    private JPanel dataPanel;
    private JPanel noDataPanel;
    private JPopupMenu cornerPopup;
    private JPopupMenu tablePopup;
    private JTreeTable fieldsListTable;
    private ReferencesBrowserController referencesBrowserController;
    private String filterValue = ""; // NOI18N
    private String[] columnNames;
    private javax.swing.table.TableCellRenderer[] columnRenderers;
    private String[] columnToolTips;
    private int[] columnWidths;
    private boolean internalCornerButtonClick = false; // flag for closing columns popup by pressing cornerButton
    private boolean needsUpdate = true;
    private boolean sortingOrder = true;
    private int sortingColumn = 0;

    private boolean retainedSizeSupported;


    //~ Constructors -------------------------------------------------------------------------------------------------------------

    // --- Constructors ----------------------------------------------------------
    public ReferencesBrowserControllerUI(ReferencesBrowserController referencesBrowserController) {
        super(Bundle.ReferencesBrowserControllerUI_ViewTitleReferences(), ICON_FIELDS, true);

        this.referencesBrowserController = referencesBrowserController;

        retainedSizeSupported = referencesBrowserController.getReferencesControllerHandler().
                                getHeapFragmentWalker().getRetainedSizesStatus() !=
                                HeapFragmentWalker.RETAINED_SIZES_UNSUPPORTED;
        columnCount = retainedSizeSupported ? 6 : 5;

        realFieldsListTableModel = new FieldsListTreeTableModel();
        fieldsListTableModel = new ExtendedTreeTableModel(realFieldsListTableModel);

        addHierarchyListener(new HierarchyListener() {
                public void hierarchyChanged(HierarchyEvent e) {
                    if (((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) && isShowing() && needsUpdate) {
                        update();
                    }
                }
            });

        initColumnsData();
        initData();
        initComponents();

        referencesBrowserController.getReferencesControllerHandler().
            getHeapFragmentWalker().addStateListener(
                new HeapFragmentWalker.StateListener() {
                    public void stateChanged(StateEvent e) {
                        if (e.getRetainedSizesStatus() == HeapFragmentWalker.
                            RETAINED_SIZES_COMPUTED && e.isMasterChange()) {
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        fieldsListTableModel.
                                                setRealColumnVisibility(5, true);
                                        fieldsListTable.createDefaultColumnsFromModel();
                                        fieldsListTable.updateTreeTableHeader();
                                        setColumnsData();
                                    }
                                });
                        }
                    }
                }
            );

    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    // --- Internal interface ----------------------------------------------------
    public void refreshView() {
        // Used for refreshing treetable after lazy-populating the model
        if (fieldsListTable != null) {
            if (expandedPaths != null || selectedPath != null) {
                BrowserUtils.restoreState(fieldsListTable, expandedPaths, selectedPath);
                expandedPaths = null;
                selectedPath = null;
            } else {
                fieldsListTable.updateTreeTable();
            }
        }
    }

    private static final int MAX_STEP = 10;

    public void selectNode(HeapWalkerNode node) {
        CCTNode[] pathArr = fieldsListTable.getPathToRoot(node);
        selectPath(pathArr, Math.min(pathArr.length, MAX_STEP));
    }

    private void selectPath(final CCTNode[] path, final int length) {
        if (length >= path.length) {
            final CCTNode node = (CCTNode)new TreePath(path).getLastPathComponent();
            
            // --- #208900 make sure the row is visible even if displaying a scrollbar
            fieldsListTable.selectNode(node, true);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    fieldsListTable.selectNode(node, true);
                }
            });
            // --- 
        } else {
            Object[] shortPath = new Object[length];
            System.arraycopy(path, 0, shortPath, 0, length);
            final TreePath p = new TreePath(shortPath);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    selectPath(path, length + MAX_STEP);
                }
            });
        }
    }
    
    private List expandedPaths = null;
    private TreePath selectedPath = null;
    
    public List<TreePath> getExpandedPaths() {
        if (!showsData()) return null;
        return fieldsListTable.getExpandedPaths();
    }
    
    public TreePath getSelectedRow() {
        if (!showsData()) return null;
        return fieldsListTable.getTree().getSelectionPath();
    }
    
    public void restoreState(List expanded, TreePath selected) {
        if (showsData()) {
            expandedPaths = expanded;
            selectedPath = selected;
        } else {
            expandedPaths = null;
            selectedPath = null;
        }
    }
    
    private boolean showsData() {
        Object root = fieldsListTableModel.getRoot();
        return root != null &&
               root != ReferencesBrowserController.EMPTY_INSTANCE_NODE;
                
    }

    // --- Public interface ------------------------------------------------------
    public void update() {
        if (isShowing()) {
            initData();

            if (contents != null) { // ui already initialized

                if (!showsData()) {
                    contents.show(getContentPanel(), NO_DATA);
                } else {
                    contents.show(getContentPanel(), DATA);
                }

                fieldsListTable.resetTreeCellOffsetX(); // Ideally should be invoked directly on the component when root node changes
            }

            needsUpdate = false;
        } else {
            needsUpdate = true;
        }
    }

    protected void initColumnSelectorItems() {
        cornerPopup.removeAll();

        JCheckBoxMenuItem menuItem;

        for (int i = 0; i < realFieldsListTableModel.getColumnCount(); i++) {
            menuItem = new JCheckBoxMenuItem(realFieldsListTableModel.getColumnName(i));
            menuItem.setActionCommand(Integer.valueOf(i).toString());
            addMenuItemListener(menuItem);

            if (fieldsListTable != null) {
                menuItem.setState(fieldsListTableModel.isRealColumnVisible(i));

                if (i == 0) {
                    menuItem.setEnabled(false);
                }
            } else {
                menuItem.setState(true);
            }

            cornerPopup.add(menuItem);
        }

        cornerPopup.pack();
    }

    private void setColumnsData() {
        TableColumnModel colModel = fieldsListTable.getColumnModel();

        fieldsListTable.setTreeCellRenderer(treeCellRenderer);

        for (int i = 0; i < fieldsListTableModel.getColumnCount(); i++) {
            int index = fieldsListTableModel.getRealColumn(i);

            if (index != 0) {
                colModel.getColumn(i).setPreferredWidth(columnWidths[index - 1]);
                colModel.getColumn(i).setCellRenderer(columnRenderers[index]);
            }
        }
    }

    private void addMenuItemListener(final JCheckBoxMenuItem menuItem) {
        final boolean[] internalChange = new boolean[1];
        menuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (internalChange[0]) return;
                final int column = Integer.parseInt(e.getActionCommand());
                if (column == 5 && !fieldsListTableModel.isRealColumnVisible(column)) {
                    BrowserUtils.performTask(new Runnable() {
                        public void run() {
                            final int retainedSizesState = referencesBrowserController.getReferencesControllerHandler().
                                    getHeapFragmentWalker().computeRetainedSizes(false, true);
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    if (retainedSizesState != HeapFragmentWalker.RETAINED_SIZES_COMPUTED) {
                                        internalChange[0] = true;
                                        menuItem.setSelected(!menuItem.isSelected());
                                        internalChange[0] = false;
                                    } else {
                                        fieldsListTableModel.setRealColumnVisibility(column,
                                                !fieldsListTableModel.isRealColumnVisible(column));
                                        fieldsListTable.createDefaultColumnsFromModel();
                                        fieldsListTable.updateTreeTableHeader();
                                        setColumnsData();
                                    }
                                }
                            });
                        }
                    });
                } else {
                    fieldsListTableModel.setRealColumnVisibility(column,
                            !fieldsListTableModel.isRealColumnVisible(column));
                    fieldsListTable.createDefaultColumnsFromModel();
                    fieldsListTable.updateTreeTableHeader();
                    setColumnsData();
                }
            }
        });
    }

    private JButton createHeaderPopupCornerButton(final JPopupMenu headerPopup) {
        final JButton cornerButton = new JButton(Icons.getIcon(GeneralIcons.HIDE_COLUMN));
        cornerButton.setToolTipText(Bundle.ReferencesBrowserControllerUI_ShowHideColumnsString());
        cornerButton.setDefaultCapable(false);

        if (UIUtils.isWindowsClassicLookAndFeel()) {
            cornerButton.setMargin(new Insets(0, 0, 2, 2));
        } else if (UIUtils.isWindowsXPLookAndFeel()) {
            cornerButton.setMargin(new Insets(0, 0, 0, 1));
        } else if (UIUtils.isMetalLookAndFeel()) {
            cornerButton.setMargin(new Insets(0, 0, 2, 1));
        }

        cornerButton.addKeyListener(new KeyAdapter() {
                public void keyPressed(final KeyEvent evt) {
                    if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
                        showColumnSelectionPopup(headerPopup, cornerButton);
                    }
                }
            });

        cornerButton.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent mouseEvent) {
                    if (headerPopup.isVisible()) {
                        internalCornerButtonClick = true;
                        cornerButton.getModel().setArmed(false);
                    } else {
                        internalCornerButtonClick = false;

                        if (mouseEvent.getModifiers() == InputEvent.BUTTON3_MASK) {
                            showColumnSelectionPopup(headerPopup, cornerButton);
                        }
                    }
                }

                public void mouseClicked(MouseEvent mouseEvent) {
                    if ((mouseEvent.getModifiers() == InputEvent.BUTTON1_MASK) && (!internalCornerButtonClick)) {
                        showColumnSelectionPopup(headerPopup, cornerButton);
                    }
                }
            });

        return cornerButton;
    }

    private JPopupMenu createTablePopup() {
        JPopupMenu popup = new JPopupMenu();

        showInstanceItem = new JMenuItem(Bundle.ReferencesBrowserControllerUI_ShowInstanceItemText());
        showInstanceItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    performDefaultAction();
                }
            });
        showInstanceItem.setFont(popup.getFont().deriveFont(Font.BOLD));

//        showClassItem = new JMenuItem(SHOW_IN_CLASSES_ITEM_TEXT);
//        showClassItem.addActionListener(new ActionListener() {
//                public void actionPerformed(ActionEvent e) {
//                    int row = fieldsListTable.getSelectedRow();
//
//                    if (row != -1) {
//                        HeapWalkerNode node = (HeapWalkerNode) fieldsListTable.getTree().getPathForRow(row).getLastPathComponent();
//
//                        if (node instanceof HeapWalkerInstanceNode) {
//                            HeapWalkerInstanceNode instanceNode = (HeapWalkerInstanceNode) node;
//
//                            if (instanceNode instanceof ObjectFieldNode && ((ObjectFieldNode) instanceNode).isStatic()) {
//                                referencesBrowserController.navigateToClass(((ObjectFieldNode) instanceNode).getFieldValue()
//                                                                             .getField().getDeclaringClass());
//                            } else if (instanceNode.hasInstance()) {
//                                referencesBrowserController.navigateToClass(instanceNode.getInstance().getJavaClass());
//                            }
//                        }
//                    }
//                }
//            });

        showGcRootItem = new JMenuItem(Bundle.ReferencesBrowserControllerUI_ShowGcRootItemText());
        showGcRootItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    HeapWalkerNode node = getSelectedNode();

                    if (node instanceof InstanceNode) {
                        InstanceNode instanceNode = (InstanceNode) node;

                        if (instanceNode.hasInstance()) {
                            referencesBrowserController.navigateToNearestGCRoot(instanceNode);
                        }
                    }
                }
            });

        showLoopOriginItem = new JMenuItem(Bundle.ReferencesBrowserControllerUI_ShowLoopItemText());
        showLoopOriginItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                    HeapWalkerNode node = getSelectedNode();

                    if (node instanceof HeapWalkerInstanceNode && ((HeapWalkerInstanceNode) node).isLoop()) {
                        selectNode(((HeapWalkerInstanceNode) node).getLoopTo());
                    }
                }
            });
        
        // Copy Path From Root
        copyPathFromRootItem = new JMenuItem(Bundle.ReferencesBrowserControllerUI_CopyPathFromRoot());
        copyPathFromRootItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int row = fieldsListTable.getSelectedRow();
                if (row != -1) {
                    TreePath path = fieldsListTable.getTree().getPathForRow(row);
                    BrowserUtils.copyPathFromRoot(path);
                }
            };
        });

        if (GoToSource.isAvailable()) {
            showSourceItem = new JMenuItem(Bundle.ReferencesBrowserControllerUI_GoToSourceItemText());
            showSourceItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                            HeapWalkerNode node = getSelectedNode();
                            
                            if (node != null) {
                                String className = BrowserUtils.getArrayBaseType(node.getType());
                                Lookup.Provider p = referencesBrowserController.getReferencesControllerHandler().getHeapFragmentWalker().getHeapDumpProject();
                                GoToSource.openSource(p, className, null, null);
                            }
                        }
                    });
        }
            
        showInThreadsItem = new JMenuItem(Bundle.ReferencesBrowserControllerUI_ShowInThreadsItemText());
        showInThreadsItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    HeapWalkerNode node = getSelectedNode();
                    if (node instanceof HeapWalkerInstanceNode) {
                        Instance instance = ((HeapWalkerInstanceNode)node).getInstance();
                        referencesBrowserController.showInThreads(instance);
                    }
                }
            });
            
        popup.add(showInstanceItem);
//        popup.add(showClassItem);
        popup.add(showGcRootItem);
        popup.add(showInThreadsItem);
        popup.addSeparator();
        popup.add(copyPathFromRootItem);
        popup.addSeparator();
        popup.add(showLoopOriginItem);
        if (showSourceItem != null) popup.add(showSourceItem);

        return popup;
    }

    private void initColumnsData() {
        columnWidths = new int[columnCount - 1]; // Width of the first column fits to width
        columnNames = new String[columnCount];
        columnToolTips = new String[columnCount];
        columnRenderers = new TableCellRenderer[columnCount];

        columnNames[0] = Bundle.ReferencesBrowserControllerUI_FieldColumnName();
        columnToolTips[0] = Bundle.ReferencesBrowserControllerUI_FieldColumnDescr();

        columnNames[1] = Bundle.ReferencesBrowserControllerUI_TypeColumnName();
        columnToolTips[1] = Bundle.ReferencesBrowserControllerUI_TypeColumnDescr();

        columnNames[2] = Bundle.ReferencesBrowserControllerUI_FullTypeColumnName();
        columnToolTips[2] = Bundle.ReferencesBrowserControllerUI_FullTypeColumnDescr();

        columnNames[3] = Bundle.ReferencesBrowserControllerUI_ValueColumnName();
        columnToolTips[3] = Bundle.ReferencesBrowserControllerUI_ValueColumnDescr();

        columnNames[4] = Bundle.ReferencesBrowserControllerUI_SizeColumnName();
        columnToolTips[4] = Bundle.ReferencesBrowserControllerUI_SizeColumnDescr();

        if (retainedSizeSupported) {
            columnNames[5] = Bundle.ReferencesBrowserControllerUI_RetainedSizeColumnName();
            columnToolTips[5] = Bundle.ReferencesBrowserControllerUI_RetainedSizeColumnDescr();
        }

        int unitWidth = getFontMetrics(getFont()).charWidth('W'); // NOI18N // initial width of data columns

//        FieldTreeCellRenderer treeCellRenderer = new FieldTreeCellRenderer();
//        treeCellRenderer.setLeafIcon(null);
//        treeCellRenderer.setClosedIcon(null);
//        treeCellRenderer.setOpenIcon(null);

        LabelTableCellRenderer dataCellRendererL = new LabelTableCellRenderer(JLabel.LEADING);
        LabelTableCellRenderer dataCellRendererT = new LabelTableCellRenderer(JLabel.TRAILING);

        // method / class / package name
        columnRenderers[0] = null;

        columnWidths[1 - 1] = unitWidth * 18;
        columnRenderers[1] = dataCellRendererL;

        columnWidths[2 - 1] = unitWidth * 28;
        columnRenderers[2] = dataCellRendererL;

        columnWidths[3 - 1] = unitWidth * 20;
        columnRenderers[3] = new FieldTableCellRenderer();

        columnWidths[4 - 1] = unitWidth * 7;
        columnRenderers[4] = dataCellRendererT;

        if (retainedSizeSupported) {
            columnWidths[5 - 1] = unitWidth * 7;
            columnRenderers[5] = dataCellRendererT;
        }
    }

    private void initComponents() {
        treeCellRenderer.setLeafIcon(null);
        treeCellRenderer.setClosedIcon(null);
        treeCellRenderer.setOpenIcon(null);

        fieldsListTableModel.setRealColumnVisibility(2, false);
        fieldsListTableModel.setRealColumnVisibility(4, false);

        if (retainedSizeSupported)
            fieldsListTableModel.setRealColumnVisibility(5, referencesBrowserController.
                getReferencesControllerHandler().getHeapFragmentWalker().getRetainedSizesStatus()
                                          == HeapFragmentWalker.RETAINED_SIZES_COMPUTED);

        fieldsListTable = new JTreeTable(fieldsListTableModel) {
                public void doLayout() {
                    int columnsWidthsSum = 0;
                    int realFirstColumn = -1;

                    TableColumnModel colModel = getColumnModel();

                    for (int i = 0; i < fieldsListTableModel.getColumnCount(); i++) {
                        if (fieldsListTableModel.getRealColumn(i) == 0) {
                            realFirstColumn = i;
                        } else {
                            columnsWidthsSum += colModel.getColumn(i).getPreferredWidth();
                        }
                    }

                    if (realFirstColumn != -1) {
                        colModel.getColumn(realFirstColumn).setPreferredWidth(getWidth() - columnsWidthsSum);
                    }

                    super.doLayout();
                }
            };
        fieldsListTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fieldsListTable.setGridColor(UIConstants.TABLE_VERTICAL_GRID_COLOR);
        fieldsListTable.setSelectionBackground(UIConstants.TABLE_SELECTION_BACKGROUND_COLOR);
        fieldsListTable.setSelectionForeground(UIConstants.TABLE_SELECTION_FOREGROUND_COLOR);
        fieldsListTable.setShowHorizontalLines(UIConstants.SHOW_TABLE_HORIZONTAL_GRID);
        fieldsListTable.setShowVerticalLines(UIConstants.SHOW_TABLE_VERTICAL_GRID);
        fieldsListTable.setRowMargin(UIConstants.TABLE_ROW_MARGIN);
        fieldsListTable.setRowHeight(UIUtils.getDefaultRowHeight() + 2);
        fieldsListTable.getTree().setLargeModel(true);
        fieldsListTable.getTree().setToggleClickCount(0);
        fieldsListTable.getColumnModel().getColumn(0).setMinWidth(150);
        fieldsListTable.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                       .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "DEFAULT_ACTION"); // NOI18N
        fieldsListTable.getActionMap().put("DEFAULT_ACTION", // NOI18N
                                           new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    performDefaultAction();
                }
            });

        // Disable traversing table cells using TAB and Shift+TAB
        Set keys = new HashSet(fieldsListTable.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
        keys.add(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0));
        fieldsListTable.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, keys);

        keys = new HashSet(fieldsListTable.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
        keys.add(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_MASK));
        fieldsListTable.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, keys);

        setColumnsData();

        tablePopup = createTablePopup();

        cornerPopup = new JPopupMenu();

        JTreeTablePanel tablePanel = new JTreeTablePanel(fieldsListTable);
        tablePanel.setCorner(JScrollPane.UPPER_RIGHT_CORNER, createHeaderPopupCornerButton(cornerPopup));

        dataPanel = new JPanel(new BorderLayout());
        dataPanel.add(tablePanel, BorderLayout.CENTER);

        noDataPanel = new JPanel(new BorderLayout());
        noDataPanel.setBorder(BorderFactory.createLoweredBevelBorder());

        HTMLTextArea hintArea = new HTMLTextArea();
        hintArea.setBorder(BorderFactory.createEmptyBorder(10, 8, 8, 8));

        String instancesRes = Icons.getResource(HeapWalkerIcons.INSTANCES);
        String hintText = Bundle.ReferencesBrowserControllerUI_NoInstanceSelectedMsg(
                                "<img border='0' align='bottom' src='nbresloc:/" + instancesRes + "'>"); // NOI18N

        hintArea.setText(hintText);
        noDataPanel.add(hintArea, BorderLayout.CENTER);

        contents = new CardLayout();
        setLayout(contents);
        add(noDataPanel, NO_DATA);
        add(dataPanel, DATA);

        fieldsListTable.addMouseListener(new FieldsListTableMouseListener());
        fieldsListTable.addKeyListener(new FieldsListTableKeyListener());
    }

    // --- Private implementation ------------------------------------------------
    private void initData() {
        fieldsListTableModel.setRoot(referencesBrowserController.getFilteredSortedReferences(filterValue, sortingColumn, sortingOrder));
        refreshView();
    }

    private void navigateToInstance(Instance instance) {
        referencesBrowserController.navigateToInstance(instance);
    }

    private void performDefaultAction() {
        HeapWalkerNode node = getSelectedNode();

        if (node instanceof HeapWalkerInstanceNode) {
            referencesBrowserController.createNavigationHistoryPoint();

            HeapWalkerInstanceNode instanceNode = (HeapWalkerInstanceNode) node;
            referencesBrowserController.navigateToInstance(instanceNode.getInstance());
        }
    }

    private void saveColumnsData() {
        TableColumnModel colModel = fieldsListTable.getColumnModel();

        for (int i = 0; i < fieldsListTableModel.getColumnCount(); i++) {
            int index = fieldsListTableModel.getRealColumn(i);

            if (index != 0) {
                columnWidths[index - 1] = colModel.getColumn(i).getPreferredWidth();
            }
        }
    }

    private void showColumnSelectionPopup(final JPopupMenu headerPopup, final JButton cornerButton) {
        initColumnSelectorItems();
        headerPopup.show(cornerButton, cornerButton.getWidth() - headerPopup.getPreferredSize().width, cornerButton.getHeight());
    }

    public HeapWalkerNode getSelectedNode() {
        int row = fieldsListTable.getSelectedRow();
        return getSelectedNode(row);        
    }
    
    public HeapWalkerNode getSelectedNode(int row) {
        return row == -1 ? null :
               (HeapWalkerNode) fieldsListTable.getTree().
               getPathForRow(row).getLastPathComponent();
    }
    
    public void showRootGCRoot() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                final HeapWalkerNode node = getSelectedNode(0);
                if (node != null && isComputed(node) /*&& (node instanceof InstanceNode)*/) {
                    referencesBrowserController.navigateToNearestGCRoot((InstanceNode)node);
                } else {
                    BrowserUtils.performTask(new Runnable() {
                        public void run() {
                            showRootGCRoot();
                        }
                    }, 100);
                }
            }
        });
    }
    
    private boolean isComputed(HeapWalkerNode node) {
        return node instanceof HeapWalkerInstanceNode && (!node.currentlyHasChildren() ||
               node.getNChildren() != 1 || !HeapWalkerNodeFactory.isMessageNode(node.getChild(0)));
    }

    private void showPopupMenu(int row, int x, int y) {
        if (row == -1) return;
        
        HeapWalkerNode node = getSelectedNode(row);

        if (node instanceof HeapWalkerInstanceNode && ((HeapWalkerInstanceNode) node).isLoop()) {
            showLoopOriginItem.setVisible(true);
        } else {
            showLoopOriginItem.setVisible(false);
        }

        if (node != null && node.isRoot()) {
            showInstanceItem.setEnabled(false);
            copyPathFromRootItem.setEnabled(false);
        } else {
            showInstanceItem.setEnabled(node instanceof HeapWalkerInstanceNode
                                        && !(node instanceof HeapWalkerFieldNode && ((HeapWalkerFieldNode) node).isStatic()));
            copyPathFromRootItem.setEnabled(node != null);
        }

//        showClassItem.setEnabled(node instanceof HeapWalkerInstanceNode || node instanceof ClassNode);
        showGcRootItem.setEnabled(isComputed(node)); // #124306
        if (showSourceItem != null) {
            String className = null;
            if (node instanceof HeapWalkerInstanceNode) {
                HeapWalkerInstanceNode instanceNode = (HeapWalkerInstanceNode)node;
                if(instanceNode.hasInstance()) {
                    className = instanceNode.getInstance().getJavaClass().getName();
                }
            }
            showSourceItem.setEnabled(className != null && !VMUtils.isPrimitiveType(BrowserUtils.getArrayBaseType(className)));
        }
        showInThreadsItem.setEnabled(false);
        if (node instanceof HeapWalkerInstanceNode) {
            Instance rootInstance = ((HeapWalkerInstanceNode)node).getInstance();
            Heap heap = referencesBrowserController.getReferencesControllerHandler().getHeapFragmentWalker().getHeapFragment();
            GCRoot gcRoot = heap.getGCRoot(rootInstance);
            
            if (gcRoot != null && GCRoot.JAVA_FRAME.equals(gcRoot.getKind())) {
                // make sure that thread information is available
                JavaFrameGCRoot frameVar = (JavaFrameGCRoot) gcRoot;
                
                if (frameVar.getFrameNumber() != -1) {
                    showInThreadsItem.setEnabled(true);
                }
            }
        }
        
        if ((x == -1) || (y == -1)) {
            Rectangle rowBounds = fieldsListTable.getCellRect(row, 0, true);

            if (x == -1) {
                x = rowBounds.x + (rowBounds.width / 2);
            }

            if (y == -1) {
                y = rowBounds.y + (rowBounds.height / 2);
            }
        }

        tablePopup.show(fieldsListTable, x, y);
    }
}
