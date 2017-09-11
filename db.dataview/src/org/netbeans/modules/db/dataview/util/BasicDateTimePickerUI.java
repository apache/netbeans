/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2008, 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 */
package org.netbeans.modules.db.dataview.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;

import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JFormattedTextField.AbstractFormatterFactory;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.View;

import org.jdesktop.swingx.JXMonthView;
import org.jdesktop.swingx.SwingXUtilities;
import org.jdesktop.swingx.calendar.CalendarUtils;
import org.jdesktop.swingx.calendar.DatePickerFormatter;
import org.jdesktop.swingx.calendar.DateSelectionModel;
import org.jdesktop.swingx.calendar.DatePickerFormatter.DatePickerFormatterUIResource;
import org.jdesktop.swingx.event.DateSelectionEvent;
import org.jdesktop.swingx.event.DateSelectionListener;
import org.jdesktop.swingx.event.DateSelectionEvent.EventType;


/**
 * The basic implementation of a <code>DatePickerUI</code>.
 * <p>
 * 
 * 
 * @author Joshua Outwater
 * @author Jeanette Winzenburg
 */
public class BasicDateTimePickerUI  extends ComponentUI {

    @SuppressWarnings("all")
    private static final Logger LOG = Logger.getLogger(BasicDateTimePickerUI.class
            .getName());
    
    protected JXDateTimePicker datePicker;
    private JButton popupButton;
    private BasicDatePickerPopup popup;
    private Handler handler;
    /* 
     * shared listeners
     */
    protected PropertyChangeListener propertyChangeListener;
    private FocusListener focusListener;
    
    /*
     * listener's for the arrow button
     */ 
    protected MouseListener mouseListener;
    protected MouseMotionListener mouseMotionListener;

    /*
     * listeners for the picker's editor
     */
    private ActionListener editorActionListener;
    private EditorCancelAction editorCancelAction;
    private PropertyChangeListener editorPropertyListener;
    
    /**
     * listeners for the picker's monthview
     */
    private DateSelectionListener monthViewSelectionListener;
    private ActionListener monthViewActionListener;
    private PropertyChangeListener monthViewPropertyListener;

    private PopupRemover popupRemover;


    @SuppressWarnings({"UnusedDeclaration"})
    public static ComponentUI createUI(JComponent c) {
        return new BasicDateTimePickerUI();
    }

    @Override
    public void installUI(JComponent c) {
        datePicker = (JXDateTimePicker)c;
        datePicker.setLayout(createLayoutManager());
        installComponents();
        installDefaults();
        installKeyboardActions();
        installListeners();
    }

    @Override
    public void uninstallUI(JComponent c) {
        uninstallListeners();
        uninstallKeyboardActions();
        uninstallDefaults();
        uninstallComponents();
        datePicker.setLayout(null);
        datePicker = null;
    }

    protected void installComponents() {
        
        JFormattedTextField editor = datePicker.getEditor();
        if (editor == null || editor instanceof UIResource) {
            DateFormat[] formats = getCustomFormats(editor);
            // we are not yet listening ...
            datePicker.setEditor(createEditor());
            if (formats != null) {
                datePicker.setFormats(formats);
            }
        }
        updateFromEditorChanged(null, false);
        
        popupButton = createPopupButton();
        if (popupButton != null) {
            popupButton.putClientProperty("doNotCancelPopup",
                    createDoNotCancelPopupClientProperty());
            datePicker.add(popupButton);
        }
            updateChildLocale(datePicker.getLocale());
        
    }

    /**
     * Checks and returns custom formats on the editor, if any.
     * 
     * @param editor the editor to check
     * @return the custom formats uses in the editor or null if it had
     *   used defaults as defined in the datepicker properties
     */
    private DateFormat[] getCustomFormats(JFormattedTextField editor) {
        DateFormat[] formats = null;
        if (editor != null) {
            AbstractFormatterFactory factory = editor.getFormatterFactory();
            if (factory != null) {
                AbstractFormatter formatter = factory.getFormatter(editor);
                if (!(formatter instanceof DatePickerFormatterUIResource))  {
                    formats = ((DatePickerFormatter) formatter).getFormats();
                }
            }

        }
        return formats;
    }

    protected void uninstallComponents() {
        JFormattedTextField editor = datePicker.getEditor();
        if (editor != null) {
            datePicker.remove(editor);
        }

        if (popupButton != null) {
            datePicker.remove(popupButton);
            popupButton = null;
        }
    }

    protected void installDefaults() {

    }

    protected void uninstallDefaults() {

    }

    protected void installKeyboardActions() {
        // install picker's actions
        ActionMap pickerMap = datePicker.getActionMap();
        pickerMap.put(JXDateTimePicker.CANCEL_KEY, createCancelAction());
        pickerMap.put(JXDateTimePicker.COMMIT_KEY, createCommitAction());
        pickerMap.put(JXDateTimePicker.HOME_NAVIGATE_KEY, createHomeAction(false));
        pickerMap.put(JXDateTimePicker.HOME_COMMIT_KEY, createHomeAction(true));
        TogglePopupAction popupAction = createTogglePopupAction();
        pickerMap.put("TOGGLE_POPUP", popupAction);
        
        InputMap pickerInputMap = datePicker.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        pickerInputMap.put(KeyStroke.getKeyStroke("ENTER"), JXDateTimePicker.COMMIT_KEY);
        pickerInputMap.put(KeyStroke.getKeyStroke("ESCAPE"), JXDateTimePicker.CANCEL_KEY);
        // PENDING: get from LF
        pickerInputMap.put(KeyStroke.getKeyStroke("F5"), JXDateTimePicker.HOME_COMMIT_KEY);
        pickerInputMap.put(KeyStroke.getKeyStroke("shift F5"), JXDateTimePicker.HOME_NAVIGATE_KEY);
        pickerInputMap.put(KeyStroke.getKeyStroke("SPACE"), "TOGGLE_POPUP");
        
        installLinkPanelKeyboardActions();
    }

    protected void uninstallKeyboardActions() {
        uninstallLinkPanelKeyboardActions(datePicker.getLinkPanel());
    }

    
    /**
     * Installs actions and key bindings on the datePicker's linkPanel. Does
     * nothing if the linkPanel is null.
     * 
     * PRE: keybindings installed on picker.
     */
    protected void installLinkPanelKeyboardActions() {
        if (datePicker.getLinkPanel() == null)  {
            return;
        }
        ActionMap map = datePicker.getLinkPanel().getActionMap();
        map.put(JXDateTimePicker.HOME_COMMIT_KEY, datePicker.getActionMap().get(
                JXDateTimePicker.HOME_COMMIT_KEY));
        map.put(JXDateTimePicker.HOME_NAVIGATE_KEY, datePicker.getActionMap().get(
                JXDateTimePicker.HOME_NAVIGATE_KEY));
        InputMap inputMap = datePicker.getLinkPanel().getInputMap(
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        // PENDING: get from LF
        inputMap.put(KeyStroke.getKeyStroke("F5"), 
                JXDateTimePicker.HOME_COMMIT_KEY);
        inputMap.put(KeyStroke.getKeyStroke("shift F5"),
                JXDateTimePicker.HOME_NAVIGATE_KEY);
    }


    /**
     * Uninstalls actions and key bindings from linkPanel. Does nothing if the
     * linkPanel is null.
     * 
     * @param panel the component to uninstall
     * 
     */
    protected void uninstallLinkPanelKeyboardActions(JComponent panel) {
        if (panel == null) {
            return;
        }
        ActionMap map = panel.getActionMap();
        map.remove(JXDateTimePicker.HOME_COMMIT_KEY); 
        map.remove(JXDateTimePicker.HOME_NAVIGATE_KEY); 
        InputMap inputMap = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        // PENDING: get from LF
        inputMap.remove(KeyStroke.getKeyStroke("F5"));
        inputMap.remove(KeyStroke.getKeyStroke("shift F5"));
        
    }

    /**
     * Creates and installs all listeners to all components.
     *
     */
    protected void installListeners() {
        /*
         * create the listeners. 
         */
        // propertyListener for datePicker
        propertyChangeListener = createPropertyChangeListener();
        
        // mouseListener (for popup button only) ?
        mouseListener = createMouseListener();
        mouseMotionListener = createMouseMotionListener();
        
        // shared focuslistener (installed to picker and editor)
        focusListener = createFocusListener();
        
        // editor related listeners
        editorActionListener = createEditorActionListener();
        editorPropertyListener = createEditorPropertyListener();
        
        // montheView related listeners
        monthViewSelectionListener = createMonthViewSelectionListener();
        monthViewActionListener = createMonthViewActionListener();
        monthViewPropertyListener = createMonthViewPropertyListener();
        
        popupRemover = new PopupRemover();
        /*
         * install the listeners
         */
        // picker 
        datePicker.addPropertyChangeListener(propertyChangeListener);
        datePicker.addFocusListener(focusListener);
        
        if (popupButton != null) {
            // JW: which property do we want to monitor?
            popupButton.addPropertyChangeListener(propertyChangeListener);
            popupButton.addMouseListener(mouseListener);
            popupButton.addMouseMotionListener(mouseMotionListener);
        }
        
        updateEditorListeners(null);
        // JW the following does more than installing the listeners ..
        // synchs properties of datepicker to monthView's
        // prepares monthview for usage in popup
        // synch the date
        // Relies on being the last thing done in the install ..
        //
        updateFromMonthViewChanged(null);
    }
    /**
     * Uninstalls and nulls all listeners which had been installed 
     * by this delegate.
     *
     */
    protected void uninstallListeners() {
        // datePicker
        datePicker.removePropertyChangeListener(propertyChangeListener);
        datePicker.removeFocusListener(focusListener);
        
        // monthView
        datePicker.getMonthView().getSelectionModel().removeDateSelectionListener(monthViewSelectionListener);
        datePicker.getMonthView().removeActionListener(monthViewActionListener);
        datePicker.getMonthView().removePropertyChangeListener(propertyChangeListener);
        
        // JW: when can that be null?
        // maybe in the very beginning? if some code calls ui.uninstall
        // before ui.install? The editor is created by the ui. 
        if (datePicker.getEditor() != null) {
            uninstallEditorListeners(datePicker.getEditor());
        }
        if (popupButton != null) {
            popupButton.removePropertyChangeListener(propertyChangeListener);
            popupButton.removeMouseListener(mouseListener);
            popupButton.removeMouseMotionListener(mouseMotionListener);
        }

        popupRemover.unload();
        
        popupRemover = null;
        propertyChangeListener = null;
        mouseListener = null;
        mouseMotionListener = null;
        
        editorActionListener = null;
        editorPropertyListener = null;
        
        monthViewSelectionListener = null;
        monthViewActionListener = null;
        monthViewPropertyListener = null;
        
        handler = null;
    }

//  --------------------- wiring listeners    
    /**
     * Wires the picker's monthView related listening. Removes all
     * listeners from the given old view and adds the listeners to 
     * the current monthView. <p>
     * 
     * @param oldMonthView
     */
    protected void updateMonthViewListeners(JXMonthView oldMonthView) {
        DateSelectionModel oldModel = null;
        if (oldMonthView != null) {
            oldMonthView.removePropertyChangeListener(monthViewPropertyListener);
            oldMonthView.removeActionListener(monthViewActionListener);
            oldModel = oldMonthView.getSelectionModel();
        }
        datePicker.getMonthView().addPropertyChangeListener(monthViewPropertyListener);
        datePicker.getMonthView().addActionListener(monthViewActionListener);
        updateSelectionModelListeners(oldModel);
    }

    
    /**
     * Wires the picker's editor related listening and actions. Removes 
     * listeners/actions from the old editor and adds them to 
     * the new editor. <p>
     * 
     * @param oldEditor the pickers editor before the change
     */
    protected void updateEditorListeners(JFormattedTextField oldEditor) {
        if (oldEditor != null) {
            uninstallEditorListeners(oldEditor);
        }
        datePicker.getEditor().addPropertyChangeListener(editorPropertyListener);
        datePicker.getEditor().addActionListener(editorActionListener);
        datePicker.getEditor().addFocusListener(focusListener);
        editorCancelAction = new EditorCancelAction(datePicker.getEditor());
    }

    /**
     * Uninstalls all listeners and actions which have been installed
     * by this delegate from the given editor. 
     * 
     * @param oldEditor the editor to uninstall.
     */
    private void uninstallEditorListeners(JFormattedTextField oldEditor) {
        oldEditor.removePropertyChangeListener(editorPropertyListener);
        oldEditor.removeActionListener(editorActionListener);
        oldEditor.removeFocusListener(focusListener);
        if (editorCancelAction != null) {
            editorCancelAction.uninstall();
            editorCancelAction = null;
        }
    }

    /**
     * Wires monthView's selection model listening. Removes the
     * selection listener from the old model and add to the new model.
     * 
     * @param oldModel the dateSelectionModel before the change, may be null.
     */
    protected void updateSelectionModelListeners(DateSelectionModel oldModel) {
        if (oldModel != null) {
            oldModel.removeDateSelectionListener(monthViewSelectionListener);
        }
        datePicker.getMonthView().getSelectionModel()
            .addDateSelectionListener(monthViewSelectionListener);
        
    }


    // ---------------- component creation
    /**
     * Creates the editor used to edit the date selection. The editor is
     * configured with the default DatePickerFormatter marked as UIResource.
     * 
     * @return an instance of a JFormattedTextField
     */
    protected JFormattedTextField createEditor() {
        JFormattedTextField f = new DefaultEditor(
                new DatePickerFormatterUIResource(datePicker.getLocale()));
        f.setName("dateField");
        // this produces a fixed pref widths, looking a bit funny
        // int columns = UIManagerExt.getInt("JXDateTimePicker.numColumns", null);
        // if (columns > 0) {
        // f.setColumns(columns);
        // }
        // that's always 0 as it comes from the resourcebundle
        // f.setColumns(UIManager.getInt("JXDateTimePicker.numColumns"));
        Border border = UIManager.getBorder("JXDateTimePicker.border");
        if (border != null) {
            f.setBorder(border);
        }
        return f;
    }

    protected JButton createPopupButton() {
        JButton b = new JButton();
        b.setName("popupButton");
        b.setRolloverEnabled(false);
        b.setMargin(new Insets(0, 3, 0, 3));

        Icon icon = UIManager.getIcon("JXDateTimePicker.arrowIcon");
        if (icon == null) {
            icon = (Icon)UIManager.get("Tree.expandedIcon");
        }
        b.setIcon(icon);
        b.setFocusable(false);
        return b;
    }

    private class DefaultEditor extends JFormattedTextField implements UIResource {

        private Dimension prefSizeCache;
        private int prefEmptyInset;


        public DefaultEditor(AbstractFormatter formatter) {
            super(formatter);
        }


        @Override
        public Dimension getPreferredSize() {
            Dimension preferredSize = super.getPreferredSize();
            if (getColumns() <= 0) {
                if (getValue() == null) {
                    if (prefSizeCache != null) {
                        preferredSize.width = prefSizeCache.width;
                        preferredSize.height = prefSizeCache.height;
                    } else {
                        prefEmptyInset = preferredSize.width;
                        preferredSize.width = prefEmptyInset + getNullWidth();
                    }
                } else {
                    preferredSize.width += Math.max(prefEmptyInset, 4);
                    prefSizeCache = new Dimension(preferredSize);
                }
            }
            return preferredSize;
        }


        /**
         * @return
         */
        private int getNullWidth() {
            JFormattedTextField field = new JFormattedTextField(getFormatter());
            field.setMargin(getMargin());
            field.setBorder(getBorder());
            field.setFont(getFont());
            field.setValue(new Timestamp(System.currentTimeMillis()));
            return field.getPreferredSize().width;
        }
        
        
    }

// ---------------- Layout    
    /**
     * {@inheritDoc}
     */
    @Override
    public Dimension getMinimumSize(JComponent c) {
        return getPreferredSize(c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dimension getPreferredSize(JComponent c) {
        Dimension dim = getEditorPreferredSize();
        if (popupButton != null) {
            dim.width += popupButton.getPreferredSize().width;
        }
        Insets insets = datePicker.getInsets();
        dim.width += insets.left + insets.right;
        dim.height += insets.top + insets.bottom;
        return (Dimension)dim.clone();
    }

    /**
     * Returns a preferred size for the editor. If the selected date
     * is null, returns a reasonable minimal width. <p>
     * 
     * PENDING: how to find the "reasonable" width is open to discussion.
     * This implementation creates another datepicker, feeds it with 
     * the formats and asks its prefWidth. <p>
     * 
     * That hack blows in some contexts (see Issue #763) - as a very quick
     * replacement create a editor only.
     * 
     * PENDING: there's a resource property JXDateTimePicker.numColumns - why 
     *   don't we use it?
     * 
     * @return the editor's preferred size
     */
    private Dimension getEditorPreferredSize() {
        Dimension dim = datePicker.getEditor().getPreferredSize();
        if (datePicker.getDateTime() == null) {
//            JFormattedTextField field = createEditor(new DatePickerFormatterUIResource(
//                            datePicker.getFormats(), 
//                            datePicker.getLocale()));
//            field.setValue(new Date());
//            dim.width = Math.max(field.getPreferredSize().width, dim.width);
            // the editor tends to collapsing for empty values
            // JW: better do this in a custom editor?
            // seems to produce #763
//            JXDateTimePicker picker = new JXDateTimePicker(new Date());
//            picker.setFormats(datePicker.getFormats());
//            dim.width = picker.getEditor().getPreferredSize().width;
        }
        return dim;
    }

    public int getBaseline(int width, int height) {
        JFormattedTextField editor = datePicker.getEditor();
        View rootView = editor.getUI().getRootView(editor);
        if (rootView.getViewCount() > 0) {
            Insets insets = editor.getInsets();
            Insets insetsOut = datePicker.getInsets();
            int nh = height - insets.top - insets.bottom
                    - insetsOut.top - insetsOut.bottom;
            int y = insets.top + insetsOut.top;
            View fieldView = rootView.getView(0);
            int vspan = (int) fieldView.getPreferredSpan(View.Y_AXIS);
            if (nh != vspan) {
                int slop = nh - vspan;
                y += slop / 2;
            }
            FontMetrics fm = editor.getFontMetrics(editor.getFont());
            y += fm.getAscent();
            return y;
        }
        return -1;
    }


//------------------------------- controller methods/classes 
    

    public Timestamp getSelectableDate(java.util.Date date) throws PropertyVetoException {
        java.util.Date cleaned = date == null ? null :
            datePicker.getMonthView().getSelectionModel().getNormalizedDate(date);
        if (CalendarUtils.areEqual(cleaned, datePicker.getDateTime())) { 
            // one place to interrupt the update spiral
            throw new PropertyVetoException("date not selectable", null);
        }

        if (cleaned != null && datePicker.getMonthView().isUnselectableDate(cleaned)) {
            throw new PropertyVetoException("date not selectable", null);
         }
        
        if(date instanceof Timestamp) {
            return (Timestamp)date;
        }
        return new Timestamp(datePicker.getMonthView().getSelectionDate().getTime());
    }

//-------------------- update methods called from listeners     
    /**
     * Updates internals after picker's date property changed.
     */
    protected void updateFromDateChanged() {
        Timestamp visibleHook = datePicker.getDateTime() != null ?
                datePicker.getDateTime() : new Timestamp(datePicker.getLinkDay().getTime());
        datePicker.getMonthView().ensureDateVisible(visibleHook);        
        datePicker.getEditor().setValue(datePicker.getDateTime());
    }

    /**
     * Updates date related properties in picker/monthView 
     * after a change in the editor's cal1. Reverts the 
     * cal1 if the new date is unselectable.
     * 
     * @param oldDate the editor cal1 before the change
     * @param newDate the editor cal1 after the change
     */
    protected void updateFromValueChanged(Timestamp oldDate, Timestamp newDate) {
        if ((newDate != null) && datePicker.getMonthView().isUnselectableDate(newDate)) {
            revertValue(oldDate);
            return;
        }
        // the other place to interrupt the update spiral
        java.util.Date mvDate = datePicker.getMonthView().getSelectionDate();
        if (newDate != null && mvDate != null) {
            Calendar newCal = Calendar.getInstance();
            newCal.setTime(newDate);

            Calendar oldCal = Calendar.getInstance();
            oldCal.setTime(mvDate);
            
            if (!datePicker.isSameDate(newCal, oldCal)) {
                datePicker.getMonthView().setSelectionDate(newDate);
            }
        }
        datePicker.setDateTime(newDate);
    }
    

    /**
     * PENDING: currently this resets at once - but it's a no-no,
     * because it happens during notification
     * 
     * 
     * @param oldDate the old date to revert to
     */
    private void revertValue(Timestamp oldDate) {
        datePicker.getEditor().setValue(oldDate);
    }
    /**
     * Updates date related properties picker/editor 
     * after a change in the monthView's
     * selection.
     * 
     * Here: does nothing if the change is intermediate.
     * 
     * @param eventType the type of the selection change
     * @param adjusting flag to indicate whether the the selection change
     *    is intermediate
     */
    protected void updateFromSelectionChanged(EventType eventType, boolean adjusting) {
        if (adjusting) {
            return;
        }
        updateEditorValue();
    }

    /**
     * Updates internals after the picker's monthView has changed. <p>
     * 
     * Cleans to popup. Wires the listeners. Updates date. 
     * Updates formats' timezone. 
     * 
     * @param oldMonthView the picker's monthView before the change,
     *   may be null.
     */
    protected void updateFromMonthViewChanged(JXMonthView oldMonthView) {
        popup = null;
        updateMonthViewListeners(oldMonthView);
        TimeZone oldTimeZone = null;
        if (oldMonthView != null) {
            oldMonthView.setComponentInputMapEnabled(false);
            oldTimeZone = oldMonthView.getTimeZone();
        }
        datePicker.getMonthView().setComponentInputMapEnabled(true);
        updateTimeZone(oldTimeZone);
        updateEditorValue();
    }


    /**
     * Updates internals after the picker's editor property 
     * has changed. <p>
     * 
     * Updates the picker's children. Removes the old editor and 
     * adds the new editor. Wires the editor listeners, it the flag
     *  set. Typically, this method is called during installing the
     *  componentUI with the flag set to false and true at all other 
     *  moments.
     * 
     * 
     * @param oldEditor the picker's editor before the change,
     *   may be null.
     * @param updateListeners a flag to indicate whether the listeners
     *   are ready for usage.   
     */
    protected void updateFromEditorChanged(JFormattedTextField oldEditor, 
            boolean updateListeners) { 
        if (oldEditor != null) {
            datePicker.remove(oldEditor);
            oldEditor.putClientProperty("doNotCancelPopup", null);
        }
        datePicker.add(datePicker.getEditor());
        datePicker.getEditor().putClientProperty("doNotCancelPopup",
                createDoNotCancelPopupClientProperty());

        updateEditorValue();
        if (updateListeners) {
            updateEditorListeners(oldEditor);
            datePicker.revalidate();
        }
    }


    /**
     * Updates internals after the selection model changed.
     * 
     * @param oldModel the model before the change.
     */
    protected void updateFromSelectionModelChanged(DateSelectionModel oldModel) {
        updateSelectionModelListeners(oldModel);
        updateEditorValue();
    }

    /**
     * Sets the editor cal1 to the model's selectedDate.
     */
    private void updateEditorValue() {
        //datePicker.getEditor().setValue(datePicker.getMonthView().getSelectionDate());
        Timestamp old = datePicker.getDateTime();
        java.util.Date date = datePicker.getMonthView().getSelectionDate();
        if(old != null && date != null) {
            Calendar selectedCal = Calendar.getInstance();
            selectedCal.setTime(date);
            
            Calendar oldCal = Calendar.getInstance();
            oldCal.setTime(old);

            if (!datePicker.isSameDate(selectedCal, oldCal)) {
                // Merge the selected date with old time
                oldCal.set(Calendar.YEAR, selectedCal.get(Calendar.YEAR));
                oldCal.set(Calendar.MONTH, selectedCal.get(Calendar.MONTH));
                oldCal.set(Calendar.DATE, selectedCal.get(Calendar.DATE));
                date = new Timestamp(oldCal.getTimeInMillis());
                datePicker.getEditor().setValue(date);
            } else {
                datePicker.getEditor().setValue(old);
            }
        } else {
            datePicker.getEditor().setValue(date);
        }
    }

    //---------------------- updating other properties

    
    /**
     * Updates properties which depend on the picker's editable. <p>
     * 
     */
    protected void updateFromEditableChanged() {
        boolean isEditable = datePicker.isEditable();
        datePicker.getMonthView().setEnabled(isEditable);
        datePicker.getEditor().setEditable(isEditable);
        /*
         * PatrykRy: Commit today date is not allowed if datepicker is not editable!
         */
        setActionEnabled(JXDateTimePicker.HOME_COMMIT_KEY, isEditable);
        // for consistency, synch navigation as well 
        setActionEnabled(JXDateTimePicker.HOME_NAVIGATE_KEY, isEditable);
    }

    /**
     * 
     * @param key
     * @param enabled
     */
    private void setActionEnabled(String key, boolean enabled) {
        Action action = datePicker.getActionMap().get(key);
        if (action != null) {
            action.setEnabled(enabled);
        }
    }

    /**
     * Updates the picker's formats to the given TimeZone.
     * @param zone the timezone to set on the formats.
     */
    protected void updateFormatsFromTimeZone(TimeZone zone) {
        for (DateFormat format : datePicker.getFormats()) {
            format.setTimeZone(zone);
        }
    }
    
    /**
     * Updates picker's timezone dependent properties on change notification
     * from the associated monthView.
     * 
     * PENDING JW: DatePicker needs to send notification on timezone change? 
     * 
     * @param old the timezone before the change.
     */
    protected void updateTimeZone(TimeZone old) {
        updateFormatsFromTimeZone(datePicker.getTimeZone());
        updateLinkDate();
    }

    /**
     * Updates the picker's linkDate to be in synch with monthView's today.
     */
    protected void updateLinkDate() {
        datePicker.setLinkDay(datePicker.getMonthView().getToday());
    }

    /**
     * Called form property listener, updates all components locale, formats
     * etc.
     * 
     * @author PeS
     */
    protected void updateLocale() {
        Locale locale = datePicker.getLocale();
        updateFormatLocale(locale);
        updateChildLocale(locale);
    }

    private void updateFormatLocale(Locale locale) {
        if (locale != null) {
            // PENDING JW: timezone?
            if (getCustomFormats(datePicker.getEditor()) == null) {
                datePicker.getEditor().setFormatterFactory(
                        new DefaultFormatterFactory(
                                new DatePickerFormatterUIResource(locale)));
            }
        }
    }

    private void updateChildLocale(Locale locale) {
        if (locale != null) {
            datePicker.getEditor().setLocale(locale);
            datePicker.getLinkPanel().setLocale(locale);
            datePicker.getMonthView().setLocale(locale);
        }
    }
    
    /**
     * @param oldLinkPanel 
     * 
     */
    protected void updateLinkPanel(JComponent oldLinkPanel) {
        if (oldLinkPanel != null) {
            uninstallLinkPanelKeyboardActions(oldLinkPanel);
        }
        installLinkPanelKeyboardActions();
        if (popup != null) {
            popup.updateLinkPanel(oldLinkPanel);
        }
    }


//------------------- methods called by installed actions
    
    /**
     * 
     */
    protected void commit() {
        hidePopup();
        try {
            datePicker.commitEdit();
        } catch (ParseException ex) {
            // can't help it
        }
    }

    /**
     * 
     */
    protected void cancel() {
        hidePopup();
        datePicker.cancelEdit();
    }

    /**
     * PENDING: widened access for debugging - need api to
     * control popup visibility?
     */
    public void hidePopup() {
        if (popup != null) {
            popup.setVisible(false);
        }
    }

    public boolean isPopupVisible() {
        if (popup != null) {
            return popup.isVisible();
        }
        return false;
    }
    /**
     * Navigates to linkDate. If commit, the linkDate is selected
     * and committed. If not commit, the linkDate is scrolled to visible, if the 
     * monthview is open, does nothing for invisible monthView.  
     * 
     * @param commit boolean to indicate whether the linkDate should be
     *   selected and committed
     */
    protected void home(boolean commit) {
        if (commit) {
            Calendar cal = datePicker.getMonthView().getCalendar();
            cal.setTime(datePicker.getLinkDay());
            datePicker.getMonthView().setSelectionDate(cal.getTime());
            datePicker.getMonthView().commitSelection();
        } else {
            datePicker.getMonthView().ensureDateVisible(datePicker.getLinkDay());
        }
    }

//---------------------- other stuff    
    
    /**
     * Creates and returns the action for committing the picker's 
     * input.
     * 
     * @return
     */
    private Action createCommitAction() {
        Action action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                commit();
            }
            
        };
        return action;
    }

    /**
     * Creates and returns the action for cancel the picker's 
     * edit.
     * 
     * @return
     */
    private Action createCancelAction() {
        Action action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancel();
            }
            
        };
        return action;
    }

    private Action createHomeAction(final boolean commit) {
        Action action = new AbstractAction( ) {
            @Override
            public void actionPerformed(ActionEvent e) {
                home(commit);
                
            }
            
        };
        return action ;
    }
    /**
     * The wrapper for the editor cancel action. 
     * 
     * PENDING: Need to extend TestAction?
     * 
     */
    public class EditorCancelAction extends AbstractAction {
        private JFormattedTextField editor;
        private Action cancelAction;
        public static final String TEXT_CANCEL_KEY = "reset-field-edit";
       
        public EditorCancelAction(JFormattedTextField field) {
            install(field);
        }
        
        /**
         * Resets the contained editors actionMap to original and
         * nulls all fields. <p>
         * NOTE: after calling this method the action must not be
         * used! Create a new one for the same or another editor.
         *
         */
        public void uninstall() {
            editor.getActionMap().remove(TEXT_CANCEL_KEY);
            cancelAction = null;
            editor = null;
        }
        
        /**
         * @param editor
         */
        private void install(JFormattedTextField editor) {
            this.editor = editor;
            cancelAction = editor.getActionMap().get(TEXT_CANCEL_KEY);
            editor.getActionMap().put(TEXT_CANCEL_KEY, this);
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            cancelAction.actionPerformed(null);
            cancel();
        }

    }

    /**
     * Creates and returns the action which toggles the visibility of the popup.
     * 
     * @return the action which toggles the visibility of the popup.
     */
    protected TogglePopupAction createTogglePopupAction() {
        return new TogglePopupAction();
    }


    /**
     * Toggles the popups visibility after preparing internal state.
     * 
     *
     */
    public void toggleShowPopup() {
        if (popup == null) {
            popup = createMonthViewPopup();
        }
        if (popup.isVisible()) {
            popup.setVisible(false);
        } else {
            // PENDING JW: Issue 757-swing - datePicker firing focusLost on opening
            // not with following line - but need to run tests
            datePicker.getEditor().requestFocusInWindow();
//            datePicker.requestFocusInWindow();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    popup.show(datePicker,
                            0, datePicker.getHeight());
                }
            });
        }

    }

    /**
     * 
     */
    private BasicDatePickerPopup createMonthViewPopup() {
        BasicDatePickerPopup popup = new BasicDatePickerPopup();
        popup.setLightWeightPopupEnabled(datePicker.isLightWeightPopupEnabled());
        return popup;
    }
    /**
     * Action used to commit the current cal1 in the JFormattedTextField.
     * This action is used by the keyboard bindings.
     */
    private class TogglePopupAction extends AbstractAction {
        public TogglePopupAction() {
            super("TogglePopup");
        }

        @Override
        public void actionPerformed(ActionEvent ev) {
            toggleShowPopup();
        }
    }


    /**
     * Popup component that shows a JXMonthView component along with controlling
     * buttons to allow traversal of the months.  Upon selection of a date the
     * popup will automatically hide itself and enter the selection into the
     * editable field of the JXDateTimePicker.
     * 
     */
    protected class BasicDatePickerPopup extends JPopupMenu {

        public BasicDatePickerPopup() {
            setLayout(new BorderLayout());
            add(datePicker.getMonthView(), BorderLayout.CENTER);
            updateLinkPanel(null);
            //JXPanel timePanel = new JXPanel();
            //timePanel.add(new JXLabel("Time: "));
            //timePanel.add(datePicker._timeField);
            //add(timePanel, BorderLayout.SOUTH);
        }

        /**
         * @param oldLinkPanel
         */
        public void updateLinkPanel(JComponent oldLinkPanel) {
            if (oldLinkPanel != null) {
                remove(oldLinkPanel);
            }
            if (datePicker.getLinkPanel() != null) {
                add(datePicker.getLinkPanel(), BorderLayout.SOUTH);
            }
            
        }
    }

    /**
     * PENDING: JW - I <b>really</b> hate the one-in-all. Wont touch
     *   it for now, maybe later. As long as we have it, the new
     *   listeners (dateSelection) are here too, for consistency.
     *   Adding the Layout here as well is ... , IMO.
     */
    private class Handler implements LayoutManager, MouseListener, MouseMotionListener,
            PropertyChangeListener, DateSelectionListener, ActionListener, FocusListener {

//------------- implement Mouse/MotionListener        
        private boolean _forwardReleaseEvent = false;

        @Override
        public void mouseClicked(MouseEvent ev) {
        }

        @Override
        public void mousePressed(MouseEvent ev) {
            if (!datePicker.isEnabled()) {
                return;
            }
            // PENDING JW: why do we need a mouseListener? the
            // arrowbutton should have the toggleAction installed?
            // Hmm... maybe doesn't ... check!
            // reason might be that we want to open on pressed
            // typically (or LF-dependent?),
            // the button's action is invoked on released.
            toggleShowPopup();
        }

        @Override
        public void mouseReleased(MouseEvent ev) {
            if (!datePicker.isEnabled() || !datePicker.isEditable()) {
                return;
            }

            // Retarget mouse event to the month view.
            if (_forwardReleaseEvent) {
                JXMonthView monthView = datePicker.getMonthView();
                ev = SwingUtilities.convertMouseEvent(popupButton, ev,
                        monthView);
                monthView.dispatchEvent(ev);
                _forwardReleaseEvent = false;
            }
        }

        @Override
        public void mouseEntered(MouseEvent ev) {
        }

        @Override
        public void mouseExited(MouseEvent ev) {
        }

        @Override
        public void mouseDragged(MouseEvent ev) {
            if (!datePicker.isEnabled() || !datePicker.isEditable()) {
                return;
            }

            _forwardReleaseEvent = true;

            if (!popup.isShowing()) {
                return;
            }

            // Retarget mouse event to the month view.
            JXMonthView monthView = datePicker.getMonthView();
            ev = SwingUtilities.convertMouseEvent(popupButton, ev, monthView);
            monthView.dispatchEvent(ev);
        }

        @Override
        public void mouseMoved(MouseEvent ev) {
        }
//------------------ implement DateSelectionListener
        @Override
        public void valueChanged(DateSelectionEvent ev) {
            updateFromSelectionChanged(ev.getEventType(), ev.isAdjusting());
        }

//------------------ implement propertyChangeListener        
        /**
         * {@inheritDoc}
         */
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            if (e.getSource() == datePicker) {
                datePickerPropertyChange(e);
            } else
            if (e.getSource() == datePicker.getEditor()) {
                editorPropertyChange(e);
            } else
            if (e.getSource() == datePicker.getMonthView()) {
                monthViewPropertyChange(e);
            } else
            if (e.getSource() == popupButton) {
                buttonPropertyChange(e);
            } else
            // PENDING - move back, ...
            if ("value".equals(e.getPropertyName())) {
                throw new IllegalStateException(
                        "editor listening is moved to dedicated propertyChangeLisener");
            }
        }
        
        /**
         * Handles property changes from datepicker's editor.
         * 
         * @param e the PropertyChangeEvent object describing the event source
         *        and the property that has changed
         */
        private void editorPropertyChange(PropertyChangeEvent evt) {
            if ("value".equals(evt.getPropertyName())) {

                Object oldVal = evt.getOldValue();
                Object newVal = evt.getNewValue();
                
                Timestamp oldts = null;
                if(oldVal instanceof Timestamp){
                    oldts = (Timestamp)oldVal;
                } else if(oldVal instanceof java.util.Date){
                    oldts = new Timestamp(((java.util.Date)oldVal).getTime());
                } 
  
                Timestamp newts = null;
                if(newVal instanceof Timestamp){
                    newts = (Timestamp) newVal;
                } else if(newVal instanceof java.util.Date){
                    newts = new Timestamp(((java.util.Date)newVal).getTime());
                } 
                
                updateFromValueChanged(oldVal == null ? null : oldts, newts);
            }

        }

        /**
         * Handles property changes from DatePicker.
         * @param e the PropertyChangeEvent object describing the 
         *     event source and the property that has changed
         */
        private void datePickerPropertyChange(PropertyChangeEvent e) {
            String property = e.getPropertyName();
            if ("date".equals(property)) {
                updateFromDateChanged();
            } else if ("enabled".equals(property)) {
                boolean isEnabled = datePicker.isEnabled();
                popupButton.setEnabled(isEnabled);
                datePicker.getEditor().setEnabled(isEnabled);
            } else if ("editable".equals(property)) {
                updateFromEditableChanged();
            } else if (JComponent.TOOL_TIP_TEXT_KEY.equals(property)) {
                String tip = datePicker.getToolTipText();
                datePicker.getEditor().setToolTipText(tip);
                popupButton.setToolTipText(tip);
            } else if (JXDateTimePicker.MONTH_VIEW.equals(property)) {
                updateFromMonthViewChanged((JXMonthView) e.getOldValue());
            } else if (JXDateTimePicker.LINK_PANEL.equals(property)) {
                updateLinkPanel((JComponent) e.getOldValue());
            } else if (JXDateTimePicker.EDITOR.equals(property)) {
                updateFromEditorChanged((JFormattedTextField) e.getOldValue(), true);
            } else if ("componentOrientation".equals(property)) {
                datePicker.revalidate();
            } else if ("lightWeightPopupEnabled".equals(property)) {
                // Force recreation of the popup when this property changes.
                if (popup != null) {
                    popup.setVisible(false);
                }
                popup = null;
            } else if ("formats".equals(property)) {
                updateFormatsFromTimeZone(datePicker.getTimeZone());
            }
            else if ("locale".equals(property)) {
                updateLocale();
            }            
        }

        /**
         * Handles propertyChanges from the picker's monthView.
         * 
         * @param e the PropertyChangeEvent object describing the event source
         *        and the property that has changed
         */
        private void monthViewPropertyChange(PropertyChangeEvent e) {
            if ("selectionModel".equals(e.getPropertyName())) {
                updateFromSelectionModelChanged((DateSelectionModel) e.getOldValue());
            } else if ("timeZone".equals(e.getPropertyName())) {
                updateTimeZone((TimeZone) e.getOldValue());
            } else if ("today".equals(e.getPropertyName())) {
                updateLinkDate();
            }
        }

        /**
         * Handles propertyChanges from the picker's popupButton.
         * 
         * PENDING: does nothing, kept while refactoring .. which
         *   properties from the button do we want to handle?
         * 
         * @param e the PropertyChangeEvent object describing the event source
         *        and the property that has changed.
         */
        private void buttonPropertyChange(PropertyChangeEvent e) {
        }

//-------------- implement LayoutManager
        @Override
        public void addLayoutComponent(String name, Component comp) { }

        @Override
        public void removeLayoutComponent(Component comp) { }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            return parent.getPreferredSize();
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return parent.getMinimumSize();
        }

        @Override
        public void layoutContainer(Container parent) {
            Insets insets = datePicker.getInsets();
            int width = datePicker.getWidth() - insets.left - insets.right;
            int height = datePicker.getHeight() - insets.top - insets.bottom;

            int popupButtonWidth = popupButton != null ? popupButton.getPreferredSize().width : 0;

            boolean ltr = datePicker.getComponentOrientation().isLeftToRight();

            datePicker.getEditor().setBounds(ltr ? insets.left : insets.left + popupButtonWidth,
                    insets.top,
                    width - popupButtonWidth,
                    height);

            if (popupButton != null) {
                popupButton.setBounds(ltr ? width - popupButtonWidth + insets.left : insets.left,
                        insets.top,
                        popupButtonWidth,
                        height);
            }
        }

// ------------- implement actionListener (listening to monthView actionEvent)
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e == null) {
                return;
            }
            if (e.getSource() == datePicker.getMonthView()) {
                monthViewActionPerformed(e);
            } else if (e.getSource() == datePicker.getEditor()) {
                editorActionPerformed(e);
            }
        }

        /**
         * Listening to actionEvents fired by the picker's editor.
         * 
         * @param e
         */
        private void editorActionPerformed(ActionEvent e) {
            // pass the commit on to the picker.
            commit();
        }

        /**
         * Listening to actionEvents fired by the picker's monthView.
         * 
         * @param e
         */
        private void monthViewActionPerformed(ActionEvent e) {
            if (JXMonthView.CANCEL_KEY.equals(e.getActionCommand())) {
                cancel();
            } else if (JXMonthView.COMMIT_KEY.equals(e.getActionCommand())) {
                commit();
            }
        }

//------------------- focusListener
        
        /**
         * Issue #573-swingx - F2 in table doesn't focus the editor.
         * 
         * Do the same as combo: manually pass-on the focus to the editor.
         * 
         */
        @Override
        public void focusGained(FocusEvent e) {
            if (e.isTemporary()) {
                return;
            }
            popupRemover.load();
            if (e.getSource() == datePicker) {
               datePicker.getEditor().requestFocusInWindow(); 
            }
        }

        /**
         * #565-swingx: popup not hidden if clicked into combo.
         * The problem is that the combo uses the same trick as
         * this datepicker to prevent auto-closing of the popup
         * if focus is transfered back to the picker's editor.
         * 
         * The idea is to hide the popup manually when the
         * permanentFocusOwner changes to somewhere else.
         * 
         * JW: doesn't work - we only get the temporary lost,
         * but no permanent loss if the focus is transfered from 
         * the focusOwner to a new permanentFocusOwner.
         * 
         * OOOkaay ... looks like exclusively related to a combo:
         * we do get the expected focusLost if the focus is
         * transferred permanently from the temporary focusowner
         * to a new "normal" permanentFocusOwner (like a textfield),
         * we don't get it if transfered to a tricksing owner (like
         * a combo or picker). So can't do anything here. 
         * 
         * listen to keyboardFocusManager?
         */
        @Override
        public void focusLost(FocusEvent e) {
            
        }
    }

    public class PopupRemover implements PropertyChangeListener {

        private KeyboardFocusManager manager;
        private boolean loaded;
        
        public void load() {
            if (manager != KeyboardFocusManager.getCurrentKeyboardFocusManager()) {
                unload();
                manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
            }
            if (!loaded) {
                manager.addPropertyChangeListener("permanentFocusOwner", this);
                loaded = true;
            }
        }
        
        /**
         * @param b
         */
        private void unload(boolean nullManager) {
            if (manager != null) {
                manager.removePropertyChangeListener("permanentFocusOwner", this);
                if (nullManager) {
                    manager = null;
                }
            }
            loaded = false;
         }

        public void unload() {
            unload(true);
        }
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (!isPopupVisible()) {
                unload(false);
                return;
            }
            Component comp = manager.getPermanentFocusOwner();
            if ((comp != null) && !SwingXUtilities.isDescendingFrom(comp, datePicker)) {
                 unload(false);
                // on hiding the popup the focusmanager transfers 
                // focus back to the old permanentFocusOwner
                // before showing the popup, that is the picker
                // or the editor. So we have to force it back ... 
                hidePopup();
                comp.requestFocusInWindow();
                // this has no effect as focus changes are asynchronous
//                inHide = false;
            }
        }
        
        
    }

    
//  ------------------ listener creation

    /**
     * Creates and returns the property change listener for the 
     * picker's monthView
     * @return the listener for monthView properties
     */
    protected PropertyChangeListener createMonthViewPropertyListener() {
        return getHandler();
    }

    /**
     * Creates and returns the focuslistener for picker and editor.
     * @return the focusListener
     */
    protected FocusListener createFocusListener() {
        return getHandler();
    }


    /**
     * Creates and returns the ActionListener for the picker's editor.
     * @return the Actionlistener for the editor.
     */
    protected ActionListener createEditorActionListener() {
        return getHandler();
    }
   
    /**
     * Creates and returns the ActionListener for the picker's monthView.
     * 
     * @return the Actionlistener for the monthView.
     */
    protected ActionListener createMonthViewActionListener() {
        return getHandler();
    }

/**
     * Returns the listener for the dateSelection.
     * 
     * @return the date selection listener
     */
    protected DateSelectionListener createMonthViewSelectionListener() {
        return getHandler();
    }

    /**
     * @return a propertyChangeListener listening to 
     *    editor property changes
     */
    protected PropertyChangeListener createEditorPropertyListener() {
        return getHandler();
    }

    /**
     * Lazily creates and returns the shared all-mighty listener of everything
     *
     * @return the shared listener.
     */
    private Handler getHandler() {
        if (handler == null) {
            handler = new Handler();
        }
        return handler;
    }

    protected PropertyChangeListener createPropertyChangeListener() {
        return getHandler();
    }

    protected LayoutManager createLayoutManager() {
        return getHandler();
    }

    protected MouseListener createMouseListener() {
        return getHandler();
    }

    protected MouseMotionListener createMouseMotionListener() {
        return getHandler();
    }

    
//------------ utility methods

    private Object createDoNotCancelPopupClientProperty() {
        // this is a trick to get hold of the client prop which
        // prevents closing of the popup
        JComboBox box = new JComboBox();
        return box.getClientProperty("doNotCancelPopup");
    }
}
