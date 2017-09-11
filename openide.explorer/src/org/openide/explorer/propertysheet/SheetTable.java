/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.openide.explorer.propertysheet;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.ContainerOrderFocusTraversalPolicy;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.FeatureDescriptor;
import java.beans.PropertyEditor;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.util.EventObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListSelectionModel;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.plaf.TableUI;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import org.openide.awt.HtmlRenderer;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.nodes.Node.PropertySet;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * A JTable subclass that displays node properties.  To set the properties,
 * call <code>getPropertySetModel().setPropertySets()</code>.  This class
 * uses instance counts to track shared resources.  Do NOT un-final this class.
 * <p>
 * This class implements only property-specific functionality; the row-selection
 * painting logic, etc, are in the superclass BaseTable.
 *
 * @author Tim Boudreau
 */
final class SheetTable extends BaseTable implements PropertySetModelListener, CustomEditorAction.Invoker {
    /** Action key for right-arrow expansion of property sets */
    private static final String ACTION_EXPAND = "expandSet"; //NOI18N

    /** Action key for left-arrow closing of property sets */
    private static final String ACTION_COLLAPSE = "collapseSet"; //NOI18N

    /** Action key for invoking the custom editor */
    private static final String ACTION_CUSTOM_EDITOR = "invokeCustomEditor"; //NOI18N

    /** Action key for action to log the curent property editor class*/
    private static final String ACTION_EDCLASS = "edclass"; //NOI18N

    /** A reference count so the finalizer can release the shared editor and
     * renderer instances (which hold onto some fairly heavy GUI components
     * when no more instances are active */
    private static int instanceCount = 0;

    /** Flag to block calls to setModel, etc., after initialization */
    private transient boolean initialized = false;

    /** Field to hold last edited feature descriptor if state was stored */
    private FeatureDescriptor storedFd = null;

    /** Field to hold last editing state if state was stored */
    private boolean wasEditing = false;

    /** Field to hold partial user input if state was stored while editing */
    private Object partialValue = null;

    /** Fallback field storing the last selected row, in the case that the
     *  table changes and selection should be restored */
    private int lastSelectedRow = -1;

    /** Static sheetCellRenderer which will be shared by all instances of
     * SheetTable */
    private SheetCellRenderer renderer = null;

    /** Static sheetCellEditor which will be shared by all instances of
     * SheetTable */
    private SheetCellEditor sheetCellEditor = null;

    /** Custom editor action used to invoke the custom editor from keyboard
     * or button */
    private Action customEditorAction = null;

    /** Display name of the current node, for passing to the custom editor
     * dialog for setting the title */
    /** Action to collapse or expand a set when the user presses the left or right arrow */
    private Action expandAction;

    /** Action to collapse or expand a set when the user presses the left or right arrow */
    private Action collapseAction;

    /** For debugging, an action that prints the current selection's property editor class
     * to the standard out  */
    private Action edClassAction;

    /** Name used for the custom editor dialog, set by PropertySheet */
    private String beanName;

    /** Flag set when a custom editor is opening until it closes.  This is
     * used to shut off tooltips to avoid a Windows bug that when a tooltip
     * appears, the window containing it will be fronted, moving the modal
     * custom editor behind it. */
    private boolean customEditorIsOpen = false;
    private ReusablePropertyEnv reusableEnv = new ReusablePropertyEnv();
    private ReusablePropertyModel reusableModel = new ReusablePropertyModel(reusableEnv);
    boolean lastIncludeMargin = false;
    private HtmlRenderer.Renderer htmlrenderer = null;

    //***************Implementation of issue 9691 - restore editing state after failed edit (dlg shown) *********

    /** field to keep a count of focus events - there will be two following a
     * failed edit.  EditingStopped will set this value to 2.  FocusGained will
     * decrement it.  Doing almost anything else that touches the property sheet
     * will reset it to -1.  If it is 0 when a focusGained event occurs, editing
     * will be restarted.  This is less than ideal, but the code that shows the
     * dialog will not start blocking the AWT queue until after focus has
     * returned and the editor has been removed.  We get one focusGained event as a
     * result of the inplace editor being removed;  the second is the user closing
     * the dialog.  */
    int countDown = -1;
    boolean lastFailed = false;

    /** Creates a new instance of SheetTable */
    public SheetTable() {
        super(new SheetTableModel(), new SheetColumnModel(), new DefaultListSelectionModel());
        setPropertySetModel(new PropertySetModelImpl());

        //Set a default row height
        setRowHeight(16);

        //Show grid lines if no alternating color defined
        setShowGrid(PropUtils.noAltBg());
        setShowVerticalLines(PropUtils.noAltBg());
        setShowHorizontalLines(PropUtils.noAltBg());
        setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);

        if (!PropUtils.noAltBg()) {
            setIntercellSpacing(new Dimension(0, 0));
        }

        setGridColor(PropUtils.getSetRendererColor());

        Color c = UIManager.getColor("PropSheet.selectionBackground"); //NOI18N

        if (c != null) {
            setSelectionBackground(c);
        }

        c = UIManager.getColor("PropSheet.selectionForeground"); //NOI18N

        if (c != null) {
            setSelectionForeground(c);
        }

        getAccessibleContext().setAccessibleName(NbBundle.getMessage(SheetTable.class, "ACSN_SHEET_TABLE")); //NOI18N

        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SheetTable.class, "ACSD_SHEET_TABLE")); //NOI18N

        setTransferHandler(new SheetTableTransferHandler());

        Color col = UIManager.getColor("netbeans.ps.background"); //NOI18N

        if (col != null) {
            setBackground(col);
        }

        setFocusTraversalPolicy(new STPolicy());
        instanceCount++;
    }

    //************Shared infrastructure*****************************    
    @Override
    protected void finalize() {
        instanceCount--;

        if (instanceCount == 0) {
            renderer = null;
            sheetCellEditor = null;
            cleanup();
        }
    }

    /** Fetch the static render instance shared among tables */
    SheetCellRenderer getRenderer() {
        if (renderer == null) {
            renderer = new SheetCellRenderer(true, reusableEnv, reusableModel);
        }

        return renderer;
    }

    /** Fetch the static editor instance shared among tables */
    SheetCellEditor getEditor() {
        if (sheetCellEditor == null) {
            sheetCellEditor = new SheetCellEditor(getReusablePropertyEnv());
        }

        return sheetCellEditor;
    }

    private TableCellRenderer getCustomRenderer( int row ) {
        FeatureDescriptor fd = getPropertySetModel().getFeatureDescriptor(row);

        if (fd instanceof PropertySet)
            return null;

        Object res = fd.getValue( "custom.cell.renderer"); //NOI18N
        if( res instanceof TableCellRenderer ) {
            prepareCustomEditor( res );
            return ( TableCellRenderer ) res;
        }
        return null;
    }

    private TableCellEditor getCustomEditor( int row ) {
        FeatureDescriptor fd = getPropertySetModel().getFeatureDescriptor(row);

        if (fd instanceof PropertySet)
            return null;

        Object res = fd.getValue( "custom.cell.editor"); //NOI18N
        if( res instanceof TableCellEditor ) {
            prepareCustomEditor( res );
            return ( TableCellEditor ) res;
        }
        return null;
    }

    private void prepareCustomEditor( Object customEditorObj ) {
        JComboBox comboBox = null;
        if( customEditorObj instanceof DefaultCellEditor ) {
            if( ((DefaultCellEditor)customEditorObj).getComponent() instanceof JComboBox ) {
                comboBox = ( JComboBox ) ((DefaultCellEditor)customEditorObj).getComponent();
            }
        } else if( customEditorObj instanceof JComboBox ) {
            comboBox = ( JComboBox ) customEditorObj;
        }
        if( null != comboBox ) {
            if( !(comboBox.getUI() instanceof CleanComboUI) ) {
                comboBox.setUI( new CleanComboUI( true ) );
                ComboBoxAutoCompleteSupport.install( comboBox );
            }
        }
    }

    /****************Bean getters/setters*****************************************

        /** Implement's Rochelle's suggestion of including the display name
         * of the edited bean in the custom editor dlg title.  SheetTable doesn't
         * know what node it's displaying, so property sheet code sets this
         * when it changes */
    void setBeanName(String name) {
        this.beanName = name;
    }

    /** Fetch the name of the currently displayed JavaBean */
    @Override
    public String getBeanName() {
        return beanName;
    }

    /** Returns a reference to the static editor shared among all instances
     * of SheetTable */
    @Override
    public TableCellEditor getCellEditor(int row, int column) {
        if( 0 == column ) {
            TableCellEditor res = getCustomEditor( row );
            if( null != res )
                return res;
        }

        return getEditor();
    }

    /** Returns a reference to the static renderer shared among all instances
     * of SheetTable */
    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        if( 0 == column ) {
            TableCellRenderer res = getCustomRenderer( row );
            if( null != res )
                return res;
        }
        return getRenderer();
    }

    //**********Overrides of model setters to disable changes that would break the impl******    

    /** Throws an UnsupportedOperationException when called by user code.  Replacing
     *  the data model of property sheets is unsupported.  You can change the model
     *  that determines what properties are shown - see <code>setPropertySetModel()</code>. */
    @Override
    public void setModel(TableModel model) {
        if (initialized) {
            throw new UnsupportedOperationException(
                "Changing the model of a property sheet table is not supported.  If you want to change the set of properties, ordering or other characteristings, see setPropertySetModel()."
            ); //NOI18N
        }

        super.setModel(model);
    }

    /** Throws an UnsupportedOperationException when called by user code.  Replacing
     *  the column model of property sheets is unsupported.*/
    @Override
    public void setColumnModel(TableColumnModel model) {
        if (initialized) {
            throw new UnsupportedOperationException(
                "Changing the column model of a property sheet table is not supported.  If you want to change the set of properties, ordering or other characteristings, see setPropertySetModel()."
            ); //NOI18N
        }

        super.setColumnModel(model);
    }

    /** Throws an UnsupportedOperationException when called by user code.  Replacing
     *  the selection model of property sheets not supported.*/
    @Override
    public void setSelectionModel(ListSelectionModel model) {
        if (initialized) {
            throw new UnsupportedOperationException(
                "Changing the selection model of a property sheet table is not supported.  If you want to change the set of properties, ordering or other characteristings, see setPropertySetModel()."
            ); //NOI18N
        }

        super.setSelectionModel(model);
    }

    /** Set the model which determines the ordering of properties and expansion
     *  state of embedded property sets. */
    public void setPropertySetModel(PropertySetModel psm) {
        PropertySetModel old = getSheetModel().getPropertySetModel();

        if (old == psm) {
            return;
        }

        if (old != null) {
            old.removePropertySetModelListener(this);
        }

        getSheetModel().setPropertySetModel(psm);
        psm.addPropertySetModelListener(this);
    }

    /** Convenience getter for the property set model. Delegates to the SheetModel. */
    PropertySetModel getPropertySetModel() {
        return getSheetModel().getPropertySetModel();
    }

    /** Convenience getter for the model as an instance of SheetTableModel. */
    SheetTableModel getSheetModel() {
        return (SheetTableModel) this.getModel();
    }

    /** Overridden to return null - some look and feels will want to create
     * an empty header, and we don't want them to do that */
    @Override
    public JTableHeader getTableHeader() {
        return null;
    }

    //******************Keyboard/mouse mgmt***********************************
    @Override
    protected void initKeysAndActions() {
        super.initKeysAndActions();
        unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0));
        unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0));

        expandAction = new ExpandAction();
        collapseAction = new CollapseAction();
        edClassAction = new EditorClassAction();

        InputMap imp = getInputMap();
        InputMap impAncestor = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap am = getActionMap();
        KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK);
        imp.put(ks, null);

        imp.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), ACTION_EXPAND);

        imp.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), ACTION_COLLAPSE);

        if (!GraphicsEnvironment.isHeadless()) {
        imp.put(
            KeyStroke.getKeyStroke(
                KeyEvent.VK_HOME, KeyEvent.SHIFT_DOWN_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()
            ), ACTION_EDCLASS
        );
        }

        imp.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), ACTION_NEXT);

        imp.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK), ACTION_PREV);

        impAncestor.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, KeyEvent.CTRL_DOWN_MASK), ACTION_CUSTOM_EDITOR);

        impAncestor.remove(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0));
        impAncestor.remove(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0));

        am.put(ACTION_EXPAND, expandAction);
        am.put(ACTION_COLLAPSE, collapseAction);

        am.put(ACTION_CUSTOM_EDITOR, getCustomEditorAction());
        am.put(ACTION_EDCLASS, edClassAction);

        Action defaultAction = am.get( "selectNextRow" );
        if( null != defaultAction ) {
            am.put("selectNextRow", new IncrementAction(false, defaultAction));
    }
        defaultAction = am.get( "selectPreviousRow" );
        if( null != defaultAction ) {
            am.put("selectPreviousRow", new IncrementAction(true, defaultAction));
        }
    }

    Action getCustomEditorAction() {
        if (customEditorAction == null) {
            customEditorAction = new CustomEditorAction(this);
        }

        return customEditorAction;
    }

    /** Overridden to cast value to FeatureDescriptor and return true if the
     * text matches its display name.  The popup search field uses this method
     * to check matches. */
    @Override
    protected boolean matchText(Object value, String text) {
        if (value instanceof FeatureDescriptor) {
            return ((FeatureDescriptor) value).getDisplayName().toUpperCase().startsWith(text.toUpperCase());
        } else {
            return false;
        }
    }

    //******************Painting logic **********************************    

    /** Paint the table.  After the super.paint() call, calls paintMargin() to fill
     *  in the left edge with the appropriate color, and then calls paintExpandableSets()
     *  to paint the property sets, which are not painted by the default painting
     *  methods because they need to be painted across two rows.    */
    @Override
    public void paintComponent(Graphics g) {
        boolean includeMargin = PropUtils.shouldDrawMargin(getPropertySetModel());

        getRenderer().setIncludeMargin(includeMargin);
        super.paintComponent(g);

        if (!PropUtils.noAltBg()) {
            paintCenterLine(g);
        }

        if (includeMargin) {
            paintMargin(g);
        }

        paintExpandableSets(g);

        lastIncludeMargin = includeMargin;
    }

    /** Workaround for excessive paints by SwingUtilities.paintComponent() */
    private void paintComponent(Graphics g, Component c, int x, int y, int w, int h) {
        c.setBounds(x, y, w, h);
        g.translate(x, y);
        c.paint(g);
        g.translate(-x, -y);
        c.setBounds(-w, -h, 0, 0);
    }

    /** Paints the center line in the property sheet if an alternate
     * color has been specified, so the divider is visible */
    private void paintCenterLine(Graphics g) {
        Color c = PropUtils.getAltBg();
        g.setColor(c);

        int xpos = getColumn(SheetColumnModel.NAMES_IDENTIFIER).getWidth() - 1;
        g.drawLine(xpos, 0, xpos, getHeight());
    }

    /** We only use a single listener on the selected node, PropertySheet.SheetPCListener,
     * to centralize things.  It will call this method if a property change is detected
     * so that it can be repainted. */
    void repaintProperty(String name) {
        if (!isShowing()) {
            return;
        }

        if (PropUtils.isLoggable(SheetTable.class)) {
            PropUtils.log(SheetTable.class, "RepaintProperty: " + name);
        }

        PropertySetModel psm = getPropertySetModel();
        int min = getFirstVisibleRow();

        if (min == -1) {
            return;
        }

        int max = min + getVisibleRowCount();

        for (int i = min; i < max; i++) {
            FeatureDescriptor fd = psm.getFeatureDescriptor(i);

            if (null != fd && fd.getName().equals(name)) {
                //repaint property value & name
                paintRow( i );

                return;
            }
        }

        if (PropUtils.isLoggable(SheetTable.class)) {
            PropUtils.log(SheetTable.class, "Property is either scrolled offscreen or property name is bogus: " + name);
        }
    }

    /** Paint the outside margin where the spinners for expandable
     *  sets are.  This should be derived from the standard control
     *  color.  This method will overpaint the grid lines in this
     *  area.    */
    private void paintMargin(Graphics g) {
        //Don't paint the margin for sorted modes
        //fill the outer column with the set renderer color, per UI spec
        g.setColor(PropUtils.getSetRendererColor());

        int w = PropUtils.getMarginWidth();
        int h = getHeight();

        if (g.hitClip(0, 0, w, h)) {
            g.fillRect(0, 0, w, h);
        }
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
        Component result = super.prepareRenderer(renderer, row, col);

        if ((row < 0) || (row >= getRowCount())) {
            return result;
        }

        Object value = getValueAt(row, col);

        if ((result != null) && value instanceof Property && (col == 1)) {
            result.setEnabled(((Property) value).canWrite());
        }

        return result;
    }

    /** Paint the expandable sets.  These are painted double width,
     *  across the entire width of the table. */
    private void paintExpandableSets(Graphics g) {
        int start = 0;
        int end = getRowCount();

        Insets ins = getInsets();

        boolean canBeSelected = isKnownComponent(
                KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner()
            );

        for (int i = 0; i < end; i++) {
            int idx = start + i;
            Object value = getValueAt(idx, 0);

            if (value instanceof PropertySet) {
                Rectangle r = getCellRect(idx, 0, false);
                r.x = ins.left;
                r.width = getWidth() - (ins.left + ins.right);

                if (g.hitClip(r.x, r.y, r.width, r.height)) {
                    PropertySet ps = (PropertySet) value;

                    String txt = ps.getHtmlDisplayName();
                    boolean isHtml = txt != null;

                    if (!isHtml) {
                        txt = ps.getDisplayName();
                    }

                    if (htmlrenderer == null) {
                        htmlrenderer = HtmlRenderer.createRenderer();
                    }

                    JComponent painter = (JComponent) htmlrenderer.getTableCellRendererComponent(
                            this, txt, false, false, idx, 0
                        );

                    htmlrenderer.setHtml(isHtml);
                    htmlrenderer.setParentFocused(true);

                    htmlrenderer.setIconTextGap(2);

                    htmlrenderer.setIcon(
                        getPropertySetModel().isExpanded(ps) ? PropUtils.getExpandedIcon() : PropUtils.getCollapsedIcon()
                    );

                    boolean selected = canBeSelected && (getSelectedRow() == idx);

                    if (!selected) {
                        painter.setBackground(PropUtils.getSetRendererColor());
                        painter.setForeground(PropUtils.getSetForegroundColor());
                    } else {
                        painter.setBackground(PropUtils.getSelectedSetRendererColor());
                        painter.setForeground(PropUtils.getSelectedSetForegroundColor());
                    }

                    if( PropUtils.isAqua ) {
                        painter.setOpaque(false);
                        Graphics2D g2d = (Graphics2D) g;
                        Paint oldPaint = g2d.getPaint();
                        g2d.setPaint( new GradientPaint(r.x,r.y, Color.white, r.x, r.y+r.height/2, painter.getBackground()) );
                        g2d.fillRect(r.x, r.y, r.width, r.height);
                        g2d.setPaint(oldPaint);
                    } else {
                        painter.setOpaque(true);
                    }

                    paintComponent(g, painter, r.x, r.y, r.width, r.height);
                }
            }
        }
    }

    /** Overridden to check if the edit failed, and if so, set a focus event
     * countdown for re-initiating editing */
    @Override
    public void editingStopped(ChangeEvent e) {
        super.editingStopped(e);

        //Po Ting's request for Rave - if commit on focus loss is on, all
        //edits look like failures and trigger a new call to editCellAt()
        if (!PropUtils.psCommitOnFocusLoss && !getEditor().isLastUpdateSuccessful()) {
            //The last update failed, we're two focus events away from really
            //having focus again - we'll get one, then the error dialog will
            //steal focus.  On the second one we've got focus back.
            countDown = 2;
        }
    }

    /** Initiate editing automatically - triggered by the focus event countdown */
    private void autoEdit() {
        editCellAt(getSelectedRow(), getSelectedColumn(), null);

        if (editorComp != null) {
            editorComp.requestFocus();
        }

        countDown = -1;
    }

    /** Overridden to clear the focus event countdown */
    @Override
    public void changeSelection(int row, int col, boolean a, boolean b) {
        countDown = -1;
        super.changeSelection(row, col, a, b);
    }

    /** Overridden to check the focus event countdown and initiate editing on
     * the second focus event following a failed edit (dialog was shown) */
    @Override
    public void processFocusEvent(FocusEvent fe) {
        super.processFocusEvent(fe);

        if (fe.getID() == fe.FOCUS_GAINED) {
            countDown--;

            if (countDown == 0) {
                autoEdit();
            }
        }

        if (
            (fe.getID() == fe.FOCUS_GAINED) ||
                ((fe.getOppositeComponent() != null) && (fe.getID() == fe.FOCUS_LOST) &&
                !isAncestorOf(fe.getOppositeComponent()))
        ) {
            //Ensure the description goes back to the node description if
            //we lose focus
            fireChange();
        }
    }

    @Override
    protected void focusLostCancel() {
        if (PropUtils.psCommitOnFocusLoss && isEditing()) {
            getEditor().stopCellEditing();
        } else {
            super.focusLostCancel();
        }
    }

    //**********************Miscellaneous**************************    

    /** Overridden to catch a mouse pressed event over the custom editor
     * button and invoke the custom editor even if we do not have focus;
     * otherwise, for example, in the options dialog, clicking from the
     * tree to the table over the custom editor button will just set focus
     * to the table, but will not initiate the custom editor dialog */
    @Override
    public void processMouseEvent(MouseEvent me) {
        if (me.getID() == me.MOUSE_PRESSED && SwingUtilities.isLeftMouseButton(me) ) {
            if( me.getClickCount() > 1 ) {
                //#220256 - some properties want to handle double-click on their property name cell
                int row = rowAtPoint(me.getPoint());
                int col = columnAtPoint(me.getPoint());
                if( col == 0 ) {
                    FeatureDescriptor fd = getPropertySetModel().getFeatureDescriptor( row );
                    if( null != fd ) {
                        Object mouseListener = fd.getValue( "nb.propertysheet.mouse.doubleclick.listener" ); //NOI18N
                        if( mouseListener instanceof MouseListener ) {
                            ((MouseListener)mouseListener).mouseClicked( me );
                            return;
                        }
                    }
                }
            }
            if( onCustomEditorButton(me) && !hasFocus()) {
                if (PropUtils.psCommitOnFocusLoss && isEditing()) {
                    getEditor().stopCellEditing();

                    // #54211: it can happen that PropertySheet window is closed
                    // when previous property editing is finished (e.g. Form
                    // event properties) If this is the case don't try to edit
                    // newly selected property.
                    if (isGoingToBeClosed()) {
                        return;
                    }
                }

                int row = rowAtPoint(me.getPoint());
                int col = columnAtPoint(me.getPoint());

                if ((row != -1) && (col != -1)) {
                    changeSelection(row, col, false, false);
                    getCustomEditorAction().actionPerformed(
                            new ActionEvent(this, ActionEvent.ACTION_PERFORMED, ACTION_CUSTOM_EDITOR)
                            );
                    me.consume();

                    return;
                }
            }
        }
        
        super.processMouseEvent(me);
    }

    /** Overridden to do nothing, the editor will take care of updating
     * the value */
    @Override
    public void setValueAt(Object o, int row, int column) {
        //do nothing
    }

    /** See if a component is one we know about or one the current editor
     * knows about.  This affects whether we paint as if focused or not, and
     * is used to determine what kind of focus changes mean we should stop
     * editing, and what kind are ok */
    @Override
    protected boolean isKnownComponent(Component c) {
        boolean result = super.isKnownComponent(c);

        if (result) {
            return result;
        }

        if (c == null) {
            return false;
        }

        if (c instanceof ButtonPanel) {
            return true;
        }

        InplaceEditor ie = getEditor().getInplaceEditor();

        if (ie != null) {
            JComponent comp = ie.getComponent();

            if (comp == c) {
                return true;
            }

            if (comp.isAncestorOf(c)) {
                return true;
            }
        }

        if (c.getParent() instanceof ButtonPanel) {
            return true;
        }

        if ((getParent() != null) && (getParent().isAncestorOf(c))) {
            return true;
        }

        Container par = getParent();

        if ((par != null) && par.isAncestorOf(c)) {
            return true;
        }

        if (c instanceof InplaceEditor) {
            return true;
        }

        InplaceEditor ine = getEditor().getInplaceEditor();

        if (ine != null) {
            return ine.isKnownComponent(c);
        }

        return false;
    }

    /**  Returns true if a mouse event occured over the custom editor button.
     *   This is used to supply button specific tooltips and launch the custom
     *   editor without needing to instantiate a real button */
    private boolean onCustomEditorButton(MouseEvent e) {
        //see if we're in the approximate bounds of the custom editor button
        Point pt = e.getPoint();
        int row = rowAtPoint(pt);
        int col = columnAtPoint(pt);
        FeatureDescriptor fd = getSheetModel().getPropertySetModel().getFeatureDescriptor(row);
        if( null == fd ) {
            //prevent NPE when the activated Node has been destroyed and a new one hasn't been set yet
            return false;
        }

        //see if the event happened over the custom editor button
        boolean success;

        if (PropUtils.noCustomButtons) {
            //#41412 - impossible to invoke custom editor on props w/ no inline
            //edit mode if the no custom buttons switch is set
            success = false;
        } else {
            success = e.getX() > (getWidth() - PropUtils.getCustomButtonWidth());
        }

        //if it's a mouse button event, then we're not showing a tooltip, we're
        //deciding if we should display a custom editor.  For read-only props that
        //support one, we should return true, since clicking the non-editable cell
        //is not terribly useful.
        if (
            (e.getID() == MouseEvent.MOUSE_PRESSED) || (e.getID() == MouseEvent.MOUSE_RELEASED) ||
                (e.getID() == MouseEvent.MOUSE_CLICKED)
        ) {
            //We will show the custom editor for any click on the text value
            //of a property that looks editable but sets canEditAsText to false -
            //the click means the user is trying to edit something, so to just
            //swallow the gesture is confusing
            success |= Boolean.FALSE.equals(fd.getValue("canEditAsText"));

            if (!success && fd instanceof Property) {
                PropertyEditor pe = PropUtils.getPropertyEditor((Property) fd);

                if ((pe != null) && pe.supportsCustomEditor()) {
                    //Undocumented but used in Studio - in NB 3.5 and earlier, returning null from getAsText()
                    //was a way to make a property non-editable
                    success |= (pe.isPaintable() && (pe.getAsText() == null) && (pe.getTags() == null));
                }
            }
        }

        try {
            if (success) { //NOI18N

                if (fd instanceof Property && (col == 1)) {
                    boolean supp = PropUtils.getPropertyEditor((Property) fd).supportsCustomEditor();

                    return (supp);
                }
            }
        } catch (IllegalStateException ise) {
            //See bugtraq 4941073 - if a property accessed via Reflection throws
            //an unexpected exception (try customize bean on a vanilla GenericServlet
            //to produce this) when the getter is accessed, then we are already
            //displaying "Error fetching property value" in the value area of
            //the propertysheet.  No point in distracting the user with a 
            //stack trace - it's not our bug.
            Logger.getLogger(SheetTable.class.getName()).log(Level.WARNING, null, ise);
        }

        return false;
    }

    /** Overridden to supply different tooltips depending on mouse position (name,
     *  value, custom editor button).  Will HTML-ize long tooltips*/
    @Override
    public String getToolTipText(MouseEvent e) {
        if (customEditorIsOpen) {
            return null;
        }

        String result;
        Point pt = e.getPoint();
        int row = rowAtPoint(pt);
        int col = columnAtPoint(pt);

        if ((col == 1) && onCustomEditorButton(e)) {
            result = NbBundle.getMessage(SheetTable.class, "CTL_EDBUTTON_TIP"); // NOI18N
        } else {
            result = getSheetModel().getDescriptionFor(row, col);

            if ((col == 1) && (result != null) && (result.length() > 100)) {
                //e.g. Jesse's new file list property gives massive
                //tooltips; break them up
                result = PropUtils.createHtmlTooltip(
                        getPropertySetModel().getFeatureDescriptor(row).getDisplayName(), result
                    );
            }
        }

        if ((result != null) && "".equals(result.trim())) {
            result = null; // prevents 2x2 dot as a tooltip
        }

        return result;
    }

    /** Convenience method to get the currently selected property.  Equivalent to calling
     *  <code>getSheetModel().getPropertySetModel().getFeatureDescriptor(getSelectedRow())
     *  </code>.  This method will return null if the table does not have focus or editing
     *  is not in progress.  */
    @Override
    public final FeatureDescriptor getSelection() {
        return _getSelection();
    }

    /** Internal implementation of getSelection() which returns the selected feature
     *  descriptor whether or not the component has focus. */
    public final FeatureDescriptor _getSelection() {
        int i = getSelectedRow();
        FeatureDescriptor result;

        //Check bounds - a change can be fired after the model has been changed, but
        //before the table has received the event and updated itself, in which case
        //you get an AIOOBE
        if (i < getPropertySetModel().getCount()) {
            result = getSheetModel().getPropertySetModel().getFeatureDescriptor(getSelectedRow());
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Select (and start editing) the given property.
     * @param fd
     * @param startEditing
     */
    public void select( FeatureDescriptor fd, boolean startEditing ) {
        PropertySetModel psm = getPropertySetModel();
        final int index = psm.indexOf( fd );
        if( index < 0 ) {
            return; //not in our list
        }

        getSelectionModel().setSelectionInterval( index, index );
        if( startEditing && psm.isProperty( index ) ) {
            editCellAt( index, 1, new MouseEvent( SheetTable.this, 0, System.currentTimeMillis(), 0, 0, 0, 1, false) );
            SwingUtilities.invokeLater( new Runnable() {
                @Override
                public void run() {
                    SheetCellEditor cellEditor = getEditor();
                    if( null != cellEditor ) {
                        InplaceEditor inplace = cellEditor.getInplaceEditor();
                        if( null != inplace && null != inplace.getComponent() ) {
                            inplace.getComponent().requestFocus();
                        }
                    }
                }
            });
        }
    }

    //*********Implementation of editing*************************************    

    /**
     * Overridden to do a bunch of property related things:  cancel editing
     * if the name cell was clicked; ignore duplicate requests; ignore edit
     * requests if the user is currently dragging the center line; launch
     * the custom editor dialog without entering edit mode if a click is
     * over the custom editor button;  expand/close property sets; directly
     * toggle boolean values rather than rapidly instantiate and hide a
     * checkbox editor
     */
    @Override
    public boolean editCellAt(int row, int column, EventObject e) {
        assert SwingUtilities.isEventDispatchThread();
        enterEditRequest();

        if( (editingRow == row) && isEditing() ) {
            if( 0 == column ) {
                //click on name cell should stop editing
                getEditor().stopCellEditing();
                removeEditor();
            }
            //discard edit requests if we're already editing that cell
            exitEditRequest();

            return false;
        }

        //issue 37584, there are some requests for commit on focus loss,
        //so we'll try experimental support for this.  Not sure it's a great
        //idea, but might as well keep an open mind
        if (PropUtils.psCommitOnFocusLoss && isEditing()) {
            getEditor().stopCellEditing();

            // #53870: it can happen that PropertySheet window is closed when
            // previous property editing is finished (e.g. Form event properties)
            // If this is the case don't try to edit newly selected property.
            if (isGoingToBeClosed()) {
                return false;
            }
        }

        if ((e instanceof MouseEvent) && (onCenterLine((MouseEvent) e))) {
            //If it's a drag request, other code will handle it
            exitEditRequest();

            return false;
        }

        if ((e instanceof MouseEvent) && (onCustomEditorButton((MouseEvent) e))) {
            if (PropUtils.isLoggable(SheetTable.class)) {
                PropUtils.log(SheetTable.class, "Got a mouse click on the " + "custom editor button"); //NOI18N
            }

            if (isEditing() && (editingRow != row)) {
                removeEditor();
            }

            //If it's a click on the custom editor button, just display the
            //dialog, don't open an inplace editor
            int prevSel = getSelectedRow();
            changeSelection(row, column, false, false);

            if (prevSel != -1) {
                paintRow(prevSel);
            }

            paintSelectionRow();
            getCustomEditorAction().actionPerformed(new ActionEvent(this, 0, null));
            exitEditRequest();

            return false;
        }

        //Get the selected item
        FeatureDescriptor fd = getPropertySetModel().getFeatureDescriptor(row);

        //See if we got an edit trigger for a property set - if so,
        //toggle its expanded state
        if (fd instanceof PropertySet) {
            //It was a legitimate click, so do stop editing and set the
            //selection (otherwise selection will change but editor will remain)
            if (isEditing()) {
                removeEditor();
                changeSelection(row, column, false, false);
            }

            maybeToggleExpanded(row, e);
            exitEditRequest();

            return false;
        }

        //Set the flag indicating we're starting to edit - affects paint
        //and focus requests

        /*        boolean useRadioButtons = PropUtils.forceRadioButtons ||
                    (fd.getValue ("stringValues") != null);
         */
        boolean useRadioButtons = (e instanceof MouseEvent && PropUtils.forceRadioButtons) ||
            ((fd != null) && (fd.getValue("stringValues") != null));

        //Special handling for boolean if checkbox - no need to create an 
        //editor that will be removed immediately, just toggles the value
        //programmatically
        if (!useRadioButtons && (((column == 1) || e instanceof KeyEvent) && checkEditBoolean(row))) {
            //if checkEditBoolean returned true, then the value was toggled -
            //set the flag off and return
            exitEditRequest();

            return false;
        }

        boolean result = false;

        try {
            //Try to start an actual edit
            result = super.editCellAt(row, column, e);
        } finally {
            exitEditRequest();
        }

        return result;
    }

    @Override
    public void removeEditor() {
        enterEditorRemoveRequest();

        try {
            //        synchronized(getTreeLock()) {
            super.removeEditor();

            //Make the editor detach its listeners and clear values in the
            //inplace editor since we're done with it
            getEditor().setInplaceEditor(null);

            //        }
            //Order of removal can cause the custom editor button to get focus even
            //though it's no longer onscreen, when the custom editor is removed
            //        Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        } finally {
            exitEditorRemoveRequest();
        }
    }

    /**Overridden to do the assorted black magic by which one determines if
     * a property is editable */
    @Override
    public boolean isCellEditable(int row, int column) {
        if (column == 0) {
            return null != getCustomEditor( row );
        }

        FeatureDescriptor fd = getPropertySetModel().getFeatureDescriptor(row);
        boolean result;

        if (fd instanceof PropertySet) {
            result = false;
        } else {
            Property p = (Property) fd;
            result = p.canWrite();

            if (result) {
                Object val = p.getValue("canEditAsText"); //NOI18N

                if (val != null) {
                    result &= Boolean.TRUE.equals(val);
                    if( !result ) {
                        //#227661 - combo box editor should be allowed to show its popup
                        PropertyEditor ped = PropUtils.getPropertyEditor(p);
                        result |= ped.getTags() != null;
                    }
                }
            }
        }

        return result;
    }

    /** Toggle the expanded state of a property set if either the event
     *  was a double click in the title area, a single click in the spinner
     *  area, or a keyboard event. */
    private void maybeToggleExpanded(int row, EventObject e) {
        boolean doExpand = true;

        //If it's a mouse event, we need to check if it's a double click.
        if (e instanceof MouseEvent) {
            MouseEvent me = (MouseEvent) e;
            doExpand = me.getClickCount() > 1;

            //If not a double click, allow single click in the spinner margin
            if (!doExpand) {
                //marginWidth will definitely be initialized, you can't
                //click something that isn't on the screen
                doExpand = me.getPoint().x <= PropUtils.getMarginWidth();
            }
        }

        if (doExpand) {
            toggleExpanded(row);
        }
    }

    /** Toggle the expanded state of a property set.  If editing, the edit is
     *  cancelled.  */
    private void toggleExpanded(int index) {
        if (isEditing()) {
            getEditor().cancelCellEditing();
        }

        PropertySetModel psm = getSheetModel().getPropertySetModel();
        psm.toggleExpanded(index);
    }

    /** In the case that an edit request is made on a boolean checkbox property, an
     *  edit request should simply toggle its state without instantiating a custom
     *  editor component.  Returns true if the state was toggled, in which case the
     *  editor instantiation portion of editCellAt() should be aborted  */
    boolean checkEditBoolean(int row) {
        FeatureDescriptor fd = getSheetModel().getPropertySetModel().getFeatureDescriptor(row);

        if (fd != null && fd.getValue("stringValues") != null) {
            return false; //NOI18N
        }

        Property p = (fd instanceof Property) ? (Property) fd : null;

        if (p != null) {
            Class c = p.getValueType();

            //only do this if the property is supplying no special values for
            //the tags - if it is, we are using the radio button renderer
            if ((c == Boolean.class) || (c == boolean.class)) {
                if (!isCellEditable(row, 1)) {
                    return true;
                }

                //Okay, try to toggle it
                try {
                    Boolean b = null;

                    //get the current value
                    try {
                        Object value = p.getValue();
                        if( value instanceof Boolean ) {
                            b = (Boolean) value;
                        } else {
                            //150048 - somebody has sneaked in a wrong value
                            return false;
                        }
                    } catch (ProxyNode.DifferentValuesException dve) {
                        //If we're represeting conflicting multi-selected 
                        //properties, we'll make them both true when we toggle
                        b = Boolean.FALSE;
                    }

                    if (isEditing()) {
                        removeEditor();
                    }

                    changeSelection(row, 1, false, false);

                    //Toggle the value
                    Boolean newValue = ((b == null) || Boolean.FALSE.equals(b)) ? Boolean.TRUE : Boolean.FALSE;
                    p.setValue(newValue);

                    //Force an event so we'll repaint
                    /*
                    tableChanged(new TableModelEvent (getSheetModel(), row,
                        row, 1, TableModelEvent.UPDATE));
                     */
                    paintRow(row);

                    return true;
                } catch (Exception ex) {
                    //Something wrong, log it
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        return false;
    }

    /** Overridden to set the colors apropriately - we always want the editor
    * to appear selected */
    @Override
    public Component prepareEditor(TableCellEditor editor, int row, int col) {
        if (editor == null) {
            return null;
        }

        Component result = super.prepareEditor(editor, row, col);

        if (result == null) {
            return null;
        }

        if( 1 == col ) {
        //Usually result == ine, but custom impls may not be
        InplaceEditor ine = getEditor().getInplaceEditor();

        if (ine.supportsTextEntry()) {
            result.setBackground(PropUtils.getTextFieldBackground());
            result.setForeground(PropUtils.getTextFieldForeground());
        }
        }

        if (result instanceof JComponent) {
            //unlikely that it won't be
            ((JComponent) result).setBorder(BorderFactory.createEmptyBorder(0, PropUtils.getTextMargin(), 0, 0));
        }

        return result;
    }

    //***********Methods for storing state if a recoverable change is happening****    

    /** Overridden to store some data in the event of a recoverable change,
     * such as the row currently being edited */
    @Override
    public void tableChanged(TableModelEvent e) {
        boolean ed = isEditing();
        lastSelectedRow = ed ? getEditingRow() : getSelectionModel().getAnchorSelectionIndex();

        if (ed) {
            getEditor().stopCellEditing();
        }

        super.tableChanged(e);
        restoreEditingState();
    }

    /** Temporarily store the currently edited feature descriptor and partial
     * value from the editor.  This info is used to restore the editing state
     * after temporary losses of focus and recoverable changes like reordering
     * the model, or changes that derive from the underlying node */
    void saveEditingState() {
        storedFd = _getSelection();

        if (isEditing()) {
            InplaceEditor ine = getEditor().getInplaceEditor();

            if (ine != null) {
                partialValue = ine.getValue();
            }
        }
    }

    /** Restore the previous editing state, if the previously edited
     * FeatureDescriptor is still available for editing */
    void restoreEditingState() {
        int idx = indexOfLastSelected();
        boolean canResumeEditing = idx != -1;

        if (!canResumeEditing) {
            idx = lastSelectedRow;
        }

        if (idx == -1) {
            clearSavedEditingState();

            return;
        }

        if (idx < getRowCount()) {
            changeSelection(idx, 1, false, false);

            if ((canResumeEditing) && wasEditing) {
                editCellAt(idx, 1);

                InplaceEditor ine = getEditor().getInplaceEditor();

                if ((ine != null) && (partialValue != null)) {
                    ine.setValue(partialValue);
                }
            }
        }

        clearSavedEditingState();
    }

    /** Clear saved editing data, so no memory leaks can occur */
    private void clearSavedEditingState() {
        storedFd = null;
        wasEditing = false;
        partialValue = null;
    }

    /** Find the current index of the last edited FeatureDescriptor, to
     * figure out in which cell to restore the editing state */
    private int indexOfLastSelected() {
        if (storedFd == null) {
            return -1;
        }

        PropertySetModel mdl = getPropertySetModel();
        int idx = mdl.indexOf(storedFd);
        storedFd = null;

        return idx;
    }

    //*************PropertySetModelListener implementation ******************

    /** If we know a change is going to happen, try to store the current
     * state to restore after the change is completed.  Reordering of
     * properties and addition of properties by the underlying node can
     * trigger this.  Since the PropertySetModel has a cache of current
     * properties, it can call this while its internal state is still
     * intact */
    @Override
    public void pendingChange(PropertySetModelEvent e) {
        if (e.isReordering()) {
            wasEditing = isEditing();
            saveEditingState();
        } else {
            storedFd = null;
            wasEditing = false;
            partialValue = null;
        }
    }

    @Override
    public void boundedChange(PropertySetModelEvent e) {
        //Do nothing, we'll get notification from the TableModel
    }

    @Override
    public void wholesaleChange(PropertySetModelEvent e) {
        //Do nothing, we'll get notification from the TableModel
    }

    //*************CustomEditorAction.Invoker implementation ******************
    //Generally, EditablePropertyDisplayer does a lot more with this 
    //interface than SheetTable needs to

    /** Returns the content pane of our owner, so as to display the wait
     * cursor while the dialog is being invoked */
    @Override
    public Component getCursorChangeComponent() {
        Container cont = SheetTable.this.getTopLevelAncestor();

        return (cont instanceof JFrame) ? ((JFrame) cont).getContentPane()
                                        : ((cont instanceof JDialog) ? ((JDialog) cont).getContentPane() : cont);
    }

    /** If we have been editing and the user has typed something, fetch this
     * value to use in the custom editor */
    @Override
    public Object getPartialValue() {
        Object partialValue = null;

        if (isEditing() && (editingRow == getSelectedRow())) {
            InplaceEditor ine = getEditor().getInplaceEditor();

            if (ine != null) {
                partialValue = ine.getValue();

                //reset the inplace editor so the value is not taken when the editor
                //is closed
                ine.reset();
                getEditor().cancelCellEditing();
            }
        } else {
            partialValue = null;

            if (isEditing()) {
                removeEditor();
            }
        }

        return partialValue;
    }

    /** Restarts inline edit mode if the the preceding custom edit failed */
    @Override
    public void editorClosed() {
        if (lastFailed) {
            editCellAt(getSelectedRow(), 1, null);
        }

        repaint();
        customEditorIsOpen = false;
    }

    @Override
    public void editorOpened() {
        //Make sure it's painted as non-focused
        paintSelectionRow();
        customEditorIsOpen = true;
    }

    @Override
    public void editorOpening() {
        lastFailed = false;
        customEditorIsOpen = true;
    }

    @Override
    public void valueChanged(java.beans.PropertyEditor editor) {
        lastFailed = false;
    }

    @Override
    public boolean allowInvoke() {
        return true;
    }

    @Override
    public void failed() {
        lastFailed = true;
    }

    @Override
    public boolean wantAllChanges() {
        return false;
    }

    @Override
    public ReusablePropertyEnv getReusablePropertyEnv() {
        return reusableEnv;
    }

    public ReusablePropertyModel getReusablePropertyModel() {
        return reusableModel;
    }

    private boolean isGoingToBeClosed() {
        // TODO mkrauskopf: try to find better way for the case that
        // PropertySheet is going to be removed (note that isShowing, isVisible,
        // ... methods return still true when this method is called)
        return getRowCount() <= 0;
    }

    @Override
    public void setUI(TableUI ui) {
        super.setUI(ui);
        renderer = null;
        sheetCellEditor = null;
    }

    //*************Actions bound to the keyboard ******************
    private class ExpandAction extends AbstractAction {
        public ExpandAction() {
            super(ACTION_EXPAND);
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            FeatureDescriptor fd = _getSelection();

            if (fd instanceof PropertySet) {
                int row = SheetTable.this.getSelectedRow();
                boolean b = getPropertySetModel().isExpanded(fd);

                if (b) {
                    toggleExpanded(row);
                }
            }
        }

        @Override
        public boolean isEnabled() {
            return _getSelection() instanceof PropertySet;
        }
    }

    private class CollapseAction extends AbstractAction {
        public CollapseAction() {
            super(ACTION_COLLAPSE);
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            FeatureDescriptor fd = _getSelection();

            if (fd instanceof PropertySet) {
                int row = SheetTable.this.getSelectedRow();
                boolean b = getPropertySetModel().isExpanded(fd);

                if (!b) {
                    toggleExpanded(row);
                }
            }
        }

        @Override
        public boolean isEnabled() {
            boolean result = _getSelection() instanceof PropertySet;

            return result;
        }
    }

    private class EditorClassAction extends AbstractAction {
        public EditorClassAction() {
            super(ACTION_EDCLASS);
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            int i = getSelectedRow();

            if (i != -1) {
                FeatureDescriptor fd = getPropertySetModel().getFeatureDescriptor(i);

                if (fd instanceof Property) {
                    java.beans.PropertyEditor ped = PropUtils.getPropertyEditor((Property) fd);
                    System.err.println(ped.getClass().getName());
                } else {
                    System.err.println("PropertySets - no editor"); //NOI18N
                }
            } else {
                System.err.println("No selection"); //NOI18N
            }
        }

        @Override
        public boolean isEnabled() {
            return getSelectedRow() != -1;
        }
    }

    private class STPolicy extends ContainerOrderFocusTraversalPolicy {
        @Override
        public Component getComponentAfter(Container focusCycleRoot, Component aComponent) {
            if (inEditorRemoveRequest()) {
                return SheetTable.this;
            } else {
                Component result = super.getComponentAfter(focusCycleRoot, aComponent);

                return result;
            }
        }

        @Override
        public Component getComponentBefore(Container focusCycleRoot, Component aComponent) {
            if (inEditorRemoveRequest()) {
                return SheetTable.this;
            } else {
                return super.getComponentBefore(focusCycleRoot, aComponent);
            }
        }

        @Override
        public Component getFirstComponent(Container focusCycleRoot) {
            if (!inEditorRemoveRequest() && isEditing()) {
                return editorComp;
            } else {
                return SheetTable.this;
            }
        }

        @Override
        public Component getDefaultComponent(Container focusCycleRoot) {
            if (!inEditorRemoveRequest() && isEditing() && editorComp.isShowing()) {
                return editorComp;
            } else {
                return SheetTable.this;
            }
        }

        @Override
        protected boolean accept(Component aComponent) {
            //Do not allow focus to go to a child of the editor we're using if
            //we are in the process of removing the editor
            if (isEditing() && inEditorRemoveRequest()) {
                InplaceEditor ine = getEditor().getInplaceEditor();

                if (ine != null) {
                    if ((aComponent == ine.getComponent()) || ine.isKnownComponent(aComponent)) {
                        return false;
                    }
                }
            }

            return super.accept(aComponent) && aComponent.isShowing();
        }
    }

    private static class SheetTableTransferHandler extends TransferHandler {
        @Override
        protected Transferable createTransferable(JComponent c) {
            if (c instanceof SheetTable) {
                SheetTable table = (SheetTable) c;
                FeatureDescriptor fd = table.getSelection();

                if (fd == null) {
                    return null;
                }

                String res = fd.getDisplayName();

                if (fd instanceof Node.Property) {
                    Node.Property prop = (Node.Property) fd;
                    res += ("\t" + PropUtils.getPropertyEditor(prop).getAsText());
                }

                return new SheetTableTransferable(res);
            }

            return null;
        }

        @Override
        public int getSourceActions(JComponent c) {
            return COPY;
        }
    }

    /**
     * Transferable implementation for SheetTable.
     */
    private static class SheetTableTransferable implements Transferable {
        private static DataFlavor[] stringFlavors;
        private static DataFlavor[] plainFlavors;

        static {
            try {
                plainFlavors = new DataFlavor[3];
                plainFlavors[0] = new DataFlavor("text/plain;class=java.lang.String"); // NOI18N
                plainFlavors[1] = new DataFlavor("text/plain;class=java.io.Reader"); // NOI18N
                // XXX isn't this just DataFlavor.plainTextFlavor?
                plainFlavors[2] = new DataFlavor("text/plain;charset=unicode;class=java.io.InputStream"); // NOI18N

                stringFlavors = new DataFlavor[2];
                stringFlavors[0] = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=java.lang.String"); // NOI18N
                stringFlavors[1] = DataFlavor.stringFlavor;
            } catch (ClassNotFoundException cle) {
                assert false : cle;
            }
        }

        protected String plainData;

        public SheetTableTransferable(String plainData) {
            this.plainData = plainData;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            int nPlain = (isPlainSupported()) ? plainFlavors.length : 0;
            int nString = (isPlainSupported()) ? stringFlavors.length : 0;
            int nFlavors = nPlain + nString;
            DataFlavor[] flavors = new DataFlavor[nFlavors];

            // fill in the array
            int nDone = 0;

            if (nPlain > 0) {
                System.arraycopy(plainFlavors, 0, flavors, nDone, nPlain);
                nDone += nPlain;
            }

            if (nString > 0) {
                System.arraycopy(stringFlavors, 0, flavors, nDone, nString);
                nDone += nString;
            }

            return flavors;
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            DataFlavor[] flavors = getTransferDataFlavors();

            for (int i = 0; i < flavors.length; i++) {
                if (flavors[i].equals(flavor)) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public Object getTransferData(DataFlavor flavor)
        throws UnsupportedFlavorException, IOException {
            if (isPlainFlavor(flavor)) {
                String data = getPlainData();
                data = (data == null) ? "" : data;

                if (String.class.equals(flavor.getRepresentationClass())) {
                    return data;
                } else if (Reader.class.equals(flavor.getRepresentationClass())) {
                    return new StringReader(data);
                } else if (InputStream.class.equals(flavor.getRepresentationClass())) {
                    // XXX should this enforce UTF-8 encoding?
                    return new StringBufferInputStream(data);
                }

                // fall through to unsupported
            } else if (isStringFlavor(flavor)) {
                String data = getPlainData();
                data = (data == null) ? "" : data;

                return data;
            }

            throw new UnsupportedFlavorException(flavor);
        }

        // --- plain text flavors ----------------------------------------------

        /**
         * Returns whether or not the specified data flavor is an plain flavor
         * that is supported.
         *
         * @param flavor the requested flavor for the data
         * @return boolean indicating whether or not the data flavor is supported
         */
        protected boolean isPlainFlavor(DataFlavor flavor) {
            DataFlavor[] flavors = plainFlavors;

            for (int i = 0; i < flavors.length; i++) {
                if (flavors[i].equals(flavor)) {
                    return true;
                }
            }

            return false;
        }

        /**
         * Should the plain text flavors be offered?  If so, the method
         * getPlainData should be implemented to provide something reasonable.
         */
        protected boolean isPlainSupported() {
            return plainData != null;
        }

        /**
         * Fetch the data in a text/plain format.
         */
        protected String getPlainData() {
            return plainData;
        }

        // --- string flavors --------------------------------------------------

        /**
         * Returns whether or not the specified data flavor is a String flavor
         * that is supported.
         *
         * @param flavor the requested flavor for the data
         * @return boolean indicating whether or not the data flavor is supported
         */
        protected boolean isStringFlavor(DataFlavor flavor) {
            DataFlavor[] flavors = stringFlavors;

            for (int i = 0; i < flavors.length; i++) {
                if (flavors[i].equals(flavor)) {
                    return true;
                }
            }

            return false;
        }
    }

    private class IncrementAction extends AbstractAction {

        private final boolean isIncrement;
        private final Action changeRowAction;

        private IncrementAction( boolean increment, Action defaultAction ) {
            this.isIncrement = increment;
            this.changeRowAction = defaultAction;
}

        @Override
        public void actionPerformed(ActionEvent e) {
            if (isEditing()) {
                SheetCellEditor cellEditor = getEditor();
                InplaceEditor inplaceEditor = cellEditor.getInplaceEditor();
                if (inplaceEditor instanceof IncrementPropertyValueSupport) {
                    IncrementPropertyValueSupport incrementSupport = ( IncrementPropertyValueSupport ) inplaceEditor;
                    boolean consume = isIncrement ? incrementSupport.incrementValue() : incrementSupport.decrementValue();
                    if( consume )
                        return;
                }
            }
            changeRowAction.actionPerformed(e);
        }
    }
}
