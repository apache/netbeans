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

package org.openide.explorer.propertysheet;

import java.awt.AWTKeyStroke;
import java.awt.event.ComponentEvent;
import org.openide.util.NbBundle;
import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Event;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.util.Collections;
import java.util.EventObject;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.plaf.TableUI;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;
import org.openide.util.ChangeSupport;

/** A base class for property-sheet style tables.  This class handles all of
 * the non-property specific behaviors of the property sheet.  It is not
 * intended for subclassing except by SheetTable - it exists mainly to keep
 * orthagonal code separate and maintainable, and was factored out of the
 * original implementation of SheetTable.  Basically it provides focus
 * handling, row painting and some generic actions used by the property
 * sheet, constituting those customizations to a standard JTable which
 * the property sheet requires.
 *
 * @author  Tim Boudreau
 */
abstract class BaseTable extends JTable implements FocusListener {

    /**
    * if a property with this name is explicitly set to 'true', then quick
    * search will be disabled on all BaseTable subclasses (overriding the
    * value returned by the isQuickSearchAllowed() method).
    */
    protected static final String SYSPROP_PS_QUICK_SEARCH_DISABLED_GLOBAL = "ps.quickSearch.disabled.global"; //NOI18N

    /** Action key for the action that will move to the next row via TAB,
     * or to the next focusable component if on the last row */
    protected static final String ACTION_NEXT = "next"; //NOI18N

    /** Action key for the action that will move to the previous row via TAB,
     * or to the next focusable component if on the first row */
    protected static final String ACTION_PREV = "prev"; //NOI18N

    /** Action key for the action that will start editing the cell via the
     * keyboard */
    protected static final String ACTION_INLINE_EDITOR = "invokeInlineEditor"; //NOI18N

    /** Action key for cancelling an edit by pressing escape */
    protected static final String ACTION_CANCEL_EDIT = "cancelEditing"; //NOI18N

    /** Action key for user pressing enter when not in edit mode */
    protected static final String ACTION_ENTER = "enterPressed"; //NOI18N

    /** Action key for up/down focus action */
    protected static final String ACTION_FOCUS_NEXT = "focusNext"; //NOI18N

    /** Number of pixels on each side of the column split in which the mouse
     *  cursor should be the resize cursor and mouse events should be
     *  interpreted as initiating a drag. */
    private static final int centerLineFudgeFactor = 3;

    /** Static start-an-edit action shared by all instances */
    protected static Action editAction = null;

    /** Static cancel-an-edit action shared by all instances */
    protected static Action cancelAction = null;

    /** Action which will try to invoke the default button if the table is in
     * a dialog */
    protected static Action enterAction = null;

    /** Listener for drag events on the center line, for resizing columns when
     * there are no headers */
    protected LineDragListener dragListener;

    private final ChangeSupport cs = new ChangeSupport(this);

    /** Flag which, if true, means that the next call to paint() should trigger
     * calculating the fixed row height based on the font size */
    boolean needCalcRowHeight = true;

    /** Flag used by addFocusListener to block the UI delegate from adding
     * a focus listener (which will repaint the table incorrectly) */
    private boolean inSetUI = false;

    /** keeps track of whether 'quick search' feature is enabled for this property sheet */
    private boolean allowQuickSearch = true;

    //Variables used by subclasses to track when edit requests start/end,
    //to determine when it's appropriate to repaint.  A sort of reference
    //counting.
    private int editRequests = 0;
    private int editorRemoveRequests = 0;
    private int editorChangeRequests = 0;
    private boolean searchArmed = false;
    private transient SearchField searchField = null;
    private transient JPanel searchpanel = null;
    private transient ChangeListener viewportListener;
    private transient Point prevViewPosition = null;

    /** Creates a new instance of BaseTable. */
    public BaseTable(TableModel dm, TableColumnModel cm, ListSelectionModel sm) {
        super(dm, cm, sm);

        //set single selection mode
        getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        setSurrendersFocusOnKeystroke(true);

        setCellSelectionEnabled(false);
        setRowSelectionAllowed(true);
        setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);

        //See the sources for JTable for what these do
        putClientProperty("JTable.autoStartsEdit", Boolean.FALSE); //NOI18N
        putClientProperty("terminateEditOnFocusLost", PropUtils.psCommitOnFocusLoss ? Boolean.FALSE : Boolean.TRUE); //NOI18N

        //create a listener for dragging the grid center line to resize columns
        dragListener = new LineDragListener();
        addMouseListener(dragListener);
        addMouseMotionListener(dragListener);

        //If we are not focus cycle root, when an editor is removed, focus
        //will get set to a random component which is usually not the property
        //sheet
        setFocusCycleRoot(true);

        enableEvents(AWTEvent.FOCUS_EVENT_MASK); //JDK 1.5 

        if (getClass() != SheetTable.class) {
            throw new NoClassDefFoundError("Only SheetTable may subclass BaseTable, for good reasons"); //NOI18N
        }
    }

    /** Initialize keystrokes and actions */
    protected void initKeysAndActions() {
        //Kill off the focus traversal keys.  NavigationAction will find the
        //next/previous components if the keyboard moves the position beyond
        //the ends of the table, and manage the focus thus.
        setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.<AWTKeyStroke>emptySet());
        setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, Collections.<AWTKeyStroke>emptySet());

        //Next two lines do not work using inputmap/actionmap, but do work
        //using the older API.  We will process ENTER to skip to next row,
        //not next cell
        unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
        unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, Event.SHIFT_MASK));

        InputMap imp = getInputMap();
        ActionMap am = getActionMap();

        if (!GraphicsEnvironment.isHeadless()) {
        //Issue 37919, reinstate support for up/down cycle focus transfer.
        //being focus cycle root mangles this in some dialogs
        imp.put(
            KeyStroke.getKeyStroke(
                KeyEvent.VK_TAB, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.SHIFT_MASK, false
            ), ACTION_FOCUS_NEXT
        );
        imp.put(
            KeyStroke.getKeyStroke(KeyEvent.VK_TAB, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false),
            ACTION_FOCUS_NEXT
        );
        }

        Action ctrlTab = new CTRLTabAction();
        am.put(ACTION_FOCUS_NEXT, ctrlTab);

        imp.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), ACTION_NEXT);

        imp.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK), ACTION_PREV);

        am.put(ACTION_NEXT, new NavigationAction(true));
        am.put(ACTION_PREV, new NavigationAction(false));

        imp.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), ACTION_INLINE_EDITOR);
        imp.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), ACTION_INLINE_EDITOR);
        am.put(ACTION_INLINE_EDITOR, getEditAction());

        imp.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), ACTION_ENTER);
        am.put(ACTION_ENTER, getEnterAction());

        InputMap impAncestor = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        impAncestor.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), ACTION_CANCEL_EDIT);
        am.put(ACTION_CANCEL_EDIT, new CancelAction());

        impAncestor.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), ACTION_CANCEL_EDIT);
    }

    /** Called by the sheet table finalizer */
    protected static final void cleanup() {
        editAction = null;
        cancelAction = null;
        enterAction = null;
    }

    /** Overridden to set the flag for recalculating the fixed row height */
    @Override
    public void setFont(Font f) {
        needCalcRowHeight = true;
        super.setFont(f);
    }

    /** Lazily create the edit-on-spacebar action */
    private static Action getEditAction() {
        if (editAction == null) {
            editAction = new EditAction();
        }

        return editAction;
    }

    /** Lazily create the cancel-on-escape action */

    /*private static Action getCancelAction() {
        if (cancelAction == null) {
            cancelAction = new CancelAction();
        }
        return cancelAction;
    }*/
    private static Action getEnterAction() {
        if (enterAction == null) {
            enterAction = new EnterAction();
        }

        return enterAction;
    }

    /** Calculate the height of rows based on the current font.  This is
     *  done when the first paint occurs, to ensure that a valid Graphics
     *  object is available.  */
    private void calcRowHeight(Graphics g) {
        //Users of themes can set an explicit row height, so check for it
        Integer i = (Integer) UIManager.get(PropUtils.KEY_ROWHEIGHT); //NOI18N

        int rowHeight;

        if (i != null) {
            rowHeight = i.intValue();
        } else {
            //Derive a row height to accomodate the font and expando icon
            Font f = getFont();
            FontMetrics fm = g.getFontMetrics(f);
            rowHeight = Math.max(fm.getHeight() + 3, PropUtils.getSpinnerHeight());
        }

        //Clear the flag
        needCalcRowHeight = false;

        //Set row height.  If displayable, this will generate a new call
        //to paint()
        setRowHeight(rowHeight);
    }

    protected int getFirstVisibleRow() {
        if (getParent() instanceof JViewport) {
            JViewport jvp = (JViewport) getParent();

            return rowAtPoint(jvp.getViewPosition());
        } else {
            Insets ins = getInsets();

            return rowAtPoint(new Point(ins.left, ins.top));
        }
    }

    protected int getVisibleRowCount() {
        int rowCount = getRowCount();
        int rowHeight = getRowHeight();

        if ((rowCount == 0) || (rowHeight == 0)) {
            return 0;
        }

        if (getParent() instanceof JViewport) {
            JViewport jvp = (JViewport) getParent();

            // +1 to return also half-displayed rows (issue 53660)
            int result = Math.min(rowCount, (jvp.getExtentSize().height / rowHeight) + 1);

            return result;
        } else {
            return Math.min(rowCount, getHeight() / rowHeight);
        }
    }

    /** Overridden to not allow edits on the names column (0) */
    @Override
    public boolean isCellEditable(int row, int col) {
        return col != 0;
    }

    /** The old window system will force focus back to the table, when an editor
     * becomes visible.  This will cause the combo box to close its popup because
     * it has lost focus, unless we intervene here and make sure focus must be
     * passed directly to the editor if present */
    @Override
    public final void requestFocus() {
        if (isEditing()) {
            if (PropUtils.isLoggable(BaseTable.class)) {
                PropUtils.log(BaseTable.class, "RequestFocus on table delegating to editor component"); //NOI18N
            }

            editorComp.requestFocus();
        } else {
            if (!inEditorChangeRequest()) {
                if (PropUtils.isLoggable(BaseTable.class)) {
                    PropUtils.log(BaseTable.class, "RequestFocus on table with no editor present"); //NOI18N
                }

                super.requestFocus();
            }
        }
    }

    /** The old window system will force focus back to the table, when an editor
     * becomes visible.  This will cause the combo box to close its popup because
     * it has lost focus, unless we intervene here and make sure focus must be
     * passed directly to the editor if present */
    @Override
    public final boolean requestFocusInWindow() {
        if (isEditing()) {
            if (PropUtils.isLoggable(BaseTable.class)) {
                PropUtils.log(BaseTable.class, "RequestFocusInWindow on table delegating to editor"); //NOI18N
            }

            return editorComp.requestFocusInWindow();
        } else {
            if (!inEditorChangeRequest()) {
                if (PropUtils.isLoggable(BaseTable.class)) {
                    PropUtils.log(BaseTable.class, "RequestFocusInWindow on table with no editor present"); //NOI18N
                }

                boolean result = super.requestFocusInWindow();

                if (PropUtils.isLoggable(BaseTable.class)) {
                    PropUtils.log(BaseTable.class, "  RequestFocusInWindow result " + result); //NOI18N
                }

                return result;
            } else {
                return false;
            }
        }
    }

    /** Overridden to remove the editor before editing, so a new edit
     * can be started in a single click, to set the selection before editing,
     * so that the editor will be painted with the selection color, and
     * to request focus on the editor component */
    @Override
    public boolean editCellAt(int row, int col, EventObject e) {
        enterEditRequest();

        if (e instanceof MouseEvent) {
            if (PropUtils.isLoggable(BaseTable.class)) {
                PropUtils.log(BaseTable.class, "editCellAt " + row + "," + col + " triggered by mouse event"); //NOI18N
            }

            //Ensure that the we end up being the focus owner.  In the case
            //of the radio button editor, focus can remain with the previous
            //focus owner
            Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();

            if (focusOwner != this) {
                if (!requestFocusInWindow()) {
                    requestFocus();
                }
            }
        } else {
            if (PropUtils.isLoggable(BaseTable.class)) {
                PropUtils.log(BaseTable.class, "editCellAt " + row + "," + col + " triggered by (null = kbd evt)" + e); //NOI18N
            }
        }

        boolean wasEditing = isEditing();

        //Cancel any current edit.  By default, if you click a cell in
        //a JTable while another cell is being edited, it will change
        //the selection and stop the edit, but it will not initiate a
        //new edit.  So we need to be sure that we are not editing by
        //the time super.editCellAt is called
        if (wasEditing) {
            if (PropUtils.isLoggable(BaseTable.class)) {
                PropUtils.log(BaseTable.class, "  was already editing, removing the editor"); //NOI18N
            }

            removeEditor();
        }

        //Update the selection first - we want to change this now, so the
        //row will be painted correctly, rather than when
        //TableCellEditor.shouldSelectCell is called
        int prevSel = getSelectedRow();
        changeSelection(row, col, false, false);

        boolean result = false;

        //Set a flag - we'll want to behave slightly differently in terms
        //of repaints if we're going from editing -> editing - there's no
        //need for an update to reflect a non-editing state
        final boolean editorChange = wasEditing && isCellEditable(row, col);

        if (editorChange) {
            enterEditorChangeRequest();
        }

        try {
            //Do the super call to really start the edit
            result = super.editCellAt(row, col, e);

            if (PropUtils.isLoggable(BaseTable.class)) {
                PropUtils.log(BaseTable.class, "  Result of super.editCellAt is " + result); //NOI18N
            }

            //For the sake of the radio button editor, these paints really
            //need to be done synchronously - the editor will be added,
            //painted, handed a mouse event, process it, fire an event and
            //be removed before the next event on the event queue gets handled
            //            paintRow(prevSel);
            //            paintSelectionRow();
            //JTable will not set focus to the editor by default, so we
            //need to or it will never get focus when invoked by the
            //keyboard
            if (editorComp != null) {
                Component c = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();

                //Add ourselves as a focus listener to the component
                editorComp.addFocusListener(this);
            }
        } finally {
            //Reset the flags no matter what happened
            try {
                //in its own try-catch in case of assertion failure
                exitEditRequest();
            } finally {
                if (editorChange) {
                    exitEditorChangeRequest();
                }
            }
        }

        return result;
    }

    /** Called when an edit request is received, to indicate that some
     * repaints should be blocked while previous editors are removed,
     * selection is changed, etc. */
    protected final void enterEditRequest() {
        editRequests++;

        if (PropUtils.isLoggable(BaseTable.class)) {
            PropUtils.log(BaseTable.class, " entering edit request"); //NOI18N
        }
    }

    protected final void enterEditorRemoveRequest() {
        editorRemoveRequests++;

        if (PropUtils.isLoggable(BaseTable.class)) {
            PropUtils.log(BaseTable.class, " entering editor remove request"); //NOI18N
        }
    }

    protected final void enterEditorChangeRequest() {
        editorChangeRequests++;

        if (PropUtils.isLoggable(BaseTable.class)) {
            PropUtils.log(BaseTable.class, " entering editor change request"); //NOI18N
        }
    }

    protected final void exitEditRequest() {
        editRequests--;

        if (PropUtils.isLoggable(BaseTable.class)) {
            PropUtils.log(BaseTable.class, " exiting edit change request"); //NOI18N
        }

        assert editRequests >= 0;
    }

    protected final void exitEditorRemoveRequest() {
        editorRemoveRequests--;
        PropUtils.log(BaseTable.class, " exiting editor remove request"); //NOI18N
        assert editorRemoveRequests >= 0;
    }

    protected final void exitEditorChangeRequest() {
        editorChangeRequests--;

        if (PropUtils.isLoggable(BaseTable.class)) {
            PropUtils.log(BaseTable.class, " exiting editor change request"); //NOI18N
        }

        assert editorRemoveRequests >= 0;
    }

    protected final boolean inEditRequest() {
        return editRequests > 0;
    }

    protected final boolean inEditorChangeRequest() {
        return editorChangeRequests > 0;
    }

    protected final boolean inEditorRemoveRequest() {
        return editorRemoveRequests > 0;
    }

    /** Overridden to set the colors apropriately - we always want the editor
     * to appear selected */
    @Override
    public Component prepareEditor(TableCellEditor editor, int row, int col) {
        Component result = editor.getTableCellEditorComponent(this, getValueAt(row, col), false, row, col);

        if (result != null) {
            result.setBackground(getSelectionBackground());
            result.setForeground(getSelectionForeground());
            result.setFont(getFont());
        }

        return result;
    }

    /** Overridden to hide the selection when not focused, and paint across the
     * selected row if focused. */
    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
        Object value = getValueAt(row, col);

        Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();

        boolean isSelected = isSelected(row, focusOwner);

        Component result = renderer.getTableCellRendererComponent(this, value, isSelected, false, row, col);

        if( PropUtils.isNimbus ) {
            //HACK to get rid of alternate row background colors
            if( !isSelected ) {
                Color bkColor = getBackground();
                if( null != bkColor ) 
                    result.setBackground( new Color( bkColor.getRGB() ) );
            }
        }

        return result;
    }

    /** Determines if the row should be painted as if it were selected.  This
     * is overridden by SheetTable to also check if the focused component is
     * known to the current inplace editor, if any */
    protected boolean isSelected(int row, Component focusOwner) {
        return ((getSelectedRow() == row) || ((editingRow == row) && !inEditorRemoveRequest())) &&
        (hasFocus() || isKnownComponent(focusOwner) || inEditRequest());
    }

    @Override
    public void setUI(TableUI ui) {
        needCalcRowHeight = true;
        inSetUI = true;
        super.setUI(ui);
        inSetUI = false;
    }

    /** Overridden to not allow the UI to install a focus listener.  Reason:
     * This focus listener is installed to repaint on focus loss, but it will
     * only repaint the selected *cell*.  Since we don't differentiate selecting
     * only a cell, we need to repaint the entire row that is selected, which
     * we will do from processFocusEvent() */
    @Override
    public void addFocusListener(FocusListener fl) {
        if (!inSetUI) {
            super.addFocusListener(fl);
        }
    }

    @Override
    public void updateUI() {
        super.updateUI();

        //Initialize keys and actions after updateUI; the UI will overwrite
        //arrow key actions if this is done in the constructor
        initKeysAndActions();
    }

    /** Paint the table.  After the super.paint() call, calls paintMargin() to fill
     *  in the left edge with the appropriate color, and then calls paintExpandableSets()
     *  to paint the property sets, which are not painted by the default painting
     *  methods because they need to be painted across two rows.    */
    @Override
    public void paint(Graphics g) {
        if (needCalcRowHeight) {
            calcRowHeight(g);

            return;
        }

        super.paint(g);
    }

    protected void paintRow(int row) {
        if (row == -1) {
            return;
        }

        Rectangle dirtyRect = getCellRect(row, 0, false);
        dirtyRect.x = 0;
        dirtyRect.width = getWidth();
        repaint(dirtyRect);
    }

    /** Our own painting code for the selection row - normally the UI delegate
     * would do this, but we specifically block it from adding a focus listener
     * and do it ourselves, since when focus changes, we need to repaint both
     * rows, not just the selected cell.  */
    protected void paintSelectionRow() {
        paintRow(getSelectedRow());
    }

    /** Overridden to add the entire row that was being edited to RepaintManager
     * as a dirty region */
    @Override
    public void removeEditor() {
        enterEditorRemoveRequest();

        try {
            int i = editingRow;

            if (editorComp != null) {
                editorComp.removeFocusListener(this);
            }

            if (PropUtils.isLoggable(BaseTable.class)) {
                PropUtils.log(BaseTable.class, " removing editor"); //NOI18N
            }

            super.removeEditor();

            if (i != -1) {
                //Do schedule a repaint for the row just in case
                paintRow(i);
            }
        } finally {
            exitEditorRemoveRequest();
        }
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

                scrollPane.setColumnHeaderView(getTableHeader());
            }
        }
    }

    /** Returns true if the passed X axis pixel position is within the
     *  bounds where a drag can be initiated to resize columns */
    protected final boolean onCenterLine(int pos) {
        int line = getColumnModel().getColumn(0).getWidth();

        return (pos > (line - centerLineFudgeFactor)) && (pos < (line + centerLineFudgeFactor));
    }

    /** Returns true if the passed event occured within the
     *  bounds where a drag can be initiated to resize columns */
    protected final boolean onCenterLine(MouseEvent me) {
        int pos = me.getPoint().x;

        return (onCenterLine(pos));
    }

    /** Overridden to not change the selection if the user is currently
     * dragging the center line */
    @Override
    public void changeSelection(int row, int column, boolean toggle, boolean extend) {
        //DragListener can be null, because changeSelection is called in
        //superclass constructor
        if ((dragListener != null) && dragListener.isArmed()) {
            return;
        }

        if (PropUtils.isLoggable(BaseTable.class)) {
            PropUtils.log(BaseTable.class, "ChangeSelection to " + row + "," + column); //NOI18N
        }

        super.changeSelection(row, column, toggle, extend);
        fireChange();
    }

    /** This method exists to support experimental support for commit-on-focus-loss
     * if NetBeans is started with a specific line switch - SheetTable overrides
     * this method to stop cell editing if the flag is true. */
    protected void focusLostCancel() {
        removeEditor();
    }

    /** Overridden to remove the editor on focus lost */
    @Override
    public void processFocusEvent(FocusEvent fe) {
        super.processFocusEvent(fe);

        if (PropUtils.isLoggable(BaseTable.class)) {
            PropUtils.log(BaseTable.class, "processFocusEvent - "); //NOI18N
            PropUtils.log(BaseTable.class, fe);
        }

        if (!isAncestorOf(fe.getOppositeComponent()) || (fe.getOppositeComponent() == null)) {
            if (isEditing() && (fe.getID() == FocusEvent.FOCUS_LOST)) {
                if (PropUtils.isLoggable(BaseTable.class)) {
                    PropUtils.log(
                        BaseTable.class, "ProcessFocusEvent got focus lost to unknown component, removing editor"
                    ); //NOI18N
                }

                focusLostCancel();
            }
        }

        if (!inEditorRemoveRequest() && !inEditRequest()) { //XXX inEditRequest probably shouldn't be here

            if ((fe.getOppositeComponent() == null) && (fe.getID() == FocusEvent.FOCUS_LOST)) {
                //ignore the strange focus to null stuff NetBeans does
                return;
            }

            paintSelectionRow();
        } else {
            paintSelectionRow();
        }
    }

    /**
     * @return true if the quick search feature is currently allowed, false otherwise.
     * To be allowed, quick search must not have been disabled either globally or on
     * this instance
     * @since 6.37
     */
    protected boolean isQuickSearchAllowed() {
        // the system property can disable quick search on all instances
        String sysPropGlobalDisable = System.getProperty(SYSPROP_PS_QUICK_SEARCH_DISABLED_GLOBAL, "false");
        if (Boolean.parseBoolean(sysPropGlobalDisable)) {
            return false;
        }

        return allowQuickSearch;
    }

    /**
     * Allows one to set whether or not the quick search feature should be
     * enabled on this instance.
     *
     * @param isQuickSearchAllowed true if the quick search feature should be
     * allowed on this instance, false otherwise.
     * @since 6.37
     */
    protected void setQuickSearchAllowed(boolean isQuickSearchAllowed) {
        this.allowQuickSearch = isQuickSearchAllowed;
    }

    /** Overridden to allow standard keybinding processing of VK_TAB and
     * abort any pending drag operation on the vertical grid. */
    @Override
    public void processKeyEvent(KeyEvent e) {
        if (dragListener.isArmed()) {
            dragListener.setArmed(false);
        }

        boolean suppressDefaultHandling = ((searchField != null) && searchField.isShowing()) &&
            ((e.getKeyCode() == KeyEvent.VK_UP) || (e.getKeyCode() == KeyEvent.VK_DOWN));

        //Manually hook in the bindings for tab - does not seem to get called
        //automatically
        if (e.getKeyCode() != KeyEvent.VK_TAB) {
            if (!suppressDefaultHandling) {
                //Either the search field or the table should handle up/down, not both
                super.processKeyEvent(e);
            }

            if (!e.isConsumed()) {
                if ((e.getID() == KeyEvent.KEY_PRESSED) && !isEditing()) {
                    int modifiers = e.getModifiers();
                    int keyCode = e.getKeyCode();

                    if (((modifiers > 0) && (modifiers != KeyEvent.SHIFT_MASK)) || e.isActionKey()) {
                        return;
                    }

                    char c = e.getKeyChar();

                    if (!Character.isISOControl(c) && (keyCode != KeyEvent.VK_SHIFT) &&
                            (keyCode != KeyEvent.VK_ESCAPE)) {
                        searchArmed = true;
                        e.consume();
                    }
                } else if (searchArmed && (e.getID() == KeyEvent.KEY_TYPED)) {
                    passToSearchField(e);
                    e.consume();
                    searchArmed = false;
                } else {
                    searchArmed = false;
                }
            }
        } else {
            processKeyBinding(
                KeyStroke.getKeyStroke(e.VK_TAB, e.getModifiersEx(), e.getID() == e.KEY_RELEASED), e,
                JComponent.WHEN_FOCUSED, e.getID() == e.KEY_PRESSED
            );
        }
    }

    void passToSearchField(KeyEvent e) {
        if (! isQuickSearchAllowed()) {
            return;
        }

        //Don't do anything for normal navigation keys
        if (
            (e.getKeyCode() == KeyEvent.VK_TAB) || (e.getKeyCode() == KeyEvent.VK_ENTER) ||
                (((e.getKeyCode() == KeyEvent.VK_UP) || (e.getKeyCode() == KeyEvent.VK_DOWN)) &&
                ((searchField == null) || !searchField.isShowing()))
        ) {
            return;
        }

        if (getRowCount() == 0) {
            return;
        }

        if ((searchField == null) || !searchField.isShowing()) {
            showSearchField();
            searchField.setText(String.valueOf(e.getKeyChar()));
        }
    }

    private void showSearchField() {
        if (searchField == null) {
            searchField = new SearchField();
            searchpanel = new JPanel();

            JLabel lbl = new JLabel(NbBundle.getMessage(BaseTable.class, "LBL_QUICKSEARCH")); //NOI18N
            searchpanel.setLayout(new BoxLayout(searchpanel, BoxLayout.X_AXIS));
            searchpanel.add(lbl);
            searchpanel.add(searchField);
            lbl.setLabelFor(searchField);
            searchpanel.setBorder(BorderFactory.createRaisedBevelBorder());
            lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        }

        JComponent dest = getRootPane().getLayeredPane();

        Point loc;

        if (getParent() instanceof JViewport) {
            JViewport jvp = (JViewport) getParent();
            loc = jvp.getViewPosition();
            loc.x += getColumnModel().getColumn(0).getWidth();
            //#68516 repaint the table when scrolling
            viewportListener = new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    if( null != searchField && searchField.isVisible() ) {
                        if( null != prevViewPosition )
                            repaint( 0, prevViewPosition.y, getWidth(), searchpanel.getHeight() );
                        assert getParent() instanceof JViewport;
                        prevViewPosition = new Point( ((JViewport)getParent()).getViewPosition() );
                    }
                }
            };
            jvp.addChangeListener( viewportListener );
            prevViewPosition = new Point( loc );
        } else {
            loc = new Point(getColumnModel().getColumn(0).getWidth(), getRowHeight() / 2);
        }

        loc = SwingUtilities.convertPoint(this, loc, dest);

        int width = getColumnModel().getColumn(1).getWidth();
        int height = getRowHeight() + 5;

        if (width < 120) {
            //too narrow
            width = 160;
            loc.x -= 160;
        }

        searchpanel.setBounds(loc.x, loc.y, width, height);
        dest.add(searchpanel);
        getParent().addComponentListener( searchField );
        searchpanel.setVisible(true);
        searchField.requestFocus();
    }

    private void hideSearchField() {
        if (searchField == null) {
            return;
        }

        searchpanel.setVisible(false);

        if (getParent() instanceof JViewport && null != viewportListener ) {
            JViewport jvp = (JViewport) getParent();
            jvp.removeChangeListener( viewportListener );
            viewportListener = null;
        }
        
        getParent().removeComponentListener(searchField);
        if (searchpanel.getParent() != null) {
            searchpanel.getParent().remove(searchpanel);
        }

        paintSelectionRow();
    }

    /** Called to determine if the search field text matches an object
     * from column 0 (the passed object value).  Subclasses should override
     * to check specific info - the default implementation simply compares
     * value.toString().startsWith(text) */
    protected boolean matchText(Object value, String text) {
        if (value != null) {
            return value.toString().startsWith(text);
        } else {
            return false;
        }
    }

    @Override
    public boolean isOptimizedDrawingEnabled() {
        if ((searchField != null) && searchField.isShowing()) {
            return false;
        } else {
            return super.isOptimizedDrawingEnabled();
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        //Issue 41546 - bad repaint when scrolling
        if ((searchField != null) && searchField.isVisible()) {
            searchpanel.repaint();
        }
    }

    /** Overridden to fire a change event on a change in the table, so the
     * property sheet can refresh the displayed description if necessary */
    @Override
    public void tableChanged(TableModelEvent e) {
        super.tableChanged(e);
        fireChange();
    }

    //****************Change listener support ***************

    /** Registers ChangeListener to receive events.
     * @param listener The listener to register.  */
    public final void addChangeListener(ChangeListener listener) {
        cs.addChangeListener(listener);
    }

    /** Removes ChangeListener from the list of listeners.
     * @param listener The listener to remove. */
    public final void removeChangeListener(ChangeListener listener) {
        cs.removeChangeListener(listener);
    }

    /** Notifies all registered listeners about the event.
     */
    void fireChange() {
        if (cs != null) {
            cs.fireChange();
        } // else in constructor
    }

    //****************************** Focus listener implementation ************
    protected boolean isKnownComponent(Component c) {
        if (c == null) {
            return false;
        }

        if (c == this) {
            return true;
        }

        if (c == editorComp) {
            return true;
        }

        if (c == searchField) {
            return true;
        }

        if (c == this.getRootPane()) {
            return true;
        }

        if (c instanceof Container && ((Container) c).isAncestorOf(this)) {
            return true;
        }

        if ((editorComp instanceof Container) && ((Container) editorComp).isAncestorOf(c)) {
            return true;
        }

        return false;
    }

    public void focusGained(FocusEvent fe) {
        Component c = fe.getOppositeComponent();

        /*
        //handy for debugging
        System.out.println("Focus gained to " + (fe.getComponent().getName() == null ? fe.getComponent().getClass().getName() : fe.getComponent().getName()) + " temporary: " + fe.isTemporary()
        + " from " + (fe.getOppositeComponent() == null ? "null" :
            (fe.getOppositeComponent().getName() == null ? fe.getOppositeComponent().getClass().getName() : fe.getOppositeComponent().getName()))
        );
         */
        PropUtils.log(BaseTable.class, fe);

        if (!isKnownComponent(c)) {
            fireChange();
        }

        if (!inEditRequest() && !inEditorRemoveRequest() && (fe.getComponent() == this)) {
            //            System.out.println("Painting due to focus gain " + fe.getComponent());
            //            repaint(0,0,getWidth(),getHeight());
            paintSelectionRow();
        }
    }

    //Focus listener implementation
    public void focusLost(FocusEvent fe) {
        if ((dragListener != null) && dragListener.isDragging()) {
            dragListener.abortDrag();
        }

        PropUtils.log(BaseTable.class, fe);

        //Ignore temporary focus changes, so sloppy focus middle mouse button
        //cut/paste can work
        if (fe.isTemporary()) {
            return;
        }

        Component opposite = fe.getOppositeComponent();

        if (!isKnownComponent(opposite)) {
            doFocusLost(opposite);
        }
    }

    private void doFocusLost(Component opposite) {
        // TerminateEditOnFocusLost does not always work, ensure it
        PropUtils.log(BaseTable.class, " removing editor due to focus change"); //NOI18N

        if (PropUtils.psCommitOnFocusLoss && isEditing()) { // && (source instanceof InplaceEditor)) {
            getCellEditor().stopCellEditing();
        } else {
            removeEditor();
        }

        // fire a change if focus did not go to null, so the property sheet will
        // display the node name, not the selected property
        if (opposite != null) {
            fireChange();
        }

        paintSelectionRow();
    }

    WL parentListener;
    @Override
    public void addNotify() {
        super.addNotify();

        // #57560: properties should always save changes
        Container top = getTopLevelAncestor();

        if (top instanceof Window) {
            ((Window) top).addWindowListener(parentListener = new WL());
        }
    }
    
    private class WL extends WindowAdapter {
        @Override
        public void windowDeactivated(java.awt.event.WindowEvent we) {
            doFocusLost(we.getOppositeWindow());
        }
    }

    @Override
    public void removeNotify() {
        // #57560: properties should always save changes
        Container top = getTopLevelAncestor();

        if (top instanceof Window && parentListener != null) {
            ((Window) top).removeWindowListener(parentListener);
            parentListener = null;
        }
        super.removeNotify();
    }

    private class SearchField extends JTextField 
            implements ActionListener, FocusListener, ComponentListener {
        private int selectionBeforeLastShow = -1;

        public SearchField() {
            addActionListener(this);
            addFocusListener(this);
            setFont(BaseTable.this.getFont());
        }

        @Override
        public void addNotify() {
            super.addNotify();
            selectionBeforeLastShow = BaseTable.this.getSelectedRow();
        }

        @Override
        public void processKeyEvent(KeyEvent ke) {
            if (!isShowing()) {
                super.processKeyEvent(ke);

                return;
            }

            //override the default handling so that
            //the parent will never receive the escape key and
            //close a modal dialog
            if (ke.getKeyCode() == ke.VK_ESCAPE) {
                //The focus request will hide the field without focus getting
                //lost to somewhere else in the main window first.
                BaseTable.this.changeSelection(selectionBeforeLastShow, 0, false, false);
                BaseTable.this.requestFocus();
                ke.consume();
            } else if ((ke.getKeyCode() == ke.VK_UP) && (ke.getID() == ke.KEY_PRESSED)) {
                reverseSearch(getText());
            } else if ((ke.getKeyCode() == ke.VK_DOWN) && (ke.getID() == ke.KEY_PRESSED)) {
                forwardSearch(getText());
            } else {
                super.processKeyEvent(ke);

                if ((ke.getKeyCode() != ke.VK_UP) && (ke.getKeyCode() != ke.VK_DOWN)) {
                    processSearchText(getText());
                }
            }
        }

        public void keyPressed(KeyEvent ke) {
            if (ke.getKeyCode() == ke.VK_ESCAPE) {
                hideSearchField();
                ke.consume();
            }
        }

        public void keyReleased(KeyEvent ke) {
            processSearchText(((JTextField) ke.getSource()).getText());
        }

        public void actionPerformed(ActionEvent e) {
            processSearchText(((JTextField) e.getSource()).getText());

            //Use the focus request to hide the field, otherwise focus will
            //be sent to Explorer or some random component
            BaseTable.this.requestFocus();
        }

        public void focusGained(FocusEvent e) {
            //it will be the first focus gained event, so go select
            //whatever matches the first character
            processSearchText(((JTextField) e.getSource()).getText());

            JRootPane root = getRootPane();

            if (root != null) { // #57417 NPE
                root.getLayeredPane().repaint();
            }
            setCaretPosition(getText().length());
        }

        public void focusLost(FocusEvent e) {
            hideSearchField();
        }

        private void processSearchText(String txt) {
            if ((txt == null) || (txt.length() == 0)) {
                return;
            }

            int max = getRowCount();
            int pos = getSelectedRow();

            if ((pos == (max - 1)) || (pos < 0)) {
                pos = 0;
            }

            for (int i = 0; i < max; i++) {
                boolean match = matchText(BaseTable.this.getValueAt(i, 0), txt);

                if (match) {
                    changeSelection(i, 0, false, false);

                    //Set renderers can overpaint whole field's panel, so repaint
                    getRootPane().getLayeredPane().repaint();

                    break;
                }

                if (pos++ == (max - 1)) {
                    pos = 0;
                }
            }
        }

        private void forwardSearch(String txt) {
            if ((txt == null) || (txt.length() == 0)) {
                return;
            }

            int max = getRowCount();
            int pos = getSelectedRow() + 1;

            if ((pos == (max - 1)) || (pos < 0)) {
                pos = 0;
            }

            for (int i = pos; i < max; i++) {
                boolean match = matchText(BaseTable.this.getValueAt(i, 0), txt);

                if (match) {
                    changeSelection(i, 0, false, false);

                    //Set renderers can overpaint it, so repaint
                    repaint();

                    break;
                }
            }
        }

        private void reverseSearch(String txt) {
            if ((txt == null) || (txt.length() == 0)) {
                return;
            }

            int max = getRowCount();
            int pos = getSelectedRow();

            if (pos < 1) {
                pos = max - 1;
            }

            for (int i = pos - 1; i >= 0; i--) {
                boolean match = matchText(BaseTable.this.getValueAt(i, 0), txt);

                if (match) {
                    changeSelection(i, 0, false, false);

                    //Set renderers can overpaint it, so repaint
                    repaint();

                    break;
                }
            }
        }

        public void componentResized(ComponentEvent e) {
            hideSearchField();
        }

        public void componentMoved(ComponentEvent e) {
            hideSearchField();
        }

        public void componentShown(ComponentEvent e) {
        }

        public void componentHidden(ComponentEvent e) {
            hideSearchField();
        }
    }

    /** Action to edit via the keyboard */
    private static class EditAction extends AbstractAction {
        public void actionPerformed(ActionEvent ae) {
            JTable jt = (JTable) ae.getSource();
            int row = jt.getSelectedRow();
            int col = jt.getSelectedColumn();

            if ((row != -1) && (col != -1)) {
                if (PropUtils.isLoggable(BaseTable.class)) {
                    PropUtils.log(BaseTable.class, "Starting edit due to key event for row " + row); //NOI18N
                }

                jt.editCellAt(row, 1, null);

                //Focus will be rerouted to the editor via this call:
                jt.requestFocus();
            }
        }
    }

    /** Action to cancel an inline editor */
    private class CancelAction extends AbstractAction {
        public void actionPerformed(ActionEvent ae) {
            JTable jt = (JTable) ae.getSource();

            if (jt != null) {
                if (jt.isEditing()) {
                    TableCellEditor tce = jt.getCellEditor();

                    if (PropUtils.isLoggable(BaseTable.class)) {
                        PropUtils.log(BaseTable.class, "Cancelling edit due to keyboard event"); //NOI18N
                    }

                    if (tce != null) {
                        jt.getCellEditor().cancelCellEditing();
                    }
                } else {
                    //If we're in a dialog, try to close it
                    trySendEscToDialog(jt);
                }
            }
        }

        @Override
        public boolean isEnabled() {
            return isEditing();
        }

        private void trySendEscToDialog(JTable jt) {
            //        System.err.println("SendEscToDialog");
            EventObject ev = EventQueue.getCurrentEvent();

            if (ev instanceof KeyEvent && (((KeyEvent) ev).getKeyCode() == KeyEvent.VK_ESCAPE)) {
                if (ev.getSource() instanceof JComboBox && ((JComboBox) ev.getSource()).isPopupVisible()) {
                    return;
                }

                if (
                    ev.getSource() instanceof JTextComponent &&
                        ((JTextComponent) ev.getSource()).getParent() instanceof JComboBox &&
                        ((JComboBox) ((JTextComponent) ev.getSource()).getParent()).isPopupVisible()
                ) {
                    return;
                }

                InputMap imp = jt.getRootPane().getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
                ActionMap am = jt.getRootPane().getActionMap();

                KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
                Object key = imp.get(escape);

                if (key != null) {
                    Action a = am.get(key);

                    if (a != null) {
                        if (Boolean.getBoolean("netbeans.proppanel.logDialogActions")) { //NOI18N
                            System.err.println("Action bound to escape key is " + a); //NOI18N
                        }

                        //Actions registered with deprecated registerKeyboardAction will
                        //need this lookup of the action command
                        String commandKey = (String) a.getValue(Action.ACTION_COMMAND_KEY);

                        if (commandKey == null) {
                            commandKey = "cancel"; //NOI18N
                        }

                        a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, commandKey)); //NOI18N
                    }
                }
            }
        }
    }

    private static class EnterAction extends AbstractAction {
        public void actionPerformed(ActionEvent ae) {
            if (ae.getSource() instanceof BaseTable) {
                BaseTable bt = (BaseTable) ae.getSource();

                if (bt.isEditing()) {
                    return;
                }

                trySendEnterToDialog(bt);
            }
        }

        private void trySendEnterToDialog(BaseTable bt) {
            //        System.err.println("SendEnterToDialog");
            EventObject ev = EventQueue.getCurrentEvent();

            if (ev instanceof KeyEvent && (((KeyEvent) ev).getKeyCode() == KeyEvent.VK_ENTER)) {
                if (ev.getSource() instanceof JComboBox && ((JComboBox) ev.getSource()).isPopupVisible()) {
                    return;
                }

                if (
                    ev.getSource() instanceof JTextComponent &&
                        ((JTextComponent) ev.getSource()).getParent() instanceof JComboBox &&
                        ((JComboBox) ((JTextComponent) ev.getSource()).getParent()).isPopupVisible()
                ) {
                    return;
                }

                JRootPane jrp = bt.getRootPane();

                if (jrp != null) {
                    JButton b = jrp.getDefaultButton();

                    if ((b != null) && b.isEnabled()) {
                        b.doClick();
                    }
                }
            }
        }
    }

    /** Enables tab keys to navigate between rows */
    private final class NavigationAction extends AbstractAction {
        private boolean direction;

        public NavigationAction(boolean direction) {
            this.direction = direction;
        }

        public void actionPerformed(ActionEvent e) {
            int next = getSelectedRow() + (direction ? 1 : (-1));

            //if we're off the end, try to find a sibling component to pass
            //focus to
            if ((next >= getRowCount()) || (next < 0)) {
                if (!(BaseTable.this.getTopLevelAncestor() instanceof Dialog)) {
                    //If we're not in a dialog, we're in the main window - don't
                    //send focus somewhere because the winsys won't change the
                    //active mode
                    next = (next >= getRowCount()) ? 0 : (getRowCount() - 1);
                } else if ((next >= getRowCount()) || (next < 0)) {
                    //if we're off the end, try to find a sibling component to pass
                    //focus to
                    //This code is a bit ugly, but works
                    Container ancestor = getFocusCycleRootAncestor();

                    //Find the next component in our parent's focus cycle
                    Component sibling = direction
                        ? ancestor.getFocusTraversalPolicy().getComponentAfter(ancestor, BaseTable.this.getParent())
                        : ancestor.getFocusTraversalPolicy().getComponentBefore(ancestor, BaseTable.this);

                    //Often LayoutFocusTranferPolicy will return ourselves if we're
                    //the last.  First try to find a parent focus cycle root that
                    //will be a little more polite
                    if (sibling == BaseTable.this) {
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
                    if (sibling == BaseTable.this) {
                        if (ancestor.getFocusTraversalPolicy().getFirstComponent(ancestor) != null) {
                            sibling = ancestor.getFocusTraversalPolicy().getFirstComponent(ancestor);
                        }
                    }

                    //If we're *still* getting ourselves, find the default button and punt
                    if (sibling == BaseTable.this) {
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
                        if (sibling == BaseTable.this) {
                            //set the selection if there's nothing else to do
                            changeSelection(
                                direction ? 0 : (getRowCount() - 1), direction ? 0 : (getColumnCount() - 1), false,
                                false
                            );
                        } else {
                            //Request focus on the sibling
                            sibling.requestFocus();
                        }

                        return;
                    }
                }

                changeSelection(next, getSelectedColumn(), false, false);
            }

            if( getSelectionModel().getAnchorSelectionIndex() < 0 )
                getSelectionModel().setAnchorSelectionIndex(next);
            getSelectionModel().setLeadSelectionIndex(next);
        }
    }

    /** Listener for drag events that should resize columns */
    final class LineDragListener extends MouseAdapter implements MouseMotionListener {
        private long dragStartTime = -1;
        boolean armed;
        boolean dragging;
        int pos = -1;

        @Override
        public void mouseExited(MouseEvent e) {
            setArmed(false);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (isArmed() && onCenterLine(e)) {
                beginDrag();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (isDragging()) {
                finishDrag();
                setArmed(false);
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            setArmed(!isEditing() && onCenterLine(e));
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (!armed && !dragging) {
                return;
            }

            int newPos = e.getPoint().x;
            TableColumn c0 = getColumnModel().getColumn(0);
            TableColumn c1 = getColumnModel().getColumn(1);
            int min = Math.max(c0.getMinWidth(), getWidth() - c1.getMaxWidth());
            int max = Math.min(c0.getMaxWidth(), getWidth() - c1.getMinWidth());

            if ((newPos >= min) && (newPos <= max)) {
                pos = newPos;
                update();
            }
        }

        public boolean isArmed() {
            return armed;
        }

        public boolean isDragging() {
            return dragging;
        }

        public void setArmed(boolean val) {
            if (val != armed) {
                this.armed = val;

                if (armed) {
                    BaseTable.this.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
                } else {
                    BaseTable.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        }

        private void beginDrag() {
            dragging = true;
            dragStartTime = System.currentTimeMillis();
        }

        public void abortDrag() {
            dragging = false;
            setArmed(false);
            repaint();
        }

        private void finishDrag() {
            dragging = false;

            if ((System.currentTimeMillis() - dragStartTime) < 400) {
                update();
            } else {
                abortDrag();
            }
        }

        private void update() {
            if ((pos < 0) || (pos > getWidth())) {
                repaint();

                return;
            }

            int pos0 = pos;
            int pos1 = getWidth() - pos;

            synchronized (getTreeLock()) {
                getColumnModel().getColumn(0).setWidth(pos0);
                getColumnModel().getColumn(1).setWidth(pos1);
                getColumnModel().getColumn(0).setPreferredWidth(pos0);
                getColumnModel().getColumn(1).setPreferredWidth(pos1);
            }

            BaseTable.this.repaint();
        }
    }

    private class CTRLTabAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            setFocusCycleRoot(false);

            try {
                Container con = BaseTable.this.getFocusCycleRootAncestor();

                if (con != null) {
                    Component target = BaseTable.this;

                    if (getParent() instanceof JViewport) {
                        target = getParent().getParent();

                        if (target == con) {
                            target = BaseTable.this;
                        }
                    }

                    EventObject eo = EventQueue.getCurrentEvent();
                    boolean backward = false;

                    if (eo instanceof KeyEvent) {
                        backward = ((((KeyEvent) eo).getModifiers() & KeyEvent.SHIFT_MASK) != 0) &&
                            ((((KeyEvent) eo).getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) != 0);
                    }

                    Component to = backward ? con.getFocusTraversalPolicy().getComponentAfter(con, BaseTable.this)
                                            : con.getFocusTraversalPolicy().getComponentAfter(con, BaseTable.this);

                    if (to == BaseTable.this) {
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
