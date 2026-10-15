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
package org.openide.explorer.view;

import java.util.logging.Logger;
import org.openide.awt.MouseUtils;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.nodes.NodeListener;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerManager.Provider;
import org.openide.explorer.ExplorerUtils;

import java.awt.*;
import java.awt.event.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;

import javax.accessibility.AccessibleContext;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import javax.swing.plaf.metal.MetalScrollBarUI;
import javax.swing.table.*;
import javax.swing.tree.*;
import org.openide.awt.QuickSearch;
import org.openide.explorer.view.TreeView.PopupAdapter;
import org.openide.explorer.view.TreeView.PopupSupport;
import org.openide.explorer.view.TreeView.TreePropertyListener;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;
import org.openide.util.Utilities;


/** Explorer view. Allows to view tree of nodes on the left
 * and its properties in table on the right.
 * <p>
 * The main mechanism for setting what properties are displayed is
 * {@link #setProperties(org.openide.nodes.Node.Property[])}.
 * Pass this method an
 * array of properties.  These will act as a template, and properties of
 * the displayed nodes which share the same <i>name</i> will be used in
 * the columns of the table.
 *
 * You can customize behaviour
 * of property columns using <code>Property.setValue (String parameter,
 * Object value)</code>.  For example,
 * assume you have following array of properties:
 * <br><code>org.openide.nodes.Node.Property[] properties</code><br>
 *
 * if you need second column to be initially invisible in TreeTableView, you
 * should set its custom parameter:
 * <br><code>properties[1].setValue ("InvisibleInTreeTableView", Boolean.TRUE);</code>
 *
 * <TABLE>
 * <caption>custom parameter list</caption>
 *     <TR>
 *         <TH> Parameter name
 *         </TH>
 *         <TH> Parameter type
 *         </TH>
 *         <TH> Description
 *         </TH>
 *     </TR>
 *     <TR>
 *         <TD> InvisibleInTreeTableView</TD>
 *         <TD> Boolean </TD>
 *         <TD> This property column should be initially invisible (hidden).</TD>
 *     </TR>
 *     <TR>
 *         <TD> ComparableColumnTTV</TD>
 *         <TD> Boolean </TD>
 *         <TD> This property column should be  used for sorting.</TD>
 *     </TR>
 *     <TR>
 *         <TD> SortingColumnTTV</TD>
 *         <TD> Boolean </TD>
 *         <TD> TreeTableView should be initially sorted by this property column.</TD>
 *     </TR>
 *     <TR>
 *         <TD> DescendingOrderTTV</TD>
 *         <TD> Boolean </TD>
 *         <TD> If this parameter and <code>SortingColumnTTV</code> is set, TreeTableView should
 *              be initially sorted by this property columns in descending order.
 *         </TD>
 *     </TR>
 *     <TR>
 *         <TD> OrderNumberTTV</TD>
 *         <TD> Integer </TD>
 *         <TD> If this parameter is set to <code>N</code>, this property column will be
 *             displayed as Nth column of table. If not set, column will be
 *             displayed in natural order.
 *         </TD>
 *     </TR>
 *     <TR>
 *         <TD> TreeColumnTTV</TD>
 *         <TD> Boolean </TD>
 *         <TD> Identifies special property representing first (tree) column. To allow setting
 *             of <code>SortingColumnTTV, DescendingOrderTTV, ComparableColumnTTV</code> parameters
 *             also for first (tree) column, use this special parameter and add
 *             this property to Node.Property[] array before calling
 *             TreeTableView.setProperties (Node.Property[]).
 *         </TD>
 *     </TR>
 *    <TR>
 *        <TD> ColumnMnemonicCharTTV</TD>
 *        <TD> String </TD>
 *        <TD> When set, this parameter contains the mnemonic character for column's
 *            display name (e.g. in <I>Change Visible Columns</I> dialog window).
 *            If not set, no mnemonic will be displayed.
 *        </TD>
 *    </TR>
 *    <TR>
 *        <TD> ColumnDisplayNameWithMnemonicTTV</TD>
 *        <TD> String </TD>
 *        <TD> When set, this parameter contains column's display name with
 *              '&amp;' as the mnemonic. This parameter should be preferred over
 *              ColumnMnemonicCharTTV.
 *        </TD>
 *    </TR>
 * </TABLE>
 *
 * <p>
 * This class is a <em>view</em>
 * to use it properly you need to add it into a component which implements
 * {@link Provider}. Good examples of that can be found 
 * in {@link ExplorerUtils}. Then just use 
 * {@link Provider#getExplorerManager} call to get the {@link ExplorerManager}
 * and control its state.
 * </p>
 * <p>
 * There can be multiple <em>views</em> under one container implementing {@link Provider}. Select from
 * range of predefined ones or write your own:
 * </p>
 * <ul>
 *      <li>{@link org.openide.explorer.view.BeanTreeView} - shows a tree of nodes</li>
 *      <li>{@link org.openide.explorer.view.ContextTreeView} - shows a tree of nodes without leaf nodes</li>
 *      <li>{@link org.openide.explorer.view.ListView} - shows a list of nodes</li>
 *      <li>{@link org.openide.explorer.view.IconView} - shows a rows of nodes with bigger icons</li>
 *      <li>{@link org.openide.explorer.view.ChoiceView} - creates a combo box based on the explored nodes</li>
 *      <li>{@link org.openide.explorer.view.TreeTableView} - shows tree of nodes together with a set of their {@link Property}</li>
 *      <li>{@link org.openide.explorer.view.MenuView} - can create a {@link JMenu} structure based on structure of {@link Node}s</li>
 * </ul>
 * <p>
 * All of these views use {@link ExplorerManager#find} to walk up the AWT hierarchy and locate the
 * {@link ExplorerManager} to use as a controler. They attach as listeners to
 * it and also call its setter methods to update the shared state based on the
 * user action. Not all views make sence together, but for example
 * {@link org.openide.explorer.view.ContextTreeView} and {@link org.openide.explorer.view.ListView} were designed to complement
 * themselves and behaves like windows explorer. The {@link org.openide.explorer.propertysheet.PropertySheetView}
 * for example should be able to work with any other view.
 * </p>
 *
 * @author  jrojcek
 * @since 1.7
 */
public class TreeTableView extends BeanTreeView {
    // icon of column button
    private static final String COLUMNS_ICON = "org/netbeans/modules/openide/explorer/columns.gif"; // NOI18N

    // icons of ascending/descending order in column header
    private static final String SORT_ASC_ICON = "org/netbeans/modules/openide/explorer/columnsSortedAsc.gif"; // NOI18N
    private static final String SORT_DESC_ICON = "org/netbeans/modules/openide/explorer/columnsSortedDesc.gif"; // NOI18N

    /** The table */
    protected JTable treeTable;
    private NodeTableModel tableModel;

    // Tree scroll support
    private JScrollBar hScrollBar;
    private JScrollPane scrollPane;
    private ScrollListener listener;

    // hiding columns allowed
    private boolean allowHideColumns = false;

    // sorting by column allowed
    private boolean allowSortingByColumn = false;

    // hide horizontal scrollbar
    private boolean hideHScrollBar = false;

    // button in corner of scroll pane
    private JButton colsButton = null;

    // tree model with sorting support
    private SortedNodeTreeModel sortedNodeTreeModel;

    /** Listener on keystroke to invoke default action */
    private ActionListener defaultTreeActionListener;

    // default treetable header renderer
    private TableCellRenderer defaultHeaderRenderer = null;
    private MouseUtils.PopupMouseAdapter tableMouseListener;

    /** Accessible context of this class (implemented by inner class AccessibleTreeTableView). */
    private AccessibleContext accessContext;
    private TreeColumnProperty treeColumnProperty = new TreeColumnProperty();
    private int treeColumnWidth;
    private Component treeTableParent = null;
    private QuickSearch quickSearch;
    private Component searchpanel;

    /** Create TreeTableView with default NodeTableModel
     */
    public TreeTableView() {
        this(new NodeTableModel());
    }

    /** Creates TreeTableView with provided NodeTableModel.
     * @param ntm node table model
     */
    public TreeTableView(NodeTableModel ntm) {
        setLayout(new SearchScrollPaneLayout());
        tableModel = ntm;

        initializeTreeTable();
        setPopupAllowed(true);
        setDefaultActionAllowed(true);

        initializeTreeScrollSupport();

        // add scrollbar and scrollpane into a panel
        JPanel p = new CompoundScrollPane();
        p.setLayout(new BorderLayout());
        scrollPane.setViewportView(treeTable);
        p.add(BorderLayout.CENTER, scrollPane);

        Icon icon = ImageUtilities.image2Icon(ImageUtilities.loadImage(COLUMNS_ICON)); // NOI18N
        colsButton = new javax.swing.JButton(icon);
        // For HiDPI support.
        colsButton.setDisabledIcon(ImageUtilities.createDisabledIcon(icon));
        colsButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(TreeTableView.class, "ACN_ColumnsSelector")); //NOI18N
        colsButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(TreeTableView.class, "ACD_ColumnsSelector")); //NOI18N
        colsButton.addActionListener(
            new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    selectVisibleColumns();
                }
            }
        );

        JPanel sbp = new JPanel();
        sbp.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        sbp.add(hScrollBar);
        p.add(BorderLayout.SOUTH, sbp);

        super.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        super.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        setViewportView(p);
        setBorder(BorderFactory.createEmptyBorder()); //NOI18N
        setViewportBorder(BorderFactory.createEmptyBorder()); //NOI18N
    }

    @Override
    public void setRowHeader(JViewport rowHeader) {
        rowHeader.setBorder(BorderFactory.createEmptyBorder());
        super.setRowHeader(rowHeader);
    }

    /* Overriden to allow hide special horizontal scrollbar
     */
    @Override
    public void setHorizontalScrollBarPolicy(int policy) {
        hideHScrollBar = (policy == JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        if (hideHScrollBar) {
            hScrollBar.setVisible(false);
            ((TreeTable) treeTable).setTreeHScrollingEnabled(false);
        }
    }

    /* Overriden to delegate policy of vertical scrollbar to inner scrollPane
     */
    @Override
    public void setVerticalScrollBarPolicy(int policy) {
        if (scrollPane == null) {
            return;
        }

        allowHideColumns = (policy == JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        if (allowHideColumns) {
            scrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, colsButton);
        }

        treeTable.getTableHeader().setReorderingAllowed(allowHideColumns);

        scrollPane.setVerticalScrollBarPolicy(policy);
    }

    @Override
    protected NodeTreeModel createModel() {
        return getSortedNodeTreeModel();
    }

    /** Requests focus for the tree component. Overrides superclass method. */
    @Override
    public void requestFocus() {
        if (treeTable != null) {
            treeTable.requestFocus();
        }
    }

    @Override
    public boolean requestFocusInWindow() {
        boolean res = super.requestFocusInWindow();

        //#44856: pass the focus request to the treetable as well 
        if (null != treeTable) {
            treeTable.requestFocus();
        }

        return res;
    }

    /* Sets sorting ability
     */
    private void setAllowSortingByColumn(boolean allow) {
        if (allow && (allow != allowSortingByColumn)) {
            addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent evt) {
                        // Check whether it was really a click
                        if (evt.getClickCount() == 0) return ;
                        Component c = evt.getComponent();

                        if (c instanceof JTableHeader) {
                            JTableHeader h = (JTableHeader) c;
                            int index = h.columnAtPoint(evt.getPoint());

                            //issue 38442, column can be -1 if this is the
                            //upper right corner - there's no column there,
                            //so make sure it's an index >=0.
                            if (index >= 0) {
                                clickOnColumnAction(index - 1);
                            }
                        }
                    }
                }
            );
        }

        allowSortingByColumn = allow;
    }

    /* Change sorting after clicking on comparable column header.
     * Cycle through ascending -> descending -> no sort -> (start over)
     */
    private void clickOnColumnAction(int index) {
        if (index == -1) {
            if (treeColumnProperty.isComparable()) {
                if (treeColumnProperty.isSortingColumn()) {
                    if (!treeColumnProperty.isSortOrderDescending()) {
                        setSortingOrder(false);
                    } else {
                        noSorting();
                    }
                } else {
                    int realIndex = tableModel.translateVisibleColumnIndex(index);
                    setSortingColumn(index);
                    setSortingOrder(true);
                }
            }
        } else if (tableModel.isComparableColumn(index)) {
            if (tableModel.isSortingColumnEx(tableModel.translateVisibleColumnIndex(index))) {
                if (!tableModel.isSortOrderDescending()) {
                    setSortingOrder(false);
                } else {
                    noSorting();
                }
            } else {
                int realIndex = tableModel.translateVisibleColumnIndex(index);
                setSortingColumn(realIndex);
                setSortingOrder(true);
            }
        }
    }

    private void selectVisibleColumns() {
        setCurrentWidths();

        String viewName = null;

        if (getParent() != null) {
            viewName = getParent().getName();
        }

        if (
            tableModel.selectVisibleColumns(
                    viewName, treeTable.getColumnName(0), getSortedNodeTreeModel().getRootDescription()
                )
        ) {
            if (tableModel.getSortingColumn() == -1) {
                getSortedNodeTreeModel().setSortedByProperty(null);
            }

            setTreePreferredWidth(treeColumnWidth);

            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                setTableColumnPreferredWidth(tableModel.getArrayIndex(i), tableModel.getVisibleColumnWidth(i));
            }
        }
    }

    private void setCurrentWidths() {
        treeColumnWidth = treeTable.getColumnModel().getColumn(0).getWidth();

        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            int w = treeTable.getColumnModel().getColumn(i + 1).getWidth();
            tableModel.setVisibleColumnWidth(i, w);
        }
    }

    /** Do not initialize tree now. We will do it from our constructor.
     * [dafe] Used probably because this method is called *before* superclass
     * is fully created (constructor finished) which is horrible but I don't
     * have enough knowledge about this code to change it.
     */
    @Override
    void initializeTree() {
    }
    
    private final Object searchConstraints = new Object();

    @Override
    public void add(Component comp, Object constraints) {
        if (constraints == searchConstraints) {
            searchpanel = comp;
            constraints = null;
        }
        super.add(comp, constraints);
    }

    @Override
    public void remove(Component comp) {
        if (comp == searchpanel) {
            searchpanel = null;
        }
        super.remove(comp);
    }
    
    /** Initialize tree and treeTable.
     */
    private void initializeTreeTable() {
        treeModel = createModel();
        TreeTable tt = new TreeTable(treeModel, tableModel);
        treeTable = tt;
        tree = ((TreeTable) treeTable).getTree();
        TableQuickSearchSupport tqss = new TableQuickSearchSupport(tt, tt.getQuickSearchTableFilter(), tt.getQuickSearchSettings());
        quickSearch = QuickSearch.attach(this, searchConstraints, tqss, tqss.createSearchPopupMenu());
        tt.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                quickSearch.processKeyEvent(e);
            }
            @Override
            public void keyPressed(KeyEvent e) {
                quickSearch.processKeyEvent(e);
            }
            @Override
            public void keyReleased(KeyEvent e) {
                quickSearch.processKeyEvent(e);
            }
        });
        
        defaultHeaderRenderer = treeTable.getTableHeader().getDefaultRenderer();
        treeTable.getTableHeader().setDefaultRenderer(new SortingHeaderRenderer());

        // init listener & attach it to closing of
        managerListener = new TreePropertyListener();
        tree.addTreeExpansionListener(managerListener);

        defaultActionListener = new PopupSupport();
        Action popupWrapper = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    SwingUtilities.invokeLater( defaultActionListener );
                }

                @Override
                public boolean isEnabled() {
                    return treeTable.isFocusOwner() || tree.isFocusOwner();
                }
            };
            
        treeTable.getInputMap( JTree.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT ).put( 
                KeyStroke.getKeyStroke( KeyEvent.VK_F10, KeyEvent.SHIFT_DOWN_MASK ), "org.openide.actions.PopupAction" );
        treeTable.getActionMap().put("org.openide.actions.PopupAction", popupWrapper);
        tree.addMouseListener(defaultActionListener);

        tableMouseListener = new MouseUtils.PopupMouseAdapter() {
                    @Override
                    public void showPopup(MouseEvent mevt) {
                        if (isPopupAllowed()) {
                            if (mevt.getY() > treeTable.getHeight()) {
                                // clear selection, if click under the table
                                treeTable.clearSelection();
                            } else {
                                int selRow = treeTable.rowAtPoint( mevt.getPoint() );
                                boolean isAlreadySelected = false;
                                int[] currentSelection = tree.getSelectionRows();
                                for( int i=0; null != currentSelection && i<currentSelection.length; i++ ) {
                                    if( selRow == currentSelection[i] ) {
                                        isAlreadySelected = true;
                                        break;
                                    }
                                }
                                if( !isAlreadySelected )
                                    tree.setSelectionRow( selRow );
                            }

                            createPopup(mevt);
                        }
                    }
                };
        treeTable.addMouseListener(tableMouseListener);

        if (UIManager.getColor("control") != null) { // NOI18N
            treeTable.setGridColor(UIManager.getColor("control")); // NOI18N
        }
    }

    @Override
    public void setSelectionMode(int mode) {
        super.setSelectionMode(mode);

        if (mode == TreeSelectionModel.SINGLE_TREE_SELECTION) {
            treeTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        } else if (mode == TreeSelectionModel.CONTIGUOUS_TREE_SELECTION) {
            treeTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        } else if (mode == TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION) {
            treeTable.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        }
    }

    /** Overrides JScrollPane's getAccessibleContext() method to use internal accessible context.
     */
    @Override
    public AccessibleContext getAccessibleContext() {
        if (accessContext == null) {
            accessContext = new AccessibleTreeTableView();
        }

        return accessContext;
    }

    /** Initialize full support for horizontal scrolling.
     */
    private void initializeTreeScrollSupport() {
        scrollPane = new JScrollPane();
        scrollPane.setName("TreeTableView.scrollpane"); //NOI18N
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());

        if (UIManager.getColor("Table.background") != null) { // NOI18N
            scrollPane.getViewport().setBackground(UIManager.getColor("Table.background")); // NOI18N
        }

        hScrollBar = new JScrollBar(JScrollBar.HORIZONTAL);
        hScrollBar.putClientProperty(MetalScrollBarUI.FREE_STANDING_PROP, Boolean.FALSE);
        hScrollBar.setVisible(false);

        listener = new ScrollListener();

        treeTable.addPropertyChangeListener(listener);
        scrollPane.getViewport().addComponentListener(listener);
        tree.addPropertyChangeListener(listener);
        hScrollBar.getModel().addChangeListener(listener);
    }

    /* Overriden to work well with treeTable.
     */
    @Override
    public void setPopupAllowed(boolean value) {
        if (tree == null) {
            return;
        }

        if ((popupListener == null) && value) {
            // on
            popupListener = new PopupAdapter() {
                @Override
                        protected void showPopup(MouseEvent e) {
                            int selRow = tree.getClosestRowForLocation(e.getX(), e.getY());

                            if (!tree.isRowSelected(selRow)) {
                                tree.setSelectionRow(selRow);
                            }
                        }
                    };

            tree.addMouseListener(popupListener);

            return;
        }

        if ((popupListener != null) && !value) {
            // off
            tree.removeMouseListener(popupListener);
            popupListener = null;

            return;
        }
    }

    /* Overriden to work well with treeTable.
     */
    @Override
    public void setDefaultActionAllowed(boolean value) {
        if (tree == null) {
            return;
        }

        defaultActionEnabled = value;

        if (value) {
            defaultTreeActionListener = new DefaultTreeAction();
            treeTable.registerKeyboardAction(
                defaultTreeActionListener, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), JComponent.WHEN_FOCUSED
            );
        } else {
            // Switch off.
            defaultTreeActionListener = null;
            treeTable.unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false));
        }
    }

    @Override
    public boolean isQuickSearchAllowed() {
        return quickSearch.isEnabled();
    }
    
    @Override
    public void setQuickSearchAllowed(boolean allowedQuickSearch) {
        quickSearch.setEnabled(allowedQuickSearch);
    }

    /** Set columns.
     * @param props each column is constructed from Node.Property
     */
    public void setProperties(Property[] props) {
        tableModel.setProperties(props);
        treeColumnProperty.setProperty(tableModel.propertyForColumn(-1));

        if (treeColumnProperty.isComparable() || tableModel.existsComparableColumn()) {
            setAllowSortingByColumn(true);

            if (treeColumnProperty.isSortingColumn()) {
                getSortedNodeTreeModel().setSortedByName(true, !treeColumnProperty.isSortOrderDescending());
            } else {
                int index = tableModel.getSortingColumn();

                if (index != -1) {
                    getSortedNodeTreeModel().setSortedByProperty(
                        tableModel.propertyForColumnEx(index), !tableModel.isSortOrderDescending()
                    );
                }
            }
        }
    }

    /** Sets resize mode of table.
     *
     * @param mode - One of 5 legal values: <pre>JTable.AUTO_RESIZE_OFF,
     *                                           JTable.AUTO_RESIZE_NEXT_COLUMN,
     *                                           JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS,
     *                                           JTable.AUTO_RESIZE_LAST_COLUMN,
     *                                           JTable.AUTO_RESIZE_ALL_COLUMNS</pre>
     */
    public final void setTableAutoResizeMode(int mode) {
        treeTable.setAutoResizeMode(mode);
    }

    /** Gets resize mode of table.
     *
     * @return mode - One of 5 legal values: <pre>JTable.AUTO_RESIZE_OFF,
     *                                           JTable.AUTO_RESIZE_NEXT_COLUMN,
     *                                           JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS,
     *                                           JTable.AUTO_RESIZE_LAST_COLUMN,
     *                                           JTable.AUTO_RESIZE_ALL_COLUMNS</pre>
     */
    public final int getTableAutoResizeMode() {
        return treeTable.getAutoResizeMode();
    }

    /** Sets preferred width of table column
     * @param index column index
     * @param width preferred column width
     */
    public final void setTableColumnPreferredWidth(int index, int width) {
        if (index == -1) {
            //Issue 47969 - sometimes this is called with a -1 arg
            return;
        }

        tableModel.setArrayColumnWidth(index, width);

        int j = tableModel.getVisibleIndex(index);

        if (j != -1) {
            treeTable.getColumnModel().getColumn(j + 1).setPreferredWidth(width);
        }
    }

    /** Gets preferred width of table column
     * @param index column index
     * @return preferred column width
     */
    public final int getTableColumnPreferredWidth(int index) {
        int j = tableModel.getVisibleIndex(index);

        if (j != -1) {
            return treeTable.getColumnModel().getColumn(j + 1).getPreferredWidth();
        } else {
            return tableModel.getArrayColumnWidth(index);
        }
    }

    /** Set preferred size of tree view
     * @param width preferred width of tree view
     */
    public final void setTreePreferredWidth(int width) {
        treeTable.getColumnModel().getColumn(((TreeTable) treeTable).getTreeColumnIndex()).setPreferredWidth(width);
    }

    /** Get preferred size of tree view
     * @return preferred width of tree view
     */
    public final int getTreePreferredWidth() {
        return treeTable.getColumnModel().getColumn(((TreeTable) treeTable).getTreeColumnIndex()).getPreferredWidth();
    }

    @Override
    public void addNotify() {
        // to allow displaying popup also in blank area
        if (treeTable.getParent() != null) {
            treeTableParent = treeTable.getParent();
            treeTableParent.addMouseListener(tableMouseListener);
        }

        super.addNotify();
        if( tableModel.getRowCount() == 0 ) {
            //re-attach node listeners
            Node[] nodes = new Node[tree.getRowCount()];

            for (int i = 0; i < tree.getRowCount(); i++) {
                nodes[i] = Visualizer.findNode(tree.getPathForRow(i).getLastPathComponent());
            }

            tableModel.setNodes(nodes);
        }
        listener.revalidateScrollBar();
        ViewUtil.adjustBackground(treeTable);
        ViewUtil.adjustBackground(scrollPane);
        ViewUtil.adjustBackground(scrollPane.getViewport());
    }

    @Override
    public void removeNotify() {
        super.removeNotify();

        if (treeTableParent != null) { //IndexedEditorPanel
            treeTableParent.removeMouseListener(tableMouseListener);
        }

        treeTableParent = null;

        // clear node listeners
        tableModel.setNodes(new Node[] {  });
    }

    @Override
    public void addMouseListener(MouseListener l) {
        super.addMouseListener(l);
        treeTable.getTableHeader().addMouseListener(l);
    }

    @Override
    public void removeMouseListener(MouseListener l) {
        super.removeMouseListener(l);
        treeTable.getTableHeader().removeMouseListener(l);
    }

    /**
     * Drag and drop is not supported in TreeTableView.
     */
    @Override
    public void setDragSource(boolean state) {
    }

    /**
     * Drag and drop is not supported in TreeTableView.
     */
    @Override
    public void setDropTarget(boolean state) {
    }

    /* Overriden to get position for popup invoked by keyboard
     */
    @Override
    Point getPositionForPopup() {
        int row = treeTable.getSelectedRow();

        if (row < 0) {
            return null;
        }

        int col = treeTable.getSelectedColumn();

        if (col < 0) {
            col = 0;
        }

        Rectangle r;

        if (col == 0) {
            r = tree.getRowBounds(row);
        } else {
            r = treeTable.getCellRect(row, col, true);
        }

        Point p = SwingUtilities.convertPoint(treeTable, r.x, r.y, this);

        return p;
    }

    private void createPopup(MouseEvent e) {
        Point p = SwingUtilities.convertPoint(e.getComponent(), e.getX(), e.getY(), TreeTableView.this);

        createPopup(p.x, p.y);

        e.consume();
    }

    @Override
    void createPopup(int xpos, int ypos) {
        int treeXpos = xpos - ((TreeTable) treeTable).getPositionX();

        if (allowHideColumns || allowSortingByColumn) {
            int col = treeTable.getColumnModel().getColumnIndexAtX(treeXpos);
            super.createExtendedPopup(xpos, ypos, getListMenu(col));
        } else {
            super.createPopup(xpos, ypos);
        }
    }

    /* creates List Options menu
     */
    private JMenu getListMenu(final int col) {
        JMenu listItem = new JMenu(NbBundle.getMessage(NodeTableModel.class, "LBL_ListOptions"));

        if (allowHideColumns && (col > 0)) {
            JMenu colsItem = new JMenu(NbBundle.getMessage(NodeTableModel.class, "LBL_ColsMenu"));

            boolean addColsItem = false;

            if (col > 1) {
                JMenuItem moveLItem = new JMenuItem(NbBundle.getMessage(NodeTableModel.class, "LBL_MoveLeft"));
                moveLItem.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
                            treeTable.getColumnModel().moveColumn(col, col - 1);
                        }
                    }
                );
                colsItem.add(moveLItem);
                addColsItem = true;
            }

            if (col < tableModel.getColumnCount()) {
                JMenuItem moveRItem = new JMenuItem(
                        NbBundle.getMessage(NodeTableModel.class, "LBL_MoveRight")
                    );
                moveRItem.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
                            treeTable.getColumnModel().moveColumn(col, col + 1);
                        }
                    }
                );
                colsItem.add(moveRItem);
                addColsItem = true;
            }

            if (addColsItem) {
                listItem.add(colsItem);
            }
        }

        if (allowSortingByColumn) {
            JMenu sortItem = new JMenu(NbBundle.getMessage(NodeTableModel.class, "LBL_SortMenu"));
            JRadioButtonMenuItem noSortItem = new JRadioButtonMenuItem(
                    NbBundle.getMessage(NodeTableModel.class, "LBL_NoSort"),
                    !getSortedNodeTreeModel().isSortingActive()
                );
            noSortItem.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
                        noSorting();
                    }
                }
            );
            sortItem.add(noSortItem);

            int visibleComparable = 0;
            JRadioButtonMenuItem colItem;

            if (treeColumnProperty.isComparable()) {
                visibleComparable++;
                colItem = new JRadioButtonMenuItem(treeTable.getColumnName(0), treeColumnProperty.isSortingColumn());
                colItem.setHorizontalTextPosition(SwingConstants.LEFT);
                colItem.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
                            setSortingColumn(-1);
                        }
                    }
                );
                sortItem.add(colItem);
            }

            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                if (tableModel.isComparableColumn(i)) {
                    visibleComparable++;
                    colItem = new JRadioButtonMenuItem(
                            tableModel.getColumnName(i),
                            tableModel.isSortingColumnEx(tableModel.translateVisibleColumnIndex(i))
                        );
                    colItem.setHorizontalTextPosition(SwingConstants.LEFT);

                    final int index = tableModel.translateVisibleColumnIndex(i);
                    colItem.addActionListener(
                        new ActionListener() {
                            @Override
                            public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
                                setSortingColumn(index);
                            }
                        }
                    );
                    sortItem.add(colItem);
                }
            }

            //add invisible columns
            for (int i = 0; i < tableModel.getColumnCountEx(); i++) {
                if (tableModel.isComparableColumnEx(i) && !tableModel.isVisibleColumnEx(i)) {
                    visibleComparable++;
                    colItem = new JRadioButtonMenuItem(tableModel.getColumnNameEx(i), tableModel.isSortingColumnEx(i));
                    colItem.setHorizontalTextPosition(SwingConstants.LEFT);

                    final int index = i;
                    colItem.addActionListener(
                        new ActionListener() {
                            @Override
                            public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
                                setSortingColumn(index);
                            }
                        }
                    );
                    sortItem.add(colItem);
                }
            }

            if (visibleComparable > 0) {
                sortItem.addSeparator();

                boolean current_sort;

                if (treeColumnProperty.isSortingColumn()) {
                    current_sort = treeColumnProperty.isSortOrderDescending();
                } else {
                    current_sort = tableModel.isSortOrderDescending();
                }

                JRadioButtonMenuItem ascItem = new JRadioButtonMenuItem(
                        NbBundle.getMessage(NodeTableModel.class, "LBL_Ascending"), !current_sort
                    );
                ascItem.setHorizontalTextPosition(SwingConstants.LEFT);
                ascItem.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
                            setSortingOrder(true);
                        }
                    }
                );
                sortItem.add(ascItem);

                JRadioButtonMenuItem descItem = new JRadioButtonMenuItem(
                        NbBundle.getMessage(NodeTableModel.class, "LBL_Descending"), current_sort
                    );
                descItem.setHorizontalTextPosition(SwingConstants.LEFT);
                descItem.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
                            setSortingOrder(false);
                        }
                    }
                );
                sortItem.add(descItem);

                if (!getSortedNodeTreeModel().isSortingActive()) {
                    ascItem.setEnabled(false);
                    descItem.setEnabled(false);
                }

                listItem.add(sortItem);
            }
        }

        if (allowHideColumns) {
            JMenuItem visItem = new JMenuItem(NbBundle.getMessage(NodeTableModel.class, "LBL_ChangeColumns"));
            visItem.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
                        selectVisibleColumns();
                    }
                }
            );

            listItem.add(visItem);
        }

        return listItem;
    }

    /* Sets column to be currently used for sorting
     */
    private void setSortingColumn(int index) {
        tableModel.setSortingColumnEx(index);

        if (index != -1) {
            getSortedNodeTreeModel().setSortedByProperty(
                tableModel.propertyForColumnEx(index), !tableModel.isSortOrderDescending()
            );
            treeColumnProperty.setSortingColumn(false);
        } else {
            getSortedNodeTreeModel().setSortedByName(true, !treeColumnProperty.isSortOrderDescending());
            treeColumnProperty.setSortingColumn(true);
        }

        // to change sort icon
        treeTable.getTableHeader().repaint();
    }

    private void noSorting() {
        tableModel.setSortingColumnEx(-1);
        getSortedNodeTreeModel().setNoSorting();
        treeColumnProperty.setSortingColumn(false);

        // to change sort icon
        treeTable.getTableHeader().repaint();
    }

    /* Sets sorting order for current sorting.
     */
    private void setSortingOrder(boolean ascending) {
        if (treeColumnProperty.isSortingColumn()) {
            treeColumnProperty.setSortOrderDescending(!ascending);
        } else {
            tableModel.setSortOrderDescending(!ascending);
        }

        getSortedNodeTreeModel().setSortOrder(ascending);

        // to change sort icon
        treeTable.getTableHeader().repaint();
    }

    private synchronized SortedNodeTreeModel getSortedNodeTreeModel() {
        if (sortedNodeTreeModel == null) {
            sortedNodeTreeModel = new SortedNodeTreeModel();
        }

        return sortedNodeTreeModel;
    }

    /** This is internal accessible context for TreeTableView.
     * It delegates setAccessibleName and setAccessibleDescription methods to set these properties
     * in underlying TreeTable as well.
     */
    private class AccessibleTreeTableView extends AccessibleJScrollPane {
        AccessibleTreeTableView() {
        }

        @Override
        public void setAccessibleName(String accessibleName) {
            super.setAccessibleName(accessibleName);

            if (treeTable != null) {
                treeTable.getAccessibleContext().setAccessibleName(accessibleName);
            }
        }

        @Override
        public void setAccessibleDescription(String accessibleDescription) {
            super.setAccessibleDescription(accessibleDescription);

            if (treeTable != null) {
                treeTable.getAccessibleContext().setAccessibleDescription(accessibleDescription);
            }
        }
    }

    /* Horizontal scrolling support.
     */
    private final class ScrollListener extends ComponentAdapter implements PropertyChangeListener, ChangeListener {
        boolean movecorrection = false;

        ScrollListener() {
        }

        //Column width
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (((TreeTable) treeTable).getTreeColumnIndex() == -1) {
                return;
            }

            if ("width".equals(evt.getPropertyName())) { // NOI18N

                if (!treeTable.equals(evt.getSource())) {
                    Dimension dim = hScrollBar.getPreferredSize();
                    dim.width = treeTable.getColumnModel().getColumn(((TreeTable) treeTable).getTreeColumnIndex())
                                         .getWidth();
                    hScrollBar.setPreferredSize(dim);
                    hScrollBar.revalidate();
                    hScrollBar.repaint();
                }

                revalidateScrollBar();
            } else if ("positionX".equals(evt.getPropertyName())) { // NOI18N
                revalidateScrollBar();
            } else if ("treeColumnIndex".equals(evt.getPropertyName())) { // NOI18N
                treeTable.getColumnModel().getColumn(((TreeTable) treeTable).getTreeColumnIndex())
                         .addPropertyChangeListener(listener);
            } else if ("column_moved".equals(evt.getPropertyName())) { // NOI18N

                int from = ((Integer) evt.getOldValue()).intValue();
                int to = ((Integer) evt.getNewValue()).intValue();

                if ((from == 0) || (to == 0)) {
                    if (movecorrection) {
                        movecorrection = false;
                    } else {
                        movecorrection = true;

                        // not allowed to move first, tree column
                        treeTable.getColumnModel().moveColumn(to, from);
                    }

                    return;
                }

                // module will be revalidated in NodeTableModel
                treeTable.getTableHeader().getColumnModel().getColumn(from).setModelIndex(from);
                treeTable.getTableHeader().getColumnModel().getColumn(to).setModelIndex(to);
                tableModel.moveColumn(from - 1, to - 1);
            }
        }

        //Viewport height
        @Override
        public void componentResized(ComponentEvent e) {
            revalidateScrollBar();
        }

        //ScrollBar change
        @Override
        public void stateChanged(ChangeEvent evt) {
            int value = hScrollBar.getModel().getValue();
            ((TreeTable) treeTable).setPositionX(value);
        }

        private void revalidateScrollBar() {
            if (!isDisplayable()) {
                return;
            }

            if (
                (treeTable.getColumnModel().getColumnCount() > 0) &&
                    (((TreeTable) treeTable).getTreeColumnIndex() >= 0)
            ) {
                int extentWidth = treeTable.getColumnModel().getColumn(((TreeTable) treeTable).getTreeColumnIndex())
                                           .getWidth();
                int maxWidth = tree.getPreferredSize().width;
                int extentHeight = scrollPane.getViewport().getSize().height;
                int maxHeight = tree.getPreferredSize().height;
                int positionX = ((TreeTable) treeTable).getPositionX();

                int value = Math.max(0, Math.min(positionX, maxWidth - extentWidth));

                boolean hsbvisible = hScrollBar.isVisible();
                boolean vsbvisible = scrollPane.getVerticalScrollBar().isVisible();
                int hsbheight = hsbvisible ? hScrollBar.getHeight() : 0;
                int vsbwidth = scrollPane.getVerticalScrollBar().getWidth();

                hScrollBar.setValues(value, extentWidth, 0, maxWidth);

                if (
                    hideHScrollBar || (maxWidth <= extentWidth) ||
                        (vsbvisible &&
                        ((maxHeight <= (extentHeight + hsbheight)) && (maxWidth <= (extentWidth + vsbwidth))))
                ) {
                    hScrollBar.setVisible(false);
                } else {
                    hScrollBar.setVisible(true);
                }
            }
        }
    }

    /** Scrollable (better say not scrollable) pane. Used as container for
     * left (controlling) and rigth (controlled) scroll panes.
     */
    private static final class CompoundScrollPane extends JPanel implements Scrollable {
        CompoundScrollPane() {
        }

        @Override
        public void setBorder(Border b) {
            //do nothing
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 10;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return true;
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return this.getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 10;
        }
    }

    @Override
    public Insets getInsets() {
        Insets res = getInnerInsets();
        res = new Insets(res.top, res.left, res.bottom, res.right);
        if( null != searchpanel && searchpanel.isVisible() ) {
            res.bottom += searchpanel.getPreferredSize().height;
        }
        return res;
    }

    private Insets getInnerInsets() {
        Insets res = super.getInsets();
        if( null == res ) {
            res = new Insets(0,0,0,0);
        }
        return res;
    }

    private class SearchScrollPaneLayout extends ScrollPaneLayout {

        public SearchScrollPaneLayout() {
        }
        
        @Override
        public void layoutContainer( Container parent ) {
            super.layoutContainer(parent);
            if( null != searchpanel && searchpanel.isVisible() ) {
                Insets innerInsets = getInnerInsets();
                Dimension prefSize = searchpanel.getPreferredSize();
                searchpanel.setBounds(innerInsets.left, parent.getHeight()-innerInsets.bottom-prefSize.height,
                        parent.getWidth()-innerInsets.left-innerInsets.right, prefSize.height);
            }
        }
        
    }

    /** Invokes default action.
     */
    private class DefaultTreeAction implements ActionListener {
        DefaultTreeAction() {
        }

        /**
         * Invoked when an action occurs.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            if (treeTable.getSelectedColumn() != ((TreeTable) treeTable).getTreeColumnIndex()) {
                return;
            }

            Node[] nodes = manager.getSelectedNodes();

            if (nodes.length == 1) {
                Action a = nodes[0].getPreferredAction();

                if (a != null) {
                    if (a.isEnabled()) {
                        a.actionPerformed(new ActionEvent(nodes[0], ActionEvent.ACTION_PERFORMED, "")); // NOI18N
                    } else {
                        Utilities.disabledActionBeep();
                    }
                }
            }
        }
    }

    @Override
    Node getOriginalNode (Node n) {
        if (n instanceof SortedNodeTreeModel.SortedNode) {
            SortedNodeTreeModel.SortedNode sn = (SortedNodeTreeModel.SortedNode) n;
            return sn.getOriginalNode ();
        }
        return n;
    }

    /* Synchronizes selected nodes from the manager of this Explorer.
    */
    @Override
    protected void showSelection(TreePath[] treePaths) {
        TreePath [] modifiedTreePaths = new TreePath [treePaths.length];
        for (int i = 0; i < treePaths.length; i++) {
            TreePath tp = treePaths [i];
            Node o = ((VisualizerNode) tp.getLastPathComponent ()).node;
            TreePath mtp = getTreePath (getSortedNodeFromOriginal (o));
            modifiedTreePaths [i] = mtp;
        }
        super.showSelection (modifiedTreePaths);
    }

    @Override
    public void collapseNode(Node n) {
        super.collapseNode(getSortedNodeFromOriginal(n));
    }

    @Override
    public void expandNode(Node n) {
        super.expandNode(getSortedNodeFromOriginal(n));
    }

    @Override
    public boolean isExpanded(Node n) {
        return super.isExpanded(getSortedNodeFromOriginal(n));
    }

    private Node getSortedNodeFromOriginal (Node orig) {
        if (getSortedNodeTreeModel () != null) {
            if (getSortedNodeTreeModel ().original2filter != null) {
                SortedNodeTreeModel.SortedNode sn = getSortedNodeTreeModel ().original2filter.get (orig);
                if (sn != null) {
                    return sn;
                }
            }
        }
        return orig;
    }

    /* node tree model with added sorting support
     */
    private class SortedNodeTreeModel extends NodeTreeModel {
        private Node.Property sortedByProperty;
        private boolean sortAscending = true;
        private Comparator<Node> rowComparator;
        private boolean sortedByName = false;
        private Map<Node, SortedNode> original2filter = new WeakHashMap<Node, SortedNode> (11);

        @Override
        void setNode (Node root, TreeView.VisualizerHolder visHolder) {
            visHolder.clear ();
            original2filter.clear ();
            super.setNode (new SortedNode (root), null);
        }

        private class SortedNode extends FilterNode {
            public SortedNode (Node original) {
                super(original, original.isLeaf() ? Children.LEAF : new SortedChildren(original));
                original2filter.put (original, this);
            }
            public Node getOriginalNode () {
                return super.getOriginal ();
            }

            @Override
            protected NodeListener createNodeListener() {
                return new FilterNode.NodeAdapter(this) {
                    @Override
                    protected void propertyChange(FilterNode fn, PropertyChangeEvent ev) {
                        super.propertyChange(fn, ev);
                        if (ev.getPropertyName().equals(Node.PROP_LEAF)) {
                            final org.openide.nodes.Children[] newChildren = new org.openide.nodes.Children[1];
                            Children.MUTEX.readAccess(new Runnable() {

                                @Override
                                public void run() {
                                    boolean origIsLeaf = getOriginal().isLeaf();
                                    boolean thisIsLeaf = isLeaf();
                                    if (origIsLeaf && !thisIsLeaf) {
                                        newChildren[0] = Children.LEAF;
                                    } else if (!origIsLeaf && thisIsLeaf) {
                                        newChildren[0] = new SortedChildren(getOriginal());
                                    }
                                }
                            });

                            if (newChildren[0] != null) {
                                Children.MUTEX.postWriteRequest(
                                        new Runnable() {

                                            @Override
                                            public void run() {
                                                setChildren(newChildren[0]);
                                            }
                                        }
                                );
                            }
                        }
                        if (ev.getPropertyName().equals(Node.PROP_PARENT_NODE)) {
                            final Node node = (Node)ev.getSource();
                            if (node.getParentNode() == null) {
                                original2filter.remove(node);
                            }
                        }
                    }
                };
            }
        }

        private class SortedChildren extends FilterNode.Children {
            public SortedChildren (Node n) {
                super (n);
                sortNodes ();
            }

            @Override
            protected Node[] createNodes (Node key) {
                return new Node [] { new SortedNode (key) };
            }

            @Override
            protected void addNotify () {
                super.addNotify ();
                sortNodes ();
            }

            @Override
            protected void filterChildrenAdded (NodeMemberEvent ev) {
                super.filterChildrenAdded (ev);
                sortNodes ();
            }

            @Override
            protected void filterChildrenRemoved (NodeMemberEvent ev) {
                super.filterChildrenRemoved (ev);
                sortNodes ();
            }

            @Override
            protected void filterChildrenReordered (NodeReorderEvent ev) {
                super.filterChildrenReordered (ev);
                sortNodes ();
            }

            private void sortNodes() {
                Node[] origNodes = original.getChildren().getNodes();
                if (isSortingActive()) {
                    Node[] sortedNodes = Arrays.copyOf(origNodes, origNodes.length);
                    Arrays.sort(sortedNodes, getRowComparator());
                    setKeys(sortedNodes);
                } else {
                    setKeys(origNodes);
                }
            }
        }

        void setNoSorting() {
            setSortedByProperty(null);
            setSortedByName(false);
            sortingChanged();
        }

        boolean isSortingActive() {
            return ((sortedByProperty != null) || sortedByName);
        }

        void setSortedByProperty(Node.Property prop) {
            if (sortedByProperty == prop) {
                return;
            }

            sortedByProperty = prop;

            if (prop == null) {
                rowComparator = null;
            } else {
                sortedByName = false;
            }

            sortingChanged();
        }

        void setSortedByProperty(Node.Property prop, boolean ascending) {
            if ((sortedByProperty == prop) && (ascending == sortAscending)) {
                return;
            }

            sortedByProperty = prop;
            sortAscending = ascending;

            if (prop == null) {
                rowComparator = null;
            } else {
                sortedByName = false;
            }

            sortingChanged();
        }

        void setSortedByName(boolean sorted, boolean ascending) {
            if ((sortedByName == sorted) && (ascending == sortAscending)) {
                return;
            }

            sortedByName = sorted;
            sortAscending = ascending;

            if (sortedByName) {
                sortedByProperty = null;
            }

            sortingChanged();
        }

        void setSortedByName(boolean sorted) {
            sortedByName = sorted;

            if (sortedByName) {
                sortedByProperty = null;
            }

            sortingChanged();
        }

        void setSortOrder(boolean ascending) {
            if (ascending == sortAscending) {
                return;
            }

            sortAscending = ascending;
            sortingChanged();
        }

        private Node.Property getNodeProperty(Node node, Node.Property prop) {
            Node.PropertySet[] propsets = node.getPropertySets();

            for (int i = 0, n = propsets.length; i < n; i++) {
                Node.Property[] props = propsets[i].getProperties();

                for (int j = 0, m = props.length; j < m; j++) {
                    if (props[j].equals(prop)) {
                        return props[j];
                    }
                }
            }

            return null;
        }

        synchronized Comparator<Node> getRowComparator() {
            if (rowComparator == null) {
                rowComparator = new Comparator<Node>() {

                    @Override
                    @SuppressWarnings("unchecked")
                    public int compare(Node n1, Node n2) {
                        if (n1 == n2) {
                            return 0;
                        }

                        if ((n1 == null) && (n2 == null)) {
                            return 0;
                        }
                        if (n1 == null) {
                            return 1;
                        }
                        if (n2 == null) {
                            return -1;
                        }
                        if ((n1.getParentNode() == null) ||
                            (n2.getParentNode() == null)) {
                            // PENDING: throw Exception
                            Logger.getAnonymousLogger().warning("TTV.compare: Node " +
                                                                n1 + " or " + n2 +
                                                                " has no parent!");
                            return 0;
                        }
                        if (!(n1.getParentNode().equals(n2.getParentNode()))) {
                            // PENDING: throw Exception
                            Logger.getAnonymousLogger().warning("TTV.compare: Nodes " +
                                                                n1 + " and " +
                                                                n2 +
                                                                " has different parent!");
                            return 0;
                        }
                        int res;

                        if (sortedByName) {
                            res = n1.getDisplayName().compareTo(n2.getDisplayName());
                            return sortAscending ? res
                                                 : (-res);
                        }
                        Property p1 = getNodeProperty(n1, sortedByProperty);
                        Property p2 = getNodeProperty(n2, sortedByProperty);

                        if ((p1 == null) && (p2 == null)) {
                            return 0;
                        }
                        try {
                            if (p1 == null) {
                                res = -1;
                            } else if (p2 == null) {
                                res = 1;
                            } else {
                                Object v1 = p1.getValue();
                                Object v2 = p2.getValue();

                                if ((v1 == null) && (v2 == null)) {
                                    return 0;
                                } else if (v1 == null) {
                                    res = -1;
                                } else if (v2 == null) {
                                    res = 1;
                                } else {
                                    if ((v1.getClass() != v2.getClass()) ||
                                        !(v1 instanceof Comparable)) {
                                        v1 = v1.toString();
                                        v2 = v2.toString();
                                    }
                                    res = ((Comparable) v1).compareTo(v2);
                                }
                            }
                            return sortAscending ? res
                                                 : (-res);
                        }
                        catch (Exception ex) {
                            Logger.getLogger(TreeTableView.class.getName()).log(Level.WARNING, null, ex);
                            return 0;
                        }
                    }
                };
            }

            return rowComparator;
        }

        void sortingChanged() {
            // PENDING: remember the last sorting to avoid multiple sorting
            // remenber expanded folders
            TreeNode tn = (TreeNode) (this.getRoot());
            java.util.List<TreePath> list = new ArrayList<TreePath>();
            Enumeration<TreePath> en = TreeTableView.this.tree.getExpandedDescendants(new TreePath(tn));

            while ((en != null) && en.hasMoreElements()) {
                TreePath path = en.nextElement();

                // bugfix #32328, don't sort whole subtree but only expanded folders
                Node n = ((VisualizerNode) path.getLastPathComponent ()).node;
                Children children = n.getChildren();
                if (children instanceof SortedChildren) {
                    ((SortedChildren) children).sortNodes ();
                    list.add(path);
                } // else Children.LEAF
            }

            // expand again folders
            for (int i = 0; i < list.size(); i++) {
                TreeTableView.this.tree.expandPath(list.get(i));
            }
        }

        String getRootDescription() {
            if (getRoot() instanceof VisualizerNode) {
                //#37802 commenting this out - unfathomable why you would need
                //to sort the root's children in order to get its short 
                //description - Tim
                //                sortChildren ((VisualizerNode)getRoot ());
                return ((VisualizerNode) getRoot()).getShortDescription();
            }

            return ""; // NOI18N
        }

    }

    /* Cell renderer for sorting column header.
     */
    private class SortingHeaderRenderer extends DefaultTableCellRenderer {
        SortingHeaderRenderer() {
        }

        /** Overrides superclass method. */
        @Override
        public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column
        ) {
            Component comp = defaultHeaderRenderer.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column
                );

            if (comp instanceof JLabel) {
                if ((column == 0) && treeColumnProperty.isSortingColumn()) {
                    ((JLabel) comp).setIcon(getProperIcon(treeColumnProperty.isSortOrderDescending()));
                    ((JLabel) comp).setHorizontalTextPosition(SwingConstants.LEFT);

                    if( Utilities.isWindows() ) {
                        comp.setFont(getFont().deriveFont(Font.BOLD, getFont().getSize()));
                    } else {
                        // don't use deriveFont() - see #49973 for details
                        comp.setFont(new Font(getFont().getName(), Font.BOLD, getFont().getSize()));
                    }
                } else if ((column != 0) && ((tableModel.getVisibleSortingColumn() + 1) == column)) {
                    ((JLabel) comp).setIcon(getProperIcon(tableModel.isSortOrderDescending()));
                    ((JLabel) comp).setHorizontalTextPosition(SwingConstants.LEFT);

                    if( Utilities.isWindows() ) {
                        comp.setFont(getFont().deriveFont(Font.BOLD, getFont().getSize()));
                    } else {
                        // don't use deriveFont() - see #49973 for details
                        comp.setFont(new Font(getFont().getName(), Font.BOLD, getFont().getSize()));
                    }
                } else {
                    ((JLabel) comp).setIcon(null);
                }
            }

            return comp;
        }

        private ImageIcon getProperIcon(boolean descending) {
            if (descending) {
                return ImageUtilities.loadImageIcon(SORT_DESC_ICON, false);
            } else {
                return ImageUtilities.loadImageIcon(SORT_ASC_ICON, false);
            }
        }
    }
     // End of inner class SortingHeaderRenderer.

    private static class TreeColumnProperty {
        private Property p = null;

        TreeColumnProperty() {
        }

        void setProperty(Property p) {
            this.p = p;
        }

        boolean isComparable() {
            if (p == null) {
                return false;
            }

            Object o = p.getValue(NodeTableModel.ATTR_COMPARABLE_COLUMN);

            if (o instanceof Boolean) {
                return ((Boolean) o).booleanValue();
            }

            return false;
        }

        boolean isSortingColumn() {
            if (p == null) {
                return false;
            }

            Object o = p.getValue(NodeTableModel.ATTR_SORTING_COLUMN);

            if (o instanceof Boolean) {
                return ((Boolean) o).booleanValue();
            }

            return false;
        }

        void setSortingColumn(boolean sorting) {
            if (p == null) {
                return;
            }

            p.setValue(NodeTableModel.ATTR_SORTING_COLUMN, sorting ? Boolean.TRUE : Boolean.FALSE);
        }

        boolean isSortOrderDescending() {
            if (p == null) {
                return false;
            }

            Object o = p.getValue(NodeTableModel.ATTR_DESCENDING_ORDER);

            if (o instanceof Boolean) {
                return ((Boolean) o).booleanValue();
            }

            return false;
        }

        void setSortOrderDescending(boolean descending) {
            if (p == null) {
                return;
            }

            p.setValue(NodeTableModel.ATTR_DESCENDING_ORDER, descending ? Boolean.TRUE : Boolean.FALSE);
        }
    }

    /* For testing - use internal execution
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Node n = //new org.netbeans.core.ModuleNode();
                    RepositoryNodeFactory.getDefault().repository(DataFilter.ALL);

                org.openide.explorer.ExplorerManager em = new org.openide.explorer.ExplorerManager();
                em.setRootContext(n);

                org.openide.explorer.ExplorerPanel ep = new org.openide.explorer.ExplorerPanel(em);
                ep.setLayout (new BorderLayout ());
                ep.setBorder(new EmptyBorder(20, 20, 20, 20));

                TreeTableView ttv = new TreeTableView();
                ttv.setRootVisible(false);
                ttv.setPopupAllowed(true);
                ttv.setDefaultActionAllowed(true);
                ttv.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );
                ttv.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );

                org.openide.nodes.PropertySupport.ReadOnly prop2
                    = new org.openide.nodes.PropertySupport.ReadOnly (
                            "name", // NOI18N
                            String.class,
                            "name",
                            "Name Tooltip"
                        ) {
                            public Object getValue () {
                                return null;
                            }

                        };
                //prop2.setValue( "InvisibleInTreeTableView", Boolean.TRUE );
                prop2.setValue( "SortingColumnTTV", Boolean.TRUE );
                prop2.setValue( "DescendingOrderTTV", Boolean.TRUE );
                prop2.setValue( "ComparableColumnTTV", Boolean.TRUE );

                ttv.setProperties(
    //                    n.getChildren().getNodes()[0].getPropertySets()[0].getProperties());
                    new Property[]{
                        new org.openide.nodes.PropertySupport.ReadWrite (
                            "hidden", // NOI18N
                            Boolean.TYPE,
                            "hidden",
                            "Hidden tooltip"
                        ) {
                            public Object getValue () {
                                return null;
                            }

                            public void setValue (Object o) {
                            }
                        },
                        prop2,
                        new org.openide.nodes.PropertySupport.ReadOnly (
                            "template", // NOI18N
                            Boolean.TYPE,
                            "template",
                            "Template Tooltip"
                        ) {
                            public Object getValue () {
                                return null;
                            }

                        }

                    }
                );
                ttv.setTreePreferredWidth(200);

                ttv.setTableColumnPreferredWidth(0, 60);
                ttv.setTableColumnPreferredWidth(1, 150);
                ttv.setTableColumnPreferredWidth(2, 100);


                ep.add("Center", ttv);
                ep.open();
            }
        });
    }
    */
}
