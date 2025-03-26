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

import javax.swing.table.TableColumnModel;
import org.openide.explorer.propertysheet.PropertyPanel;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicTableUI;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.tree.*;
import org.openide.explorer.view.TableQuickSearchSupport.QuickSearchSettings;
import org.openide.nodes.Children;
import org.openide.util.Mutex;


/**
 * TreeTable implementation.
 *
 * @author Jan Rojcek
 */
class TreeTable extends JTable implements Runnable {
    /** Action key for up/down focus action */
    private static final String ACTION_FOCUS_NEXT = "focusNext"; //NOI18N
    private static Color unfocusedSelBg = null;
    private static Color unfocusedSelFg = null;

    /** A subclass of JTree. */
    private TreeTableCellRenderer tree;
    private NodeTableModel tableModel;
    private int treeColumnIndex = -1;

    /** Tree editor stuff. */
    private int lastRow = -1;
    private boolean canEdit;
    private boolean ignoreScrolling = false;

    /** Flag to ignore clearSelection() called from super.tableChanged(). */
    private boolean ignoreClearSelection = false;

    /** Position of tree renderer, used for horizontal scrolling. */
    private int positionX;

    /** If true, horizontal scrolling of tree column is enabled in TreeTableView */
    private boolean treeHScrollingEnabled = true;
    private final ListToTreeSelectionModelWrapper selectionWrapper;
    private boolean edCreated = false;
    boolean inSelectAll = false;
    private boolean needCalcRowHeight = true;
    boolean inEditRequest = false;
    boolean inEditorChangeRequest = false;
    int editRow = -1;
    private boolean inRemoveRequest = false;
    
    private TableSheetCell tableCell;

    public TreeTable(NodeTreeModel treeModel, NodeTableModel tableModel) {
        super();

        setSurrendersFocusOnKeystroke(true);

        this.tree = new TreeTableCellRenderer(treeModel);
        this.tableModel = new TreeTableModelAdapter(tree, tableModel);

        tree.setCellRenderer(new NodeRenderer());

        // Install a tableModel representing the visible rows in the tree. 
        setModel(this.tableModel);

        // Force the JTable and JTree to share their row selection models. 
        selectionWrapper = new ListToTreeSelectionModelWrapper();
        tree.setSelectionModel(selectionWrapper);
        setSelectionModel(selectionWrapper.getListSelectionModel());
        getTableHeader().setReorderingAllowed(false);

        // Install the tree editor renderer and editor. 
        setDefaultRenderer(TreeTableModelAdapter.class, tree);

        // Install property renderer and editor.
        tableCell = new TableSheetCell(this.tableModel);
        tableCell.setFlat(true);
        setDefaultRenderer(Property.class, tableCell);
        setDefaultEditor(Property.class, tableCell);
        getTableHeader().setDefaultRenderer(tableCell);
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(TreeTable.class, "ACSN_TreeTable")); // NOI18N
        getAccessibleContext().setAccessibleDescription( // NOI18N
            NbBundle.getMessage(TreeTable.class, "ACSD_TreeTable")); // NOI18N

        setFocusCycleRoot(true);
        setFocusTraversalPolicy(new STPolicy());
        putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        putClientProperty("JTable.autoStartsEdit", Boolean.FALSE);

        initKeysAndActions();
    }

    private void initKeysAndActions() {
        setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.<AWTKeyStroke>emptySet());
        setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, Collections.<AWTKeyStroke>emptySet());

        //Next two lines do not work using inputmap/actionmap, but do work
        //using the older API.  We will process ENTER to skip to next row,
        //not next cell
        unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
        unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, Event.SHIFT_MASK));

        InputMap imp = getInputMap(WHEN_FOCUSED);
        InputMap imp2 = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap am = getActionMap();

        // copied from TreeView which tried to fix #18292
        // by doing this
        imp2.put(KeyStroke.getKeyStroke("control C"), "none"); // NOI18N
        imp2.put(KeyStroke.getKeyStroke("control V"), "none"); // NOI18N
        imp2.put(KeyStroke.getKeyStroke("control X"), "none"); // NOI18N
        imp2.put(KeyStroke.getKeyStroke("COPY"), "none"); // NOI18N
        imp2.put(KeyStroke.getKeyStroke("PASTE"), "none"); // NOI18N
        imp2.put(KeyStroke.getKeyStroke("CUT"), "none"); // NOI18N

        imp.put(
            KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK, false), ACTION_FOCUS_NEXT
        );
        imp.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.CTRL_MASK, false), ACTION_FOCUS_NEXT);

        Action ctrlTab = new CTRLTabAction();
        am.put(ACTION_FOCUS_NEXT, ctrlTab);

        getActionMap().put(
            "selectNextColumn", // NOI18N
            new TreeTableAction(
                tree.getActionMap().get("selectChild"), // NOI18N
                getActionMap().get("selectNextColumn")
            )
        ); // NOI18N
        getActionMap().put(
            "selectPreviousColumn", // NOI18N
            new TreeTableAction(
                tree.getActionMap().get("selectParent"), // NOI18N
                getActionMap().get("selectPreviousColumn")
            )
        ); // NOI18N

        getAccessibleContext().setAccessibleName(NbBundle.getMessage(TreeTable.class, "ACSN_TreeTable")); // NOI18N
        getAccessibleContext().setAccessibleDescription( // NOI18N
            NbBundle.getMessage(TreeTable.class, "ACSD_TreeTable")); // NOI18N

        imp.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false), "beginEdit");
        getActionMap().put("beginEdit", new EditAction());

        imp2.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), "cancelEdit");
        getActionMap().put("cancelEdit", new CancelEditAction());

        imp.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), "enter");
        getActionMap().put("enter", new EnterAction());

        imp.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "next");

        imp.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK), "previous");

        am.put("next", new NavigationAction(true));
        am.put("previous", new NavigationAction(false));
    }

    @Override
    public TableCellEditor getDefaultEditor(Class columnClass) {
        if (!edCreated && (columnClass == TreeTableModelAdapter.class)) {
            //Creating this editor in the constructor can take > 100ms even
            //on a very fast machine, so do it lazily here to improve 
            //performance of creating a TreeTable
            setDefaultEditor(TreeTableModelAdapter.class, new TreeTableCellEditor());
            edCreated = true;
        }

        return super.getDefaultEditor(columnClass);
    }

    @Override
    public void selectAll() {
        //#48242 - select all over 1000 nodes generates 1000 re-sorts
        inSelectAll = true;

        try {
            super.selectAll();
        } finally {
            inSelectAll = false;
            selectionWrapper.updateSelectedPathsFromSelectedRows();
        }
    }

    /*
     * Overridden to message super and forward the method to the tree.
     */
    @Override
    public void updateUI() {
        super.updateUI();

        if (tree != null) {
            tree.updateUI();
        }
        
        if( null != tableCell ) {
            tableCell.updateUI();
        }

        // Use the tree's default foreground and background colors in the
        // table. 
        LookAndFeel.installColorsAndFont(this, "Tree.background", // NOI18N
            "Tree.foreground", "Tree.font"
        ); // NOI18N

        if (UIManager.getColor("Table.selectionBackground") == null) { // NOI18N
            UIManager.put("Table.selectionBackground", new JTable().getSelectionBackground()); // NOI18N
        }

        if (UIManager.getColor("Table.selectionForeground") == null) { // NOI18N
            UIManager.put("Table.selectionForeground", new JTable().getSelectionForeground()); // NOI18N
        }

        if (UIManager.getColor("Table.gridColor") == null) { // NOI18N
            UIManager.put("Table.gridColor", new JTable().getGridColor()); // NOI18N
        }

        setUI(new TreeTableUI());
        needCalcRowHeight = true;
    }

    /* Workaround for BasicTableUI anomaly. Make sure the UI never tries to
     * paint the editor. The UI currently uses different techniques to
     * paint the renderers and editors and overriding setBounds() below
     * is not the right thing to do for an editor. Returning -1 for the
     * editing row in this case, ensures the editor is never painted.
     */
    @Override
    public int getEditingRow() {
        return (getColumnClass(editingColumn) == TreeTableModelAdapter.class) ? (-1) : editingRow;
    }

    /** Overridden - JTable's implementation of the method will
     *  actually attach (and leave behind) a gratuitous border
     *  on the enclosing scroll pane. */
    @Override
    protected final void configureEnclosingScrollPane() {
        Container p = getParent();

        if (p instanceof JViewport) {
            Container gp = p.getParent();

            if (gp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) gp;
                JViewport viewport = scrollPane.getViewport();

                if ((viewport == null) || (viewport.getView() != this)) {
                    return;
                }

                JTableHeader jth = getTableHeader();

                if (jth != null) {
                    jth.setBorder(null);
                }

                scrollPane.setColumnHeaderView(jth);
            }
        }
    }
    
    private QuickSearchSettings qss = new QuickSearchSettings();
    
    QuickSearchSettings getQuickSearchSettings() {
        return qss;
    }
    
    private QuickSearchTableFilter qstf = new DefaultQuickSearchTableFilter();
    
    QuickSearchTableFilter getQuickSearchTableFilter() {
        return qstf;
    }

    private final class DefaultQuickSearchTableFilter implements QuickSearchTableFilter {

        @Override
        public String getStringValueAt(int row, int col) {
            Object value = getValueAt(row, col);
            String str;
            if (value instanceof Property) {
                Property p = (Property) value;
                Object v = null;
                try {
                    v = p.getValue();
                } catch (IllegalAccessException ex) {
                } catch (InvocationTargetException ex) {
                }
                if (v instanceof String) {
                    str = (String) v;
                } else {
                    str = null;
                }
            } else if (value instanceof VisualizerNode) {
                str = ((VisualizerNode) value).getDisplayName();
                //str = Visualizer.findNode(value).getDisplayName();
            } else {
                str = null;
            }
            return str;
        }
        
    }
    
    private class GuardedActions implements Mutex.Action<Object> {

        private int type;
        private Object p1;
        final Object ret;

        public GuardedActions(int type, Object p1) {
            this.type = type;
            this.p1 = p1;
            ret = Children.MUTEX.readAccess(this);
        }

        public Object run() {
            switch (type) {
                case 0:
                    paintImpl((Graphics) p1);
                    break;
                case 1:
                    TreeTable.super.validateTree();
                    break;
                case 2:
                    TreeTable.super.doLayout();
                    break;
                case 3:
                    repaintSelection((Boolean) p1);
                    break;
                case 4:
                    TreeTable.super.processEvent((AWTEvent) p1);
                    break;
                case 5:
                    return TreeTable.super.getPreferredSize();
                case 6:
                    //return getToolTipTextImpl((MouseEvent) p1);
                case 10:
                    Object[] arr = (Object[]) p1;
                    return TreeTable.super.processKeyBinding(
                            (KeyStroke) arr[0],
                            (KeyEvent) arr[1],
                            (Integer) arr[2],
                            (Boolean) arr[3]);                
                default:
                    throw new IllegalStateException("type: " + type);
            }

            return null;
        }
    } 

    @Override
    public void paint(Graphics g) {
         new GuardedActions(0, g);
    }
    
    public void paintImpl(Graphics g) {
        if (needCalcRowHeight) {
            calcRowHeight(g);
            return;
        }

        /*
        long time = perf.highResCounter();
         */
        super.paint(g);

        /*
        double dur = perf.highResCounter()-time;

        total += dur;
        System.err.println("Paint time: " + total + " ticks = " + (total / perf.highResFrequency()) + " ms. ");
         */
    }

    @Override
    protected void validateTree() {
        new GuardedActions(1, null);
    }

    @Override
    public Dimension getPreferredSize() {
        return (Dimension) new GuardedActions(5, null).ret;
    }

    @Override
    public void doLayout() {
        new GuardedActions(2, null);
    }

    @Override
    protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
        return (Boolean) new GuardedActions(10, new Object[]{ks, e, condition, pressed}).ret;
    }

    
    //   private static final sun.misc.Perf perf = sun.misc.Perf.getPerf();
    //   private static double total = 0; 

    /** Calculate the height of rows based on the current font.  This is
     *  done when the first paint occurs, to ensure that a valid Graphics
     *  object is available.
     *  @since 1.25   */
    private void calcRowHeight(Graphics g) {
        Font f = getFont();
        FontMetrics fm = g.getFontMetrics(f);
        int rh = fm.getHeight() + fm.getMaxDescent();
        needCalcRowHeight = false;
        rh = Math.max(20, rh);
        tree.setRowHeight(rh);
        setRowHeight(rh);
    }

    /**
     * Returns the tree that is being shared between the model.
     */
    JTree getTree() {
        return tree;
    }

    /**
      * Returns table column index of the column displaying the tree.
      */
    int getTreeColumnIndex() {
        return treeColumnIndex;
    }

    /**
     * Sets tree column index and fires property change.
     */
    void setTreeColumnIndex(int index) {
        if (treeColumnIndex == index) {
            return;
        }

        int old = treeColumnIndex;
        treeColumnIndex = index;
        firePropertyChange("treeColumnIndex", old, treeColumnIndex);
    }

    /* Overriden to do not clear a selection upon model changes.
     */
    @Override
    public void clearSelection() {
        if (!ignoreClearSelection) {
            super.clearSelection();
        }
    }
    
    /* Updates tree column name and sets ignoreClearSelection flag.
     */
    @Override
    public void tableChanged(TableModelEvent e) {
        // update tree column name
        int modelColumn = getTreeColumnIndex();

        if ((e.getFirstRow() <= 0) && (modelColumn != -1) && (getColumnCount() > 0)) {
            String columnName = getModel().getColumnName(modelColumn);
            TableColumn aColumn = getColumnModel().getColumn(modelColumn);
            aColumn.setHeaderValue(columnName);
        }

        ignoreClearSelection = true;

        try {
            super.tableChanged(e);
            //#61728 - force update of tree's horizontal scrollbar
            if( null != getTree() ) {
                firePropertyChange( "positionX", -1, getPositionX() );
            }
        } finally {
            ignoreClearSelection = false;
        }
    }

    @Override
    public void processKeyEvent(KeyEvent e) {
        //Manually hook in the bindings for tab - does not seem to get called
        //automatically
        if (isEditing() && ((e.getKeyCode() == KeyEvent.VK_DOWN) || (e.getKeyCode() == KeyEvent.VK_UP))) {
            return; //XXX
        }

        //Bypass standard tab and escape handling, and use our registered
        //actions instead
        if (
            !isEditing() ||
                (((e.getKeyCode() != KeyEvent.VK_TAB) && (e.getKeyCode() != KeyEvent.VK_ESCAPE)) ||
                ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0))
        ) {
            super.processKeyEvent(e);
        } else {
            processKeyBinding(
                KeyStroke.getKeyStroke(e.getKeyCode(), e.getModifiersEx(), e.getID() == KeyEvent.KEY_RELEASED), e,
                JComponent.WHEN_FOCUSED, e.getID() == KeyEvent.KEY_PRESSED
            );
        }
    }

    /* Performs horizontal scrolling of the tree when editing is started.
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean editCellAt(int row, int column, EventObject e) {
        if (e instanceof MouseEvent && (column != 0)) {
            MouseEvent me = (MouseEvent) e;

            if (!SwingUtilities.isLeftMouseButton(me) || (me.getID() != MouseEvent.MOUSE_PRESSED)) {
                return false;
            }
        }

        if ((row >= getRowCount()) || (row < 0) || (column > getColumnCount()) || (column < 0)) {
            //I don't want to know why this happens, but it does.
            return false;
        }

        inEditRequest = true;
        editRow = row;

        if ((editingRow == row) && (editingColumn == column) && isEditing()) {
            //discard edit requests if we're already editing that cell
            inEditRequest = false;

            return false;
        }

        if (isEditing()) {
            inEditorChangeRequest = true;

            try {
                removeEditor();
                changeSelection(row, column, false, false);
            } finally {
                inEditorChangeRequest = false;
            }
        }

        //Treat a keyEvent request to edit on a non-editable
        //column as a request to edit the nearest column that is
        //editable
        boolean editable = getModel().isCellEditable(row, column);

        //We never want to invoke node name editing from the keyboard,
        //it doesn't work anyway - better to look for an editable property
        if (editable && ((e == null) || e instanceof KeyEvent) && (column == 0)) {
            editable = false;
            column = 1;
        }

        boolean columnShifted = false;

        if (!editable && (e instanceof KeyEvent || (e == null))) {
            for (int i = column; i < getColumnCount(); i++) {
                if (getModel().isCellEditable(row, i)) {
                    columnShifted = i != column;
                    column = i;
                    changeSelection(row, column, false, false);

                    break;
                }
            }
        }

        final Rectangle r = getCellRect(row, column, true);

        //#44226 - Provide a way to invoke the custom editor on disabled cells
        boolean canTryCustomEditor = (!columnShifted && e instanceof MouseEvent)
            ? ((((MouseEvent) e).getX() > ((r.x + r.width) - 24)) && (((MouseEvent) e).getX() < (r.x + r.width))) : true;

        try {
            canEdit = (lastRow == row);

            Object o = getValueAt(row, column);

            if (o instanceof Property) { // && (e == null || e instanceof KeyEvent)) {

                //Toggle booleans without instantiating an editor
                Property p = (Property) o;

                if (p.canWrite() && ((p.getValueType() == Boolean.class) || (p.getValueType() == Boolean.TYPE))) {
                    try {
                        Boolean val = (Boolean) p.getValue();

                        if (Boolean.FALSE.equals(val)) {
                            p.setValue(Boolean.TRUE);
                        } else {
                            //This covers null multi-selections too
                            p.setValue(Boolean.FALSE);
                        }

                        repaint(r.x, r.y, r.width, r.height);

                        return false;
                    } catch (Exception e1) {
                        Logger.getLogger(TreeTable.class.getName()).log(Level.WARNING, null, e1);

                        return false;
                    }
                } else if (canTryCustomEditor && !Boolean.TRUE.equals(p.getValue("suppressCustomEditor"))) { //NOI18N

                    PropertyPanel panel = new PropertyPanel(p);
                    @SuppressWarnings("deprecation")
                    PropertyEditor ed = panel.getPropertyEditor();

                    if ((ed != null) && ed.supportsCustomEditor()) {
                        Action act = panel.getActionMap().get("invokeCustomEditor"); //NOI18N

                        if (act != null) {
                            SwingUtilities.invokeLater(
                                new Runnable() {
                                    public void run() {
                                        r.x = 0;
                                        r.width = getWidth();
                                        TreeTable.this.repaint(r);
                                    }
                                }
                            );
                            act.actionPerformed(null);

                            return false;
                        }
                    }
                }

                if (!p.canWrite()) {
                    return false;
                }
            }

            boolean ret = super.editCellAt(row, column, e);

            if (ret) {
                //InvokeLater to get out of the way of anything the winsys is going to do
                if (column == getTreeColumnIndex()) {
                    ignoreScrolling = true;
                    tree.scrollRectToVisible(tree.getRowBounds(row));
                    ignoreScrolling = false;
                } else {
                    SwingUtilities.invokeLater(this);
                }
            }

            return ret;
        } finally {
            inEditRequest = false;
        }
    }

    /**
     *
     */
    public void run() {
        if ((editorComp != null) && editorComp.isShowing()) {
            editorComp.requestFocus();
        }
    }

    /*
     */
    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (getSelectedRowCount() == 1) {
            lastRow = getSelectedRow();
        } else {
            lastRow = -1;
        }

        super.valueChanged(e);
    }

    /* Updates tree column index
     */
    @Override
    public void columnAdded(TableColumnModelEvent e) {
        super.columnAdded(e);
        updateTreeColumnIndex();
    }

    /* Updates tree column index
     */
    @Override
    public void columnRemoved(TableColumnModelEvent e) {
        super.columnRemoved(e);
        updateTreeColumnIndex();
    }

    /* Updates tree column index
     */
    @Override
    public void columnMoved(TableColumnModelEvent e) {
        super.columnMoved(e);
        updateTreeColumnIndex();

        int from = e.getFromIndex();
        int to = e.getToIndex();

        if (from != to) {
            firePropertyChange("column_moved", from, to); // NOI18N
        }
    }

    /* Updates tree column index
     */
    private void updateTreeColumnIndex() {
        for (int i = getColumnCount() - 1; i >= 0; i--) {
            if (getColumnClass(i) == TreeTableModelAdapter.class) {
                setTreeColumnIndex(i);

                return;
            }
        }

        setTreeColumnIndex(-1);
    }

    /** Returns x coordinate of tree renderer.
     */
    public int getPositionX() {
        return positionX;
    }

    /** Sets x position.
     */
    public void setPositionX(int x) {
        if ((x == positionX) || !treeHScrollingEnabled) {
            return;
        }

        int old = positionX;
        positionX = x;

        firePropertyChange("positionX", old, x);

        if (isEditing() && (getEditingColumn() == getTreeColumnIndex())) {
            CellEditor editor = getCellEditor();

            if (ignoreScrolling && editor instanceof TreeTableCellEditor) {
                ((TreeTableCellEditor) editor).revalidateTextField();
            } else {
                removeEditor();
            }
        }

        repaint();
    }

    /** Overridden to manually draw the focused rectangle for the tree column */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (hasFocus() && (getSelectedColumn() == 0) && (getSelectedRow() > 0)) {
            Color bdr = UIManager.getColor("Tree.selectionBorderColor"); //NOI18N

            if (bdr == null) {
                //Button focus color doesn't work on win classic - better to
                //get the color from a value we know will work - Tim
                if (getForeground().equals(Color.BLACK)) { //typical
                    bdr = getBackground().darker();
                } else {
                    bdr = getForeground().darker();
                }
            }

            g.setColor(bdr);

            Rectangle r = getCellRect(getSelectedRow(), getSelectedColumn(), false);
            g.drawRect(r.x + 1, r.y + 1, r.width - 3, r.height - 3);
        }
    }

    /** Enables horizontal scrolling of tree column */
    void setTreeHScrollingEnabled(boolean enabled) {
        treeHScrollingEnabled = enabled;
    }

    boolean isKnownComponent(Component c) {
        if (c == null) {
            return false;
        }

        if (isAncestorOf(c)) {
            return true;
        }

        if (c == editorComp) {
            return true;
        }

        if ((editorComp instanceof Container) && ((Container) editorComp).isAncestorOf(c)) {
            return true;
        }

        return false;
    }

    public boolean isValidationRoot() {
        return true;
    }

    @Override
    public void paintImmediately(int x, int y, int w, int h) {
        //Eliminate duplicate repaints in an editor change request
        if (inEditorChangeRequest) {
            return;
        }

        super.paintImmediately(x, y, w, h);
    }

    @Override
    protected void processFocusEvent(FocusEvent fe) {
        super.processFocusEvent(fe);

        //Remove the editor here if the new focus owner is not
        //known to the table & the focus event is not temporary
        if ((fe.getID() == FocusEvent.FOCUS_LOST) && !fe.isTemporary() && !inRemoveRequest && !inEditRequest) {
            boolean stopEditing = ((fe.getOppositeComponent() != getParent()) &&
                !isKnownComponent(fe.getOppositeComponent()) && (fe.getOppositeComponent() != null));

            if (stopEditing) {
                removeEditor();
            }
        }

        //The UI will only repaint the lead selection, but we need to
        //paint all selected rows for the color to change when focus
        //is lost/gained
        if (!inRemoveRequest && !inEditRequest) {
            repaintSelection(fe.getID() == FocusEvent.FOCUS_GAINED);
        }
    }

    @Override
    public void removeEditor() {
        inRemoveRequest = true;

        try {
            synchronized (getTreeLock()) {
                super.removeEditor();
            }
        } finally {
            inRemoveRequest = false;
        }
    }

    /** Repaint the selected row */
    private void repaintSelection(boolean focused) {
        if (Children.MUTEX.isReadAccess() || Children.MUTEX.isWriteAccess()) {
            int start = getSelectionModel().getMinSelectionIndex();
            int end = getSelectionModel().getMaxSelectionIndex();

            if (end != -1) {
                if (end != start) {
                    Rectangle begin = getCellRect(start, 0, false);
                    Rectangle r = getCellRect(end, 0, false);

                    r.y = begin.y;
                    r.x = 0;
                    r.width = getWidth();
                    r.height = (r.y + r.height) - begin.y;
                    repaint(r.x, r.y, r.width, r.height);
                } else {
                    Rectangle r = getCellRect(start, 0, false);
                    r.width = getWidth();
                    r.x = 0;
                    repaint(r.x, r.y, r.width, r.height);
                }
            }

            if (isEditing() && (editorComp != null)) {
                editorComp.setBackground(focused ? getSelectionBackground() : getUnfocusedSelectedBackground());
                editorComp.setForeground(focused ? getSelectionForeground() : getUnfocusedSelectedForeground());
            }
        } else {
            new GuardedActions(3, focused);
        }
    }

    /** Get the system-wide unfocused selection background color */
    static Color getUnfocusedSelectedBackground() {
        if (unfocusedSelBg == null) {
            //allow theme/ui custom definition
            unfocusedSelBg = UIManager.getColor("nb.explorer.unfocusedSelBg"); //NOI18N

            if (unfocusedSelBg == null) {
                //try to get standard shadow color
                unfocusedSelBg = UIManager.getColor("controlShadow"); //NOI18N

                if (unfocusedSelBg == null) {
                    //Okay, the look and feel doesn't suport it, punt
                    unfocusedSelBg = Color.lightGray;
                }

                //Lighten it a bit because disabled text will use controlShadow/
                //gray
                unfocusedSelBg = unfocusedSelBg.brighter();
            }
        }

        return unfocusedSelBg;
    }

    /** Get the system-wide unfocused selection foreground color */
    static Color getUnfocusedSelectedForeground() {
        if (unfocusedSelFg == null) {
            //allow theme/ui custom definition
            unfocusedSelFg = UIManager.getColor("nb.explorer.unfocusedSelFg"); //NOI18N

            if (unfocusedSelFg == null) {
                //try to get standard shadow color
                unfocusedSelFg = UIManager.getColor("textText"); //NOI18N

                if (unfocusedSelFg == null) {
                    //Okay, the look and feel doesn't suport it, punt
                    unfocusedSelFg = Color.BLACK;
                }
            }
        }

        return unfocusedSelFg;
    }

    @Override
    protected JTableHeader createDefaultTableHeader() {
        return new TreeTableHeader( getColumnModel() );
    }
    
    /**
     * #53748: Default TableHeader provides wrong preferred height when the first column
     * uses default renderer and has no value.
     */
    private static class TreeTableHeader extends JTableHeader {
        public TreeTableHeader( TableColumnModel columnModel ) {
            super( columnModel );
        }

        @Override
        public Dimension getPreferredSize() {

            Dimension retValue = super.getPreferredSize();
            
            Component comp = getDefaultRenderer().getTableCellRendererComponent( getTable(), 
						"X", false, false, -1, 0 );
            int rendererHeight = comp.getPreferredSize().height; 
            retValue.height = Math.max(retValue.height, rendererHeight); 
            return retValue;
        }
    }

    /**
     * A TreeCellRenderer that displays a JTree.
     */
    class TreeTableCellRenderer extends JTree implements TableCellRenderer {
        /** Last table/tree row asked to renderer. */
        protected int visibleRow;

        /* Last width of the tree.
         */
        private int oldWidth;
        private int transY = 0;

        public TreeTableCellRenderer(TreeModel model) {
            super(model);
            setToggleClickCount(0);
            putClientProperty("JTree.lineStyle", "None"); // NOI18N
        }

        @Override
        public void validate() {
            //do nothing
        }

        @Override
        public void repaint(long tm, int x, int y, int width, int height) {
            //do nothing
        }

        @Override
        public void addHierarchyListener(java.awt.event.HierarchyListener hl) {
            //do nothing
        }

        @Override
        public void addComponentListener(java.awt.event.ComponentListener cl) {
            //do nothing
        }

        /**
         * Accessor so NodeRenderer can check if the tree table or its child has
         * focus and paint with the appropriate color.
         *
         * @see NodeRenderer#configureFrom
         * @return The tree table
         */
        TreeTable getTreeTable() {
            return TreeTable.this;
        }

        /**
         * Sets the row height of the tree, and forwards the row height to
         * the table.
         */
        @Override
        public void setRowHeight(int rowHeight) {
            if (rowHeight > 0) {
                super.setRowHeight(rowHeight);
                TreeTable.this.setRowHeight(rowHeight);
            }
        }

        /**
         * Overridden to always set the size to the height of the TreeTable
         * and the width of column 0.  The paint() method will translate the
         * coordinates to the correct position. 
         * Fire width property change so that we can revalidate horizontal scrollbar in TreeTableView.
         */
        @Override
        public void setBounds(int x, int y, int w, int h) {
            transY = -y;
            int oldW = getWidth();
            super.setBounds(0, 0, TreeTable.this.getColumnModel().getColumn(0).getWidth(), TreeTable.this.getHeight());
            if (oldW != w) {
                firePropertyChange("width", oldW, w);
            }
        }

        @Override
        public void paint(Graphics g) {
            g.translate(-getPositionX(), transY);
            super.paint(g);
        }

        @Override
        public Rectangle getVisibleRect() {
            Rectangle visibleRect = TreeTable.this.getVisibleRect();
            visibleRect.x = positionX;
            visibleRect.width = TreeTable.this.getColumnModel().getColumn(getTreeColumnIndex()).getWidth();

            return visibleRect;
        }

        /* Overriden to use this call for moving tree renderer.
         */
        @Override
        public void scrollRectToVisible(Rectangle aRect) {
            Rectangle rect = getVisibleRect();
            rect.y = aRect.y;
            rect.height = aRect.height;

            TreeTable.this.scrollRectToVisible(rect);

            int x = rect.x;

            if (aRect.width > rect.width) {
                x = aRect.x;
            } else if (aRect.x < rect.x) {
                x = aRect.x;
            } else if ((aRect.x + aRect.width) > (rect.x + rect.width)) {
                x = (aRect.x + aRect.width) - rect.width;
            }

            TreeTable.this.setPositionX(x);
        }

        @Override
        public String getToolTipText(MouseEvent event) {
            if (event != null) {
                Point p = event.getPoint();
                p.translate(positionX, visibleRow * getRowHeight());

                int selRow = getRowForLocation(p.x, p.y);

                if (selRow != -1) {
                    TreePath path = getPathForRow(selRow);
                    VisualizerNode v = (VisualizerNode) path.getLastPathComponent();
                    String tooltip = v.getShortDescription();
                    String displayName = v.getDisplayName();

                    if ((tooltip != null) && !tooltip.equals(displayName)) {
                        return tooltip;
                    }
                }
            }

            return null;
        }

        /**
         * TreeCellRenderer method. Overridden to update the visible row.
         */
        public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column
        ) {
            if (isSelected) {
                Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();

                boolean tableHasFocus = (focusOwner == this) || (focusOwner == TreeTable.this) ||
                    TreeTable.this.isAncestorOf(focusOwner) || focusOwner instanceof JRootPane; //RootPane == popup menu

                setBackground(tableHasFocus ? table.getSelectionBackground() : getUnfocusedSelectedBackground());
                setForeground(tableHasFocus ? table.getSelectionForeground() : getUnfocusedSelectedForeground());
            } else {
                setBackground(table.getBackground());
                setForeground(table.getForeground());
            }

            visibleRow = row;

            return this;
        }

        @Override
        protected TreeModelListener createTreeModelListener() {
            return new JTree.TreeModelHandler() {
                    @Override
                    public void treeNodesRemoved(TreeModelEvent e) {
                        if (tree.getSelectionCount() == 0) {
                            TreePath path = TreeView.findSiblingTreePath(e.getTreePath(), e.getChildIndices());

                            if ((path != null) && (path.getPathCount() > 0)) {
                                tree.setSelectionPath(path);
                            }
                        }
                    }
                };
        }

        @Override
        public void fireTreeCollapsed(TreePath path) {
            super.fireTreeCollapsed(path);
            firePropertyChange("width", -1, getWidth());
        }

        @Override
        public void fireTreeExpanded(TreePath path) {
            super.fireTreeExpanded(path);
            firePropertyChange("width", -1, getWidth());
        }

        boolean treeTableHasFocus() {
            Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
            boolean tableHasFocus = (focusOwner == this) || (focusOwner == TreeTable.this) ||
                TreeTable.this.isAncestorOf(focusOwner) || focusOwner instanceof JRootPane; //RootPane == popup menu
            return tableHasFocus;
        }
    }

    /**
     * TreeTableCellEditor implementation.
     */
    class TreeTableCellEditor extends DefaultCellEditor implements TreeSelectionListener, ActionListener, FocusListener,
        CellEditorListener {
        /** Used in editing. Indicates x position to place editingComponent. */
        protected transient int offset;

        /** Used before starting the editing session. */
        protected transient Timer timer;

        public TreeTableCellEditor() {
            super(new TreeTableTextField());

            tree.addTreeSelectionListener(this);
            addCellEditorListener(this);
            super.getComponent().addFocusListener(this);
        }

        /**
         * Overridden to determine an offset that tree would place the
         * editor at. The offset is determined from the
         * <code>getRowBounds</code> JTree method, and additionally
         * from the icon DefaultTreeCellRenderer will use.
         * <p>The offset is then set on the TreeTableTextField component
         * created in the constructor, and returned.
         */
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int r, int c) {
            Component component = super.getTableCellEditorComponent(table, value, isSelected, r, c);

            determineOffset(value, isSelected, r);
            ((TreeTableTextField) getComponent()).offset = offset;

            return component;
        }

        /**
         * This is overridden to forward the event to the tree and start editor timer.
         */
        @Override
        public boolean isCellEditable(EventObject e) {
            if (lastRow != -1) {
                TreePath tp = tree.getPathForRow(lastRow);
                org.openide.nodes.Node n = tp != null ? Visualizer.findNode(tp.getLastPathComponent()) : null;

                if ((n == null) || !n.canRename()) {
                    //return false;
                    canEdit = false;
                }
            }

            if (canEdit && (e != null) && (e.getSource() instanceof Timer)) {
                return true;
            }

            if (canEdit && shouldStartEditingTimer(e)) {
                startEditingTimer();
            } else if (shouldStopEditingTimer(e)) {
                timer.stop();
            }

            if (e instanceof MouseEvent) {
                MouseEvent me = (MouseEvent) e;
                int column = getTreeColumnIndex();

                if (SwingUtilities.isLeftMouseButton(me) && (me.getClickCount() == 2)) {
                    TreePath path = tree.getPathForRow(TreeTable.this.rowAtPoint(me.getPoint()));
                    Rectangle r = tree.getPathBounds(path);

                    if ((me.getX() < (r.x - positionX)) || (me.getX() > (r.x - positionX + r.width))) {
                        me.translatePoint(r.x - me.getX(), 0);
                    }
                }

                MouseEvent newME = new MouseEvent(
                        TreeTable.this.tree, me.getID(), me.getWhen(), me.getModifiers()+me.getModifiersEx(),
                        me.getX() - getCellRect(0, column, true).x + positionX, me.getY(), me.getClickCount(),
                        me.isPopupTrigger()
                    );
                TreeTable.this.tree.dispatchEvent(newME);
            }

            return false;
        }

        /* Stop timer when selection has been changed.
         */
        public void valueChanged(TreeSelectionEvent e) {
            if (timer != null) {
                timer.stop();
            }
        }

        /* Timer performer.
         */
        public void actionPerformed(java.awt.event.ActionEvent e) {
            if (lastRow != -1) {
                editCellAt(lastRow, getTreeColumnIndex(), new EventObject(timer));
            }
        }

        /* Start editing timer only on certain conditions.
         */
        private boolean shouldStartEditingTimer(EventObject event) {
            if ((event instanceof MouseEvent) && SwingUtilities.isLeftMouseButton((MouseEvent) event)) {
                MouseEvent me = (MouseEvent) event;

                return ((me.getID() == MouseEvent.MOUSE_PRESSED) && (me.getClickCount() == 1) && inHitRegion(me));
            }

            return false;
        }

        /* Stop editing timer only on certain conditions.
         */
        private boolean shouldStopEditingTimer(EventObject event) {
            if (timer == null) {
                return false;
            }

            if (event instanceof MouseEvent) {
                MouseEvent me = (MouseEvent) event;

                return (!SwingUtilities.isLeftMouseButton(me) || (me.getClickCount() > 1));
            }

            return false;
        }

        /**
         * Starts the editing timer.
         */
        private void startEditingTimer() {
            if (timer == null) {
                timer = new Timer(1200, this);
                timer.setRepeats(false);
            }

            timer.start();
        }

        /* Does a click go into node's label?
         */
        private boolean inHitRegion(MouseEvent me) {
            determineOffset(me);

            if (me.getX() <= offset) {
                return false;
            }

            return true;
        }

        /* Determines offset of node's label from left edge of the table.
         */
        private void determineOffset(MouseEvent me) {
            int row = TreeTable.this.rowAtPoint(me.getPoint());

            if (row == -1) {
                offset = 0;

                return;
            }

            determineOffset(tree.getPathForRow(row).getLastPathComponent(), TreeTable.this.isRowSelected(row), row);
        }

        /* Determines offset of node's label from left edge of the table.
         */
        private void determineOffset(Object value, boolean isSelected, int row) {
            JTree t = getTree();
            boolean rv = t.isRootVisible();
            int offsetRow = row;

            if (!rv && (row > 0)) {
                offsetRow--;
            }

            Rectangle bounds = t.getRowBounds(offsetRow);
            offset = bounds.x;

            TreeCellRenderer tcr = t.getCellRenderer();
            Object node = t.getPathForRow(offsetRow).getLastPathComponent();
            Component comp = tcr.getTreeCellRendererComponent(
                    t, node, isSelected, t.isExpanded(offsetRow), t.getModel().isLeaf(node), offsetRow, false
                );

            if (comp instanceof JLabel) {
                Icon icon = ((JLabel) comp).getIcon();

                if (icon != null) {
                    offset += (((JLabel) comp).getIconTextGap() + icon.getIconWidth());
                }
            }

            offset -= positionX;
        }

        /* Revalidates text field upon change of x position of renderer
         */
        private void revalidateTextField() {
            int row = TreeTable.this.editingRow;

            if (row == -1) {
                offset = 0;

                return;
            }

            determineOffset(tree.getPathForRow(row).getLastPathComponent(), TreeTable.this.isRowSelected(row), row);
            ((TreeTableTextField) super.getComponent()).offset = offset;
            getComponent().setBounds(TreeTable.this.getCellRect(row, getTreeColumnIndex(), false));
        }

        // Focus listener

        /* Cancel editing when text field loses focus
         */
        public void focusLost(java.awt.event.FocusEvent evt) {
            /* to allow Escape functionality
            if (!stopCellEditing())
              cancelCellEditing();
             */
        }

        /* Select a text in text field when it gets focus.
         */
        public void focusGained(java.awt.event.FocusEvent evt) {
            ((TreeTableTextField) super.getComponent()).selectAll();
        }

        // Cell editor listener - copied from TreeViewCellEditor

        /** Implements <code>CellEditorListener</code> interface method. */
        public void editingStopped(ChangeEvent e) {
            TreePath lastP = tree.getPathForRow(lastRow);

            if (lastP != null) {
                Node n = Visualizer.findNode(lastP.getLastPathComponent());

                if ((n != null) && n.canRename()) {
                    String newStr = (String) getCellEditorValue();
                    ViewUtil.nodeRename(n, newStr);
                }
            }
        }

        /** Implements <code>CellEditorListener</code> interface method. */
        public void editingCanceled(ChangeEvent e) {
        }
    }

    /**
     * Component used by TreeTableCellEditor. The only thing this does
     * is to override the <code>reshape</code> method, and to ALWAYS
     * make the x location be <code>offset</code>.
     */
    static class TreeTableTextField extends JTextField {
        public int offset;

        @Override
        public void setBounds(int x, int y, int w, int h) {
            int newX = Math.max(x, offset);
            super.setBounds(newX, y, w - (newX - x), h);
        }

    }
    /**
     * ListToTreeSelectionModelWrapper extends DefaultTreeSelectionModel
     * to listen for changes in the ListSelectionModel it maintains. Once
     * a change in the ListSelectionModel happens, the paths are updated
     * in the DefaultTreeSelectionModel.
     */
    class ListToTreeSelectionModelWrapper extends DefaultTreeSelectionModel {
        /** Set to true when we are updating the ListSelectionModel. */
        protected boolean updatingListSelectionModel;

        public ListToTreeSelectionModelWrapper() {
            super();
            listSelectionModel = new TreeTableSelectionModel();
            getListSelectionModel().addListSelectionListener(createListSelectionListener());
        }

        /**
         * Returns the list selection model. ListToTreeSelectionModelWrapper
         * listens for changes to this model and updates the selected paths
         * accordingly.
         */
        ListSelectionModel getListSelectionModel() {
            return listSelectionModel;
        }

        /**
         * This is overridden to set <code>updatingListSelectionModel</code>
         * and message super. This is the only place DefaultTreeSelectionModel
         * alters the ListSelectionModel.
         */
        @Override
        public void resetRowSelection() {
            if (!updatingListSelectionModel) {
                updatingListSelectionModel = true;

                try {
                    super.resetRowSelection();
                } finally {
                    updatingListSelectionModel = false;
                }
            }

            // Notice how we don't message super if
            // updatingListSelectionModel is true. If
            // updatingListSelectionModel is true, it implies the
            // ListSelectionModel has already been updated and the
            // paths are the only thing that needs to be updated.
        }

        /**
         * Creates and returns an instance of ListSelectionHandler.
         */
        protected ListSelectionListener createListSelectionListener() {
            return new ListSelectionHandler();
        }

        /**
         * If <code>updatingListSelectionModel</code> is false, this will
         * reset the selected paths from the selected rows in the list
         * selection model.
         */
        protected void updateSelectedPathsFromSelectedRows() {
            if (!updatingListSelectionModel) {
                updatingListSelectionModel = true;

                try {
                    int min = listSelectionModel.getMinSelectionIndex();
                    int max = listSelectionModel.getMaxSelectionIndex();

                    if ((min == 0) && (max == getRowCount())) {
                        //#48242 - optimize the case of select all
                        int[] rows = new int[max];

                        for (int i = 0; i < rows.length; i++) {
                            rows[i] = i;
                        }

                        tree.setSelectionRows(rows);
                    } else {
                        List<Integer> list = new ArrayList<Integer>(11);

                        for (int i = min; i <= max; i++) {
                            if (listSelectionModel.isSelectedIndex(i)) {
                                list.add(Integer.valueOf(i));
                            }
                        }

                        if (list.isEmpty()) {
                            clearSelection();
                        } else {
                            int[] rows = (int[]) Utilities.toPrimitiveArray(list.toArray(new Integer[0])
                                );
                            tree.setSelectionRows(rows);
                        }
                    }
                } finally {
                    updatingListSelectionModel = false;
                }
            }
        }

        /**
         * Class responsible for calling updateSelectedPathsFromSelectedRows
         * when the selection of the list changes.
         */
        class ListSelectionHandler implements ListSelectionListener {
            public void valueChanged(ListSelectionEvent e) {
                if( inSelectAll || e.getValueIsAdjusting() ) {
                    return;
                }
                
                updateSelectedPathsFromSelectedRows();
            }
        }
    }

    /**
     * #63287 - in JDK1.6 the JTable makes additional changes to the ListSelectionModel
     * when clearing row selection. These events must be ignored the same way as
     * the overridden method TreeTable.clearSelection()
     */
    class TreeTableSelectionModel extends DefaultListSelectionModel {
        @Override
        public void setAnchorSelectionIndex(int anchorIndex) {
            if( ignoreClearSelection )
                return;
            super.setAnchorSelectionIndex(anchorIndex);
        }

        @Override
        public void setLeadSelectionIndex(int leadIndex) {
            if( ignoreClearSelection )
                return;
            super.setLeadSelectionIndex(leadIndex);
        }
    }
    
    /* This is overriden to handle mouse events especially. E.g. do not change selection
     * when it was clicked on tree's expand/collapse toggles.
     */
    class TreeTableUI extends BasicTableUI {
        /**
         * Creates the mouse listener for the JTable.
         */
        @Override
        protected MouseInputListener createMouseInputListener() {
            return new TreeTableMouseInputHandler();
        }

        public class TreeTableMouseInputHandler extends MouseInputHandler {
            // Component recieving mouse events during editing. May not be editorComponent.
            private Component dispatchComponent;

            //  The Table's mouse listener methods.
            @Override
            public void mouseClicked(MouseEvent e) {
                processMouseEvent(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                processMouseEvent(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (shouldIgnore(e)) {
                    return;
                }

                repostEvent(e);
                dispatchComponent = null;
                setValueIsAdjusting(false);

                if (!TreeTable.this.isEditing()) {
                    processMouseEvent(e);
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                return;
            }

            private void setDispatchComponent(MouseEvent e) {
                Component editorComponent = table.getEditorComponent();
                Point p = e.getPoint();
                Point p2 = SwingUtilities.convertPoint(table, p, editorComponent);
                dispatchComponent = SwingUtilities.getDeepestComponentAt(editorComponent, p2.x, p2.y);
            }

            private boolean repostEvent(MouseEvent e) {
                if (dispatchComponent == null) {
                    return false;
                }

                MouseEvent e2 = SwingUtilities.convertMouseEvent(table, e, dispatchComponent);
                dispatchComponent.dispatchEvent(e2);

                return true;
            }

            private void setValueIsAdjusting(boolean flag) {
                table.getSelectionModel().setValueIsAdjusting(flag);
                table.getColumnModel().getSelectionModel().setValueIsAdjusting(flag);
            }

            private boolean shouldIgnore(MouseEvent e) {
                return !table.isEnabled() ||
                ((e.getButton() == MouseEvent.BUTTON3) && (e.getClickCount() == 1) && !e.isPopupTrigger());
            }

            private boolean isTreeColumn(int column) {
                return TreeTable.this.getColumnClass(column) == TreeTableModelAdapter.class;
            }

            /** Forwards mouse events to a renderer (tree).
             */
            private void processMouseEvent(MouseEvent e) {
                if (shouldIgnore(e)) {
                    return;
                }

                Point p = e.getPoint();
                int row = table.rowAtPoint(p);
                int column = table.columnAtPoint(p);

                // The autoscroller can generate drag events outside the Table's range. 
                if ((column == -1) || (row == -1)) {
                    return;
                }

                // for automatic jemmy testing purposes
                if ((getEditingColumn() == column) && (getEditingRow() == row)) {
                    return;
                }

                boolean changeSelection = true;

                if (isTreeColumn(column)) {
                    TreePath path = tree.getPathForRow(TreeTable.this.rowAtPoint(e.getPoint()));
                    Rectangle r = tree.getPathBounds(path);
 
                    if ((e.getX() >= (r.x - positionX)) && (e.getX() <= (r.x - positionX + r.width))
                        || isLocationInExpandControl( path, p )
                        || e.getID() == MouseEvent.MOUSE_RELEASED || e.getID() == MouseEvent.MOUSE_CLICKED ) {
                        changeSelection = false;
                    }
                }

                if (table.getSelectionModel().isSelectedIndex(row) && e.isPopupTrigger()) {
                    return;
                }

                if (table.editCellAt(row, column, e)) {
                    setDispatchComponent(e);
                    repostEvent(e);
                }

                if (e.getID() == MouseEvent.MOUSE_PRESSED) {
                    table.requestFocus();
                }

                CellEditor editor = table.getCellEditor();

                if (changeSelection && ((editor == null) || editor.shouldSelectCell(e))) {
                    setValueIsAdjusting(true);
                    table.changeSelection(row, column, 
                            Utilities.isMac() ? e.isMetaDown() : e.isControlDown(), //use META key on Mac to toggle selection
                            e.isShiftDown());
                    setValueIsAdjusting(false);
                }
            }
            
            private boolean isLocationInExpandControl( TreePath path, Point location ) {
                if( tree.getModel().isLeaf( path.getLastPathComponent() ) )
                    return false;
                
                Rectangle r = tree.getPathBounds(path);
                int boxWidth = 8;
                Insets i = tree.getInsets();
                int indent = 0;
                
                if( tree.getUI() instanceof BasicTreeUI ) {
                    BasicTreeUI ui = (BasicTreeUI)tree.getUI();
                    if( null != ui.getExpandedIcon() )
                        boxWidth = ui.getExpandedIcon().getIconWidth();
                    
                    indent = ui.getLeftChildIndent();
                }
                int boxX;
                if( tree.getComponentOrientation().isLeftToRight() ) {
                    boxX = r.x - positionX - indent - boxWidth;
                } else {
                    boxX = r.x - positionX + indent + r.width;
                }
                return location.getX() >= boxX && location.getX() <= (boxX + boxWidth);
             }
        }
    }

    /* When selected column is tree column then call tree's action otherwise call table's.
     */
    class TreeTableAction extends AbstractAction {
        Action treeAction;
        Action tableAction;

        TreeTableAction(Action treeAction, Action tableAction) {
            this.treeAction = treeAction;
            this.tableAction = tableAction;
        }

        public void actionPerformed(ActionEvent e) {
            if (TreeTable.this.getSelectedColumn() == getTreeColumnIndex()) {
                //Issue 40075, on JDK 1.5, BasicTreeUI remarkably expects
                //that action events performed on trees actually come from
                //trees
                e.setSource(getTree());
                treeAction.actionPerformed(e);
            } else {
                tableAction.actionPerformed(e);
            }
        }
    }

    /** Focus transfer policy that retains focus after closing an editor.
     * Copied wholesale from org.openide.explorer.propertysheet.SheetTable */
    private class STPolicy extends ContainerOrderFocusTraversalPolicy {
        @Override
        public Component getComponentAfter(Container focusCycleRoot, Component aComponent) {
            if (inRemoveRequest) {
                return TreeTable.this;
            } else {
                Component result = super.getComponentAfter(focusCycleRoot, aComponent);

                return result;
            }
        }

        @Override
        public Component getComponentBefore(Container focusCycleRoot, Component aComponent) {
            if (inRemoveRequest) {
                return TreeTable.this;
            } else {
                return super.getComponentBefore(focusCycleRoot, aComponent);
            }
        }

        @Override
        public Component getFirstComponent(Container focusCycleRoot) {
            if (!inRemoveRequest && isEditing()) {
                return editorComp;
            } else {
                return TreeTable.this;
            }
        }

        @Override
        public Component getDefaultComponent(Container focusCycleRoot) {
            if (inRemoveRequest && isEditing() && editorComp.isShowing()) {
                return editorComp;
            } else {
                return TreeTable.this;
            }
        }

        @Override
        protected boolean accept(Component aComponent) {
            //Do not allow focus to go to a child of the editor we're using if
            //we are in the process of removing the editor
            if (isEditing() && inEditRequest) {
                return isKnownComponent(aComponent);
            }

            return super.accept(aComponent) && aComponent.isShowing();
        }
    }

    /** Enables tab keys to navigate between rows but also exit the table
     * to the next focusable component in either direction */
    private final class NavigationAction extends AbstractAction {
        private boolean direction;

        public NavigationAction(boolean direction) {
            this.direction = direction;
        }

        public void actionPerformed(ActionEvent e) {
            if (isEditing()) {
                removeEditor();
            }

            int targetRow;
            int targetColumn;

            if (direction) {
                if (getSelectedColumn() == (getColumnCount() - 1)) {
                    targetColumn = 0;
                    targetRow = getSelectedRow() + 1;
                } else {
                    targetColumn = getSelectedColumn() + 1;
                    targetRow = getSelectedRow();
                }
            } else {
                if (getSelectedColumn() <= 0) {
                    targetColumn = getColumnCount() - 1;
                    targetRow = getSelectedRow() - 1;
                } else {
                    targetRow = getSelectedRow();
                    targetColumn = getSelectedColumn() - 1;
                }
            }

            //if we're off the end, try to find a sibling component to pass
            //focus to
            if ((targetRow >= getRowCount()) || (targetRow < 0)) {
                //This code is a bit ugly, but works
                Container ancestor = getFocusCycleRootAncestor();

                //Find the next component in our parent's focus cycle
                Component sibling = direction
                    ? ancestor.getFocusTraversalPolicy().getComponentAfter(ancestor, TreeTable.this.getParent())
                    : ancestor.getFocusTraversalPolicy().getComponentBefore(ancestor, TreeTable.this);

                //Often LayoutFocusTranferPolicy will return ourselves if we're
                //the last.  First try to find a parent focus cycle root that
                //will be a little more polite
                if (sibling == TreeTable.this) {
                    Container grandcestor = ancestor.getFocusCycleRootAncestor();

                    if (grandcestor != null) {
                        sibling = direction
                            ? grandcestor.getFocusTraversalPolicy().getComponentAfter(grandcestor, ancestor)
                            : grandcestor.getFocusTraversalPolicy().getComponentBefore(grandcestor, ancestor);
                        ancestor = grandcestor;
                    }
                }

                //Okay, we still ended up with ourselves, or there is only one focus
                //cycle root ancestor.  Try to find the first component according to
                //the policy
                if (sibling == TreeTable.this) {
                    if (ancestor.getFocusTraversalPolicy().getFirstComponent(ancestor) != null) {
                        sibling = ancestor.getFocusTraversalPolicy().getFirstComponent(ancestor);
                    }
                }

                //If we're *still* getting ourselves, find the default button and punt
                if (sibling == TreeTable.this) {
                    JRootPane rp = getRootPane();
                    JButton jb = rp.getDefaultButton();

                    if (jb != null) {
                        sibling = jb;
                    }
                }

                //See if it's us, or something we know about, and if so, just
                //loop around to the top or bottom row - there's noplace
                //interesting for focus to go to
                if (sibling != null) {
                    if (sibling == TreeTable.this) {
                        //set the selection if there's nothing else to do
                        changeSelection(
                            direction ? 0 : (getRowCount() - 1), direction ? 0 : (getColumnCount() - 1), false, false
                        );
                    } else {
                        //Request focus on the sibling
                        sibling.requestFocus();
                    }

                    return;
                }
            }

            changeSelection(targetRow, targetColumn, false, false);
        }
    }

    /** Used to explicitly invoke editing from the keyboard */
    private class EditAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            int row = getSelectedRow();
            int col = getSelectedColumn();

            if (col == 0) {
                col = 1;
            }

            editCellAt(row, col, null);
        }

        @Override
        public boolean isEnabled() {
            return (getSelectedRow() != -1) && (getSelectedColumn() != -1) && !isEditing() && getSelectedColumn() != getTreeColumnIndex();
        }
    }

    /** Either cancels an edit, or closes the enclosing dialog if present */
    private class CancelEditAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            if (isEditing() || (editorComp != null)) {
                removeEditor();

                return;
            } else {
                Component c = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();

                InputMap imp = getRootPane().getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
                ActionMap am = getRootPane().getActionMap();

                KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
                Object key = imp.get(escape);

                if (key == null) {
                    //Default for NbDialog
                    key = "Cancel";
                }

                if (key != null) {
                    Action a = am.get(key);

                    if (a != null) {
                        String commandKey = (String) a.getValue(Action.ACTION_COMMAND_KEY);

                        if (commandKey == null) {
                            commandKey = key.toString();
                        }

                        a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, commandKey)); //NOI18N
                    }
                }
            }
        }

        @Override
        public boolean isEnabled() {
            return isEditing();
        }
    }

    private class EnterAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            JRootPane jrp = getRootPane();

            if (jrp != null) {
                JButton b = getRootPane().getDefaultButton();

                if ((b != null) && b.isEnabled()) {
                    b.doClick();
                }
            }
        }

        @Override
        public boolean isEnabled() {
            return !isEditing() && !inRemoveRequest;
        }
    }

    private class CTRLTabAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            setFocusCycleRoot(false);

            try {
                Container con = TreeTable.this.getFocusCycleRootAncestor();

                if (con != null) {
                    Component target = TreeTable.this;

                    if (getParent() instanceof JViewport) {
                        target = getParent().getParent();

                        if (target == con) {
                            target = TreeTable.this;
                        }
                    }

                    EventObject eo = EventQueue.getCurrentEvent();
                    boolean backward = false;

                    if (eo instanceof KeyEvent) {
                        backward = ((((KeyEvent) eo).getModifiers() & KeyEvent.SHIFT_MASK) != 0) &&
                            ((((KeyEvent) eo).getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) != 0);
                    }

                    Component to = backward ? con.getFocusTraversalPolicy().getComponentAfter(con, TreeTable.this)
                                            : con.getFocusTraversalPolicy().getComponentAfter(con, TreeTable.this);

                    if (to == TreeTable.this) {
                        to = backward ? con.getFocusTraversalPolicy().getFirstComponent(con)
                                      : con.getFocusTraversalPolicy().getLastComponent(con);
                    }

                    to.requestFocus();
                }
            } finally {
                setFocusCycleRoot(true);
            }
        }
    }
}
