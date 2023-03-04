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
package org.openide.explorer.propertysheet;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyEditor;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.*;
import javax.swing.event.AncestorListener;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.text.JTextComponent;
import org.openide.explorer.propertysheet.editors.EnhancedPropertyEditor;


/** A combo box inplace editor.  Does a couple of necessary things:
 * 1.  It does not allow the UI delegate to install a focus listener on
 * it - it will manage opening and closing the popup on its own - this
 * is to avoid a specific problem - that if the editor is moved to a
 * different cell and updated, the focus lost event will arrive after
 * it has been moved, and the UI delegate will try to close the popup
 * when it should be opening.  2.  Contains a replacement renderer for
 * use on GTK look and feel - on JDK 1.4.2, combo boxes do not respect
 * the value assigned by setBackground() (there is a fixme note about this
 * in SynthComboBoxUI, so presumably this will be fixed at some point).
 */
class ComboInplaceEditor extends JComboBox implements InplaceEditor, FocusListener, AncestorListener, IncrementPropertyValueSupport {
    /*Keystrokes this inplace editor wants to consume */
    static final KeyStroke[] cbKeyStrokes = new KeyStroke[] {
            KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, false), KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, false),
            KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, true), KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, true),
            KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0, false),
            KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0, false),
            KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0, true), KeyStroke.getKeyStroke(
                KeyEvent.VK_PAGE_UP, 0, true
            )
        };
    private static PopupChecker checker = null;
    protected PropertyEditor editor;
    protected PropertyEnv env;
    private ListCellRenderer originalRenderer;
    protected PropertyModel mdl;
    boolean inSetUI = false;
    private boolean tableUI;
    private boolean connecting = false;
    private boolean hasBeenEditable = false;
    private boolean needLayout = false;

    private boolean suppressFireActionEvent = false;
    private final boolean isAutoComplete;
    private boolean strictAutoCompleteMatching;

    /** Create a ComboInplaceEditor - the tableUI flag will tell it to use
     * less borders & such */
    public ComboInplaceEditor(boolean tableUI) {
        if (tableUI) {
            putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE); //NOI18N
        }

        if (Boolean.getBoolean("netbeans.ps.combohack")) { //NOI18N
            setLightWeightPopupEnabled(false);
        }

        if (getClass() == ComboInplaceEditor.class) {
            enableEvents(AWTEvent.FOCUS_EVENT_MASK);
        }

        this.tableUI = tableUI;

        if (tableUI) {
            updateUI();
        }

        originalRenderer = getRenderer();

        isAutoComplete = ComboBoxAutoCompleteSupport.install( this );
        String lafId = UIManager.getLookAndFeel().getID();
        if ("Aqua".equals(lafId) || "Metal".equals(lafId) ) { //NOI18N
            //#220163
            UIManager.put("PopupMenu.consumeEventOnClose", Boolean.TRUE); //NOI18N
        }
    }

    /** Overridden to add a listener to the editor if necessary, since the
     * UI won't do that for us without a focus listener */
    @Override
    public void addNotify() {
        super.addNotify();

        if (isEditable() && (getClass() == ComboInplaceEditor.class)) {
            getEditor().getEditorComponent().addFocusListener(this);
        }

        getLayout().layoutContainer(this);
    }

    @Override
    public void setEditable(boolean val) {
        boolean hadBeenEditable = hasBeenEditable;
        hasBeenEditable |= val;
        super.setEditable(val);

        if (hadBeenEditable != hasBeenEditable) {
            log("Combo editor for " + editor + " setEditable (" + val + ")");
            needLayout = true;
        }
    }

    /** Overridden to hide the popup and remove any listeners from the
     * combo editor */
    @Override
    public void removeNotify() {
        log("Combo editor for " + editor + " removeNotify forcing popup close");
        setPopupVisible(false);
        super.removeNotify();
        getEditor().getEditorComponent().removeFocusListener(this);
    }

    @Override
    public Insets getInsets() {
        if ("Aqua".equals(UIManager.getLookAndFeel().getID())) {
            return new Insets(0, 0, 0, 0);
        } else {
            return super.getInsets();
        }
    }

    @Override
    public void clear() {
        editor = null;
        env = null;
    }

    @Override
    public void connect(PropertyEditor pe, PropertyEnv env) {
        connecting = true;

        try {
            log("Combo editor connect to " + pe + " env=" + env);

            this.env = env;
            this.editor = pe;
            setModel(new DefaultComboBoxModel(pe.getTags()));

            boolean editable = (editor instanceof EnhancedPropertyEditor)
                ? ((EnhancedPropertyEditor) editor).supportsEditingTaggedValues()
                : ((env != null) && Boolean.TRUE.equals(env.getFeatureDescriptor().getValue("canEditAsText"))); //NOI18N
            boolean noAutoComplete = null != env && Boolean.FALSE.equals(env.getFeatureDescriptor().getValue("canAutoComplete")); //NOI18N

            strictAutoCompleteMatching = !editable;
            setEditable(editable || (isAutoComplete && !noAutoComplete));
            setActionCommand(COMMAND_SUCCESS);

            //Support for custom ListCellRenderer injection via PropertyEnv
            //The instance obtained from the env by the "customListCellRendererSupport" key
            //must both implement ListCellRenderer and extend AtomicReference<ListCellRenderer>
            //The AtomicReference workaround it necessary since we somehow need to put
            //reference to the original ListCellRenderer to the custom one.
            Object customRendererSupport = env.getFeatureDescriptor().getValue("customListCellRendererSupport"); //NOI18N
            if(customRendererSupport != null) {
                //set the actual renrerer to the custom one so it may delegate
                AtomicReference<ListCellRenderer> ref = (AtomicReference<ListCellRenderer>)customRendererSupport;
                ref.set(originalRenderer);
                setRenderer((ListCellRenderer)customRendererSupport);
            }

            if(PropUtils.supportsValueIncrement(env)) {
                PropUtils.wrapUpDownArrowActions(this, this);
                PropUtils.wrapUpDownArrowActions(((JComponent)getEditor().getEditorComponent()), this);
            }

            reset();
        } finally {
            connecting = false;
        }
    }

    private void log(String s) {
        if (PropUtils.isLoggable(ComboInplaceEditor.class) && (getClass() == ComboInplaceEditor.class)) {
            PropUtils.log(ComboInplaceEditor.class, s); //NOI18N
        }
    }

    /**
     * Prevent the "autocomplete decorated" combobox to call setSelectedItem with empty
     * value when one explicitly call InlineEditor.setValue(...)
     */
    private boolean in_setSelectedItem = false;

    @Override
    public void setSelectedItem(Object o) {
        try {
            if(in_setSelectedItem) {
                in_setSelectedItem = false;
                if(PropUtils.supportsValueIncrement(env)) {
                    //return only when we are in the hack mode
                    return ;
                }
            }

            in_setSelectedItem = true;

            //Some property editors (i.e. IMT's choice editor) treat
            //null as 0.  Probably not the right way to do it, but needs to
            //be handled.
            if ((o == null) && (editor != null) && (editor.getTags() != null) && (editor.getTags().length > 0)) {
                o = editor.getTags()[0];
            }

            if (o != null) {
                super.setSelectedItem(o);
            }
        } finally {
            in_setSelectedItem = false;
        }
    }

    /** Overridden to not fire changes is an event is called inside the
     * connect method */
    @Override
    public void fireActionEvent() {
        if (connecting || (editor == null)) {
            return;
        } else {
            if (editor == null) {
                return;
            }

            if( suppressFireActionEvent ) {
                return;
            }

            if ("comboBoxEdited".equals(getActionCommand())) {
                log("Translating comboBoxEdited action command to COMMAND_SUCCESS");
                setActionCommand(COMMAND_SUCCESS);
            }

            log("Combo editor firing ActionPerformed command=" + getActionCommand());
            super.fireActionEvent();
        }
    }

    @Override
    public void reset() {
        String targetValue = null;

        if (editor != null) {
            log("Combo editor reset setting selected item to " + editor.getAsText());
            targetValue = editor.getAsText();

            //issue 26367, form editor needs ability to set a custom value
            //when editing is initiated (event handler combos, part of them
            //cleaning up their EnhancedPropertyEditors).
        }

        if ((getClass() == ComboInplaceEditor.class) && (env != null) && (env.getFeatureDescriptor() != null)) {
            String initialEditValue = (String) env.getFeatureDescriptor().getValue("initialEditValue"); //NOI18N

            if (initialEditValue != null) {
                targetValue = initialEditValue;
            }
        }

        setSelectedItem(targetValue);
    }

    @Override
    public Object getValue() {
        if (isEditable()) {
            if( isAutoComplete ) {
                Object editorItem = getEditor().getItem();
                if( null != editorItem ) {
                    int selItem = ComboBoxAutoCompleteSupport.findMatch( this, editorItem.toString() );
                    if( selItem >= 0 && selItem < getItemCount() )
                        return getItemAt( selItem );
                    if( strictAutoCompleteMatching ) {
                        int selIndex = getSelectedIndex();
                        if( selIndex < 0 ) {
                            if( null != editor )
                                return editor.getAsText();
                            return null;
                        }
                        return getSelectedItem();
                    }
                }
            }
            return getEditor().getItem();
        } else {
            return getSelectedItem();
        }
    }

    @Override
    public PropertyEditor getPropertyEditor() {
        return editor;
    }

    @Override
    public PropertyModel getPropertyModel() {
        return mdl;
    }

    @Override
    public void setPropertyModel(PropertyModel pm) {
        log("Combo editor set property model to " + pm);
        this.mdl = pm;
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public KeyStroke[] getKeyStrokes() {
        return cbKeyStrokes;
    }

    /** Overridden to use CleanComboUI on Metal L&F to avoid extra borders */
    @Override
    public void updateUI() {
        LookAndFeel lf = UIManager.getLookAndFeel();
        String id = lf.getID();
        boolean useClean = tableUI && (lf instanceof MetalLookAndFeel
                || "GTK".equals(id) //NOI18N
                || "Nimbus".equals(id) //NOI18N
                || ("Aqua".equals(id) && checkMacSystemVersion()) // NOI18N
                || PropUtils.isWindowsVistaLaF() //#217957
                || "Kunststoff".equals(id)); //NOI18N

        if (useClean) {
            super.setUI(PropUtils.createComboUI(this, tableUI));
        } else {
            super.updateUI();
        }

        if (tableUI & getEditor().getEditorComponent() instanceof JComponent) {
            ((JComponent) getEditor().getEditorComponent()).setBorder(null);
        }
    }

    private static Boolean syscheck = null;

    static boolean checkMacSystemVersion() {
        // Check that the system version is higher than "10.5":
        int majv = 10;
        int minv = 5;
        if (syscheck == null) {
            String version = System.getProperty("os.version");  //NOI18N
            int d1 = version.indexOf('.');
            if (d1 > 0) {
                int d2 = version.indexOf('.', d1+1);
                if (d2 < 0) {
                    d2 = version.length();
                }
                try {
                    int m1 = Integer.parseInt(version.substring(0, d1));
                    int m2 = Integer.parseInt(version.substring(d1+1, d2));
                    syscheck = m1 > majv || m1 == majv && m2 >= minv;
                } catch (NumberFormatException nfex) {
                    syscheck = false;
                }
            } else {
                syscheck = false;
            }
        }
        return syscheck;
    }

    /** Overridden to set a flag used to block the UI from adding a focus
     * listener, and to use an alternate renderer class on GTK look and feel
     * to work around a painting bug in SynthComboUI (colors not set correctly)*/
    @Override
    public void setUI(ComboBoxUI ui) {
        inSetUI = true;

        try {
            super.setUI(ui);
        } finally {
            inSetUI = false;
        }
    }

    /** Overridden to handle a corner case - an NPE if the UI tries to display
     * the popup, but the combo box is removed from the parent before that can
     * happen - only happens on very rapid clicks between popups */
    @Override
    public void showPopup() {
        try {
            log(" Combo editor show popup");
            super.showPopup();
        } catch (NullPointerException e) {
            //An inevitable consequence - the look and feel will queue display
            //of the popup, but it can be processed after the combo box is
            //offscreen
            log(" Combo editor show popup later due to npe");

            SwingUtilities.invokeLater(
                new Runnable() {
                    @Override
                    public void run() {
                        ComboInplaceEditor.super.showPopup();
                    }
                }
            );
        }
    }

    private void prepareEditor() {
        Component c = getEditor().getEditorComponent();

        if (c instanceof JTextComponent) {
            JTextComponent jtc = (JTextComponent) c;
            String s = jtc.getText();

            if ((s != null) && (s.length() > 0)) {
                jtc.setSelectionStart(0);
                jtc.setSelectionEnd(s.length());
            }

            if (tableUI) {
                jtc.setBackground(getBackground());
            } else {
                jtc.setBackground(PropUtils.getTextFieldBackground());
            }
            if( tableUI )
                jtc.requestFocus();
        }

        if (getLayout() != null) {
            getLayout().layoutContainer(this);
        }

        repaint();
    }

    /** Overridden to do the focus-popup handling that would normally be done
     * by the look and feel */
    @Override
    public void processFocusEvent(FocusEvent fe) {
        if ((fe.getID() == FocusEvent.FOCUS_LOST) &&
            fe.getOppositeComponent() == getEditor().getEditorComponent() &&
            isPopupVisible()) {

            return ; // If the popup is visible and the focus is transferred to the editor component,
                     // ignore the event - it would close the popup.
        }
        super.processFocusEvent(fe);

        if (PropUtils.isLoggable(ComboInplaceEditor.class)) {
            PropUtils.log(ComboInplaceEditor.class, "Focus event on combo " + "editor"); //NOI18N
            PropUtils.log(ComboInplaceEditor.class, fe);
        }

        Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();

        if (isDisplayable() && (fe.getID() == FocusEvent.FOCUS_GAINED) && (focusOwner == this) && !isPopupVisible()) {
            if (isEditable()) {
                prepareEditor();

                if( tableUI )
                    SwingUtilities.invokeLater(new PopupChecker());
            } else {
                if (tableUI) {
                    if( null == env || !PropUtils.supportsValueIncrement( env ) ) {
                        showPopup();

                        //Try to beat the event mis-ordering at its own game
                        SwingUtilities.invokeLater(new PopupChecker());
                    }
                }
            }

            repaint();
        } else if ((fe.getID() == FocusEvent.FOCUS_LOST) && isPopupVisible() && !isDisplayable()) {
            if (!PropUtils.psCommitOnFocusLoss) {
                setActionCommand(COMMAND_FAILURE);
                fireActionEvent();
            }

            //We were removed, but we may be immediately added. See if that's the
            //case after other queued events run
            SwingUtilities.invokeLater(
                new Runnable() {
                    @Override
                    public void run() {
                        if (!isDisplayable()) {
                            hidePopup();
                        }
                    }
                }
            );
        }

        repaint();
    }

    @Override
    public boolean isKnownComponent(Component c) {
        return (c == getEditor().getEditorComponent());
    }

    @Override
    public void setValue(Object o) {
        ComboBoxAutoCompleteSupport.setIgnoreSelectionEvents( this, true );
        setSelectedItem(o);
        ComboBoxAutoCompleteSupport.setIgnoreSelectionEvents( this, false );
    }

    /** Returns true if the combo box is editable */
    @Override
    public boolean supportsTextEntry() {
        return isEditable();
    }

    /** Overridden to install an ancestor listener which will ensure the
     * popup is always opened correctly */
    @Override
    protected void installAncestorListener() {
        //Use a replacement which will check to ensure the popup is
        //displayed
        if (tableUI) {
            addAncestorListener(this);
        } else {
            super.installAncestorListener();
        }
    }

    /** Overridden to block the UI from adding its own focus listener, which
     * will close the popup at the wrong times.  We will manage focus
     * ourselves instead */
    @Override
    public void addFocusListener(FocusListener fl) {
        if (!inSetUI || !tableUI) {
            super.addFocusListener(fl);
        }
    }

    @Override
    public void focusGained(FocusEvent e) {
        //do nothing
        prepareEditor();
        if( null == env || !PropUtils.supportsValueIncrement( env ) )
            showPopup();
    }

    /** If the editor loses focus, we're done editing - fire COMMAND_FAILURE */
    @Override
    public void focusLost(FocusEvent e) {
        Component c = e.getOppositeComponent();

        if (!isAncestorOf(c) && (c != getEditor().getEditorComponent())) {
            if ((c == this) || (c instanceof SheetTable && ((SheetTable) c).isAncestorOf(this))) {
                //workaround for issue 38029 - editable combo editor can lose focus to ...itself
                return;
            }

            setActionCommand(COMMAND_FAILURE);
            log(" Combo editor lost focus - setting action command to " + COMMAND_FAILURE);
            getEditor().getEditorComponent().removeFocusListener(this);

            if (checker == null) {
                log("No active popup checker, firing action event");
                fireActionEvent();
            }
        }
    }

    /** Overridden to ensure the editor gets focus if editable */
    @Override
    public void firePopupMenuCanceled() {
        super.firePopupMenuCanceled();

        if (isEditable()) {
            Component focus = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();

            if (isDisplayable() && (focus == this)) {
                log("combo editor popup menu canceled.  Requesting focus on editor component");
                getEditor().getEditorComponent().requestFocus();
            }
        }
    }

    /** Overridden to fire COMMAND_FAILURE on Escape */
    @Override
    public void processKeyEvent(KeyEvent ke) {
        super.processKeyEvent(ke);

        if ((ke.getID() == KeyEvent.KEY_PRESSED) && (ke.getKeyCode() == KeyEvent.VK_ESCAPE)) {
            setActionCommand(COMMAND_FAILURE);
            fireActionEvent();
        }
    }

    @Override
    public void ancestorAdded(javax.swing.event.AncestorEvent event) {
        //This is where we typically have a problem with popups not showing,
        //and below is the cure... Problem is that the popup is hidden
        //because the combo's ancestor is changed (even though we blocked
        //the normal ancestor listener from being added)
        checker = new PopupChecker();
        SwingUtilities.invokeLater(checker);
    }

    @Override
    public void ancestorMoved(javax.swing.event.AncestorEvent event) {
        //do nothing
        if (needLayout && (getLayout() != null)) {
            getLayout().layoutContainer(this);
        }
    }

    @Override
    public void ancestorRemoved(javax.swing.event.AncestorEvent event) {
        //do nothing
    }

    @Override
    public void paintChildren(Graphics g) {
        if ((editor != null) && !hasFocus() && editor.isPaintable()) {
            return;
        } else {
            super.paintChildren(g);
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        //For property panel usage, allow the editor to paint
        if ((editor != null) && !hasFocus() && editor.isPaintable()) {
            Insets ins = getInsets();
            Color c = g.getColor();

            try {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
            } finally {
                g.setColor(c);
            }

            ins.left += PropUtils.getTextMargin();
            editor.paintValue(
                g,
                new Rectangle(
                    ins.left, ins.top, getWidth() - (ins.right + ins.left), getHeight() - (ins.top + ins.bottom)
                )
            );
        } else {
            super.paintComponent(g);
        }
    }

    @Override
    public boolean incrementValue() {
        return setNextValue(true);
    }

    @Override
    public boolean decrementValue() {
        return setNextValue(false);
    }

    private boolean setNextValue( boolean increment ) {
        try {
            suppressFireActionEvent = true;
            if( isPopupVisible() ) {
                return false;
            }
            if( !PropUtils.supportsValueIncrement( env ) )
                return false;

            Object nextValue = PropUtils.getNextValue( env, increment );
            if( null == nextValue )
                return true;

            setValue( nextValue );

            return PropUtils.updateProp( this );
        } finally {
            suppressFireActionEvent = false;
        }
    }

    @Override
    public boolean isIncrementEnabled() {
        return !isPopupVisible();
    }

    /** A handy runnable which will ensure the popup is really displayed */
    private class PopupChecker implements Runnable {
        @Override
        public void run() {
            Window w = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();

            //in Java 1.5+ KeyboardFocusManager.getActiveWindow() may return null
            if (null != w && w.isAncestorOf(ComboInplaceEditor.this)) {
                if (isShowing() && !isPopupVisible()) {
                    log("Popup checker ensuring editor prepared or popup visible");

                    if (isEditable()) {
                        prepareEditor();
                    }
                    showPopup();
                }

                checker = null;
            }
        }
    }
}
