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
/*
 * EditablePropertyDisplayer.java
 *
 * Created on 18 October 2003, 19:06
 */
package org.openide.explorer.propertysheet;

import org.openide.nodes.Node.Property;
import org.openide.util.NbBundle;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;

import java.beans.FeatureDescriptor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;


import java.util.EventObject;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import javax.swing.table.TableCellEditor;
import javax.swing.text.JTextComponent;
import org.openide.util.Exceptions;


/** Extends EditorPropertyDisplayer to implement editor logic, listening for
 * changes, updating properties, etc.
 *
 * @author  Tim Boudreau
 */
class EditablePropertyDisplayer extends EditorPropertyDisplayer implements PropertyDisplayer_Editable {
    private static final Object NO_VALUE = new Object();
    private int updatePolicy = UPDATE_ON_CONFIRMATION;
    private String actionCommand = "enterPressed"; //NOI18N
    private EnvListener envListener = null;
    private javax.swing.event.EventListenerList listenerList = null;
    private int actionListenerCount = 0;
    private InplaceEditorListener ieListener = null;
    private Object cachedInitialValue = NO_VALUE;
    private Action customEditorAction = null;
    boolean customEditorIsOpening = false;
    private PropertyEditor editor = null;
    private PropertyEnv attachedEnv = null;
    private Object lastKnownState = null;

    //Some property panel specific, package private hacks
    private PropertyChangeListener remoteEnvListener = null;
    private VetoableChangeListener remotevEnvListener = null;

    /** Creates a new instance of EditablePropertyDisplayer */
    public EditablePropertyDisplayer(Property p) {
        super(p, null);
    }

    EditablePropertyDisplayer(Property p, PropertyModel mdl) {
        super(p, mdl);
    }

    public void setEnabled(boolean b) {
        super.setEnabled(b);

        if (customEditorAction != null) {
            customEditorAction.setEnabled(b);
        }
    }

    public boolean commit() throws IllegalArgumentException {
        boolean result;

        try {
            result = _commit();
        } catch (IllegalArgumentException iae) {
            result = false;

            if (getUpdatePolicy() != UPDATE_ON_EXPLICIT_REQUEST) {
                PropertyDialogManager.notify(iae);
            } else {
                throw iae;
            }
        }

        return result;
    }

    private boolean _commit() throws IllegalArgumentException {
        //        System.err.println("Commit on " + getProperty().getDisplayName() + " value will be " + getInplaceEditor().getValue());

        //Hold the reference and don't call getInplaceEditor() again during this
        //method - it can trigger a vetoable property change from the property
        //env which would trigger prematurely replacing the inner component
        InplaceEditor ine = getInplaceEditor();

        PropertyEditor editor = (ine == null) ? PropUtils.getPropertyEditor(getProperty()) : ine.getPropertyEditor();

        //Cache the state of the property env
        PropertyEnv env = getPropertyEnv();

        //A temporary instance of PropertyEnv we'll attach to check for state
        //changes without triggering internal component changes
        PropertyEnv tempEnv = null;

        if (env != null) {
            //we want to ignore any events here
            tempEnv = new PropertyEnv();
            detachFromEnv(env);

            //Must set the feature descriptor, and it must be the real underlying
            //feature descriptor in case the property editor will cast the
            //result of env.getFeatureDescriptor()
            tempEnv.setFeatureDescriptor(findFeatureDescriptor(this));

            if (editor instanceof ExPropertyEditor) {
                //Make sure the editor will not talk to our property env
                ((ExPropertyEditor) editor).attachEnv(tempEnv);
            }
        }

        //our result variable
        boolean success = false;

        //        System.err.println("UPDATING THE PROPERTY EDITOR for " + getProperty().getDisplayName() + " to " + getEnteredValue());
        try {
            //First, try to put what the user has entered into the property
            //editor.  updatePropertyEditor will try setAsText if the value
            //object is a string, and setValue if it is not
            Object result = PropUtils.updatePropertyEditor(getPropertyEditor(), getEnteredValue());

            if (
                (result == null) && editor instanceof ExPropertyEditor &&
                    PropertyEnv.STATE_NEEDS_VALIDATION.equals(tempEnv.getState())
            ) {
                //Give other listeners on the propertyenv a chance to veto the
                //change
                String msg = tempEnv.silentlySetState(env.STATE_VALID, getEnteredValue());

                //something vetoed the change
                if ((msg != null) && !PropertyEnv.STATE_VALID.equals(env.getState())) {
                    IllegalArgumentException exc = new IllegalArgumentException("Error setting value"); //NOI18N
                    Exceptions.attachLocalizedMessage(exc, msg);
                    throw exc;
                }
            }

            //            System.err.println(" Really updating the property " + getProperty().getDisplayName() + " to " + editor.getValue());
            //If the result is non null, it as an exception thrown in setAsText.
            //Now try to write the value into the property.  The result will be
            //Boolean.TRUE if it is updated, Boolean.FALSE if the value was the
            //same as the property value, or an exception  that was thrown.
            if (result == null) {
                result = PropUtils.noDlgUpdateProp(ine.getPropertyModel(), editor);
            }

            //            System.err.println(" result is " + result);
            //Process the exception, if any
            if (result instanceof Exception) {
                //Okay, something went wrong
                Exception e = (Exception) result;

                //We will return it if it's an IAE, or wrap it in one
                IllegalArgumentException iae;

                if (e instanceof IllegalArgumentException) {
                    iae = (IllegalArgumentException) e;
                } else {
                    //Wrap it in an iae and use the localized message from the
                    //real exception
                    String msg = PropUtils.findLocalizedMessage(e, getEnteredValue(), getProperty().getDisplayName());

                    iae = new IllegalArgumentException(msg);
                    Exceptions.attachMessage(iae, "Cannot set value to " + getEnteredValue()); //NOI18N
                    Exceptions.attachLocalizedMessage(iae, msg); 

                    /*                    if (e instanceof InvocationTargetException || e instanceof IllegalAccessException) {
                                            ErrorManager.getDefault().notify(e);
                                        }
                     */
                    throw iae;
                }

                try {
                    //restore a good value so the env will have the correct state
                    editor.setValue(getProperty().getValue());
                } catch (Exception ex) {
                    //do nothing
                }

                throw iae;
            }

            success = Boolean.TRUE.equals(result);

            if (success) {
                fireStateChanged();
            } else {
                InplaceEditor ed = getInplaceEditor();

                //#43980 - if change causes the component to be synchronously
                //hidden, we may have disposed our state before we get here.
                if (ed != null) {
                    getInplaceEditor().reset();
                }
            }

            return success;
        } finally {

            if ((env != null) && (editor != null)) {
                attachToEnv(env);

                if (editor instanceof ExPropertyEditor) {
                    ((ExPropertyEditor) editor).attachEnv(env);
                }
            }
        }
    }

    private void cancelEditor() {
        if (getInplaceEditor() != null) {
            java.awt.Container parent = getParent();
            while (parent != null && !(parent instanceof javax.swing.JTable)) {
                parent = parent.getParent();
            }
            if (parent != null) {
                TableCellEditor tce = ((javax.swing.JTable) parent).getCellEditor();
                if (tce != null) {
                    tce.cancelCellEditing();
                }
            }
        }
    }

    public Object getEnteredValue() {
        Object result;

        if (getInplaceEditor() != null) {
            result = getInplaceEditor().getValue();
        } else {
            if (cachedInitialValue != NO_VALUE) {
                result = cachedInitialValue;
            } else {
                PropertyEditor ed = PropUtils.getPropertyEditor(getProperty());

                try {
                    result = ed.getAsText();
                } catch (ProxyNode.DifferentValuesException dve) {
                    result = null;
                }
            }
        }

        return result;
    }

    PropertyEditor getPropertyEditor() { //package private for unit tests

        PropertyEditor result;

        if (editor != null) {
            return editor;
        }

        if (getInplaceEditor() != null) {
            result = getInplaceEditor().getPropertyEditor();
        } else {
            result = PropUtils.getPropertyEditor(getProperty());
        }

        editor = result;

        return result;
    }

    public String isModifiedValueLegal() {
        //Fetch the editor - we don't want any events triggered (none should
        //be) to rip it out from under us
        PropertyEditor editor = getPropertyEditor();

        //A new property env we'll create and use for checking the state
        PropertyEnv env = null;

        //Get the new value we'll test
        Object newValue = getEnteredValue();

        //Get the current property env we're using to test things
        PropertyEnv myEnv = getPropertyEnv();

        //To hold the exception that might be thrown
        Exception exception = null;

        //To hold the env state, for comparing with the state when the value
        //is the one really held by the property
        Object envState = null;

        if ((myEnv != null) && PropertyEnv.STATE_NEEDS_VALIDATION.equals(myEnv.getState())) {
            String msg = myEnv.silentlySetState(myEnv.STATE_VALID, newValue);

            //something vetoed the change
            if ((msg != null) && !PropertyEnv.STATE_VALID.equals(myEnv.getState())) {
                return msg;
            }
        }

        try {
            //If it's an ExPropertyEditor, we also want to see if the env state
            //will be STATE_VALID, so create an env and attach it
            if (editor instanceof ExPropertyEditor) {
                if (myEnv != null) {
                    detachFromEnv(myEnv);
                }

                env = new PropertyEnv();
                env.setFeatureDescriptor(findFeatureDescriptor(this));
                ((ExPropertyEditor) editor).attachEnv(env);
            }

            //Put the entered value into the property editor, and fetch the
            //exception thrown, if any
            exception = PropUtils.updatePropertyEditor(editor, newValue);

            //check the state
            envState = (env == null) ? null : env.getState();
        } finally {
            //Reattach the env we're listening on
            if (editor instanceof ExPropertyEditor && (myEnv != null)) {
                //put things back the way they were
                try {
                    editor.setValue(getProperty().getValue());
                } catch (Exception e) {
                    //well, we can't solve everything
                    Logger.getLogger(EditablePropertyDisplayer.class.getName()).log(Level.WARNING, null, e);
                }

                //Now attach the env back to the property editor, so it will
                //get notified of state changes
                ((ExPropertyEditor) editor).attachEnv(myEnv);

                //And attach our listeners back to the env
                attachToEnv(myEnv);
            }
        }

        String result = null;

        if (exception != null) {
            //find the localized exception to return
            result = PropUtils.findLocalizedMessage(exception, getEnteredValue(), getProperty().getDisplayName());
        } else if (PropertyEnv.STATE_INVALID.equals(envState)) {
            //create a generic message if state is invalid but we don't know why
            result = NbBundle.getMessage(
                    EditablePropertyDisplayer.class, "FMT_CannotUpdateProperty", newValue,getProperty().getDisplayName()); //NOI18N
        }

        return result;
    }

    public boolean isValueModified() {
        boolean result = false;
        PropertyEditor peditor = getPropertyEditor();

        Object enteredValue = getEnteredValue();
        Object realValue = null;

        //Get the value from the editor to make sure getAsText() does not lie
        Object editorValue = null;

        try {
            editorValue = peditor.getValue();
        } catch (ProxyNode.DifferentValuesException dve) {
            return false;
        }

        //some editors provide a single from getTags()
        //but the value is null by default
        if ((enteredValue == null) != (editorValue == null)) {
            return true;
        }

        if (realValue == null) {
            //try to check the editor value if the editor does not support
            //getAsText
            realValue = editorValue;
        }

        if ((realValue == null) != (enteredValue == null)) {
            result = true;
        } else if (realValue == enteredValue) {
            result = false;
        } else if (realValue != null) {
            result = !realValue.equals(enteredValue);
        } else {
            result = false;
        }

        return result;
    }

    public void reset() {
        if (getInplaceEditor() != null) {
            getInplaceEditor().reset();
        }
    }

    public void setEnteredValue(Object o) {
        if (getInplaceEditor() != null) {
            getInplaceEditor().setValue(o);
        } else {
            storeCachedInitialValue(o);
        }
    }

    protected void setPropertyEnv(PropertyEnv env) {
        if (getPropertyEnv() != null) {
            detachFromEnv(getPropertyEnv());
        }

        super.setPropertyEnv(env);

        if (env != null) {
            env.setChangeImmediate(getUpdatePolicy() != UPDATE_ON_EXPLICIT_REQUEST);
            attachToEnv(getPropertyEnv());
        }
    }

    protected void setInplaceEditor(InplaceEditor ed) {
        if (getInplaceEditor() != null) {
            detachFromInplaceEditor(getInplaceEditor());
        }

        super.setInplaceEditor(ed);

        if ((ed == null) && (getPropertyEnv() != null)) {
            detachFromEnv(getPropertyEnv());
        }

        if (getInplaceEditor() != null) {
            attachToInplaceEditor(getInplaceEditor());
        }
    }

    public int getUpdatePolicy() {
        return updatePolicy;
    }

    public void setUpdatePolicy(int i) {
        if ((i != UPDATE_ON_FOCUS_LOST) && (i != UPDATE_ON_EXPLICIT_REQUEST) && (i != UPDATE_ON_CONFIRMATION)) {
            throw new IllegalArgumentException("Bad update policy: " + i); //NOI18N
        }

        updatePolicy = i;

        PropertyEnv env = getPropertyEnv();

        if (env != null) {
            env.setChangeImmediate(i != UPDATE_ON_EXPLICIT_REQUEST);
        }
    }

    /** Transmits escape sequence to dialog */
    private void trySendEscToDialog() {
        if (isTableUI()) {
            //let the table decide, don't be preemptive
            return;
        }

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

            InputMap imp = getRootPane().getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            ActionMap am = getRootPane().getActionMap();

            KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
            Object key = imp.get(escape);

            if (key != null) {
                Action a = am.get(key);

                if (a != null) {
                    if (Boolean.getBoolean("netbeans.proppanel.logDialogActions")) { //NOI18N
                        System.err.println("Action bound to escape key is " + a); //NOI18N
                    }

                    String commandKey = (String) a.getValue(Action.ACTION_COMMAND_KEY);

                    if (commandKey == null) {
                        commandKey = "cancel"; //NOI18N
                    }

                    a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, commandKey)); //NOI18N
                }
            }
        }
    }

    private void trySendEnterToDialog() {
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

            JRootPane jrp = getRootPane();

            if (jrp != null) {
                JButton b = jrp.getDefaultButton();

                if ((b != null) && b.isEnabled()) {
                    b.doClick();
                }
            }
        }
    }

    private void attachToEnv(PropertyEnv env) {
        if (attachedEnv == env) {
            return;
        }

        //        System.err.println("  attachToEnv - " + env);
        env.addVetoableChangeListener(getEnvListener());
        env.addPropertyChangeListener(getEnvListener());
        env.setBeans(findBeans(this));
    }

    private void detachFromEnv(PropertyEnv env) {
        //        System.err.println("  detachFromEnv - " + env);
        env.removeVetoableChangeListener(getEnvListener());
        env.addPropertyChangeListener(getEnvListener());
        env.setBeans(null);
        attachedEnv = null;
    }

    private void attachToInplaceEditor(InplaceEditor ed) {
        Object o = fetchCachedInitialValue();

        if (o != NO_VALUE) {
            ed.setValue(o);
        }

        ed.addActionListener(getInplaceEditorListener());
        ed.getComponent().addFocusListener(getInplaceEditorListener());
    }

    private void detachFromInplaceEditor(InplaceEditor ed) {
        ed.removeActionListener(getInplaceEditorListener());
        ed.getComponent().removeFocusListener(getInplaceEditorListener());
    }

    private void storeCachedInitialValue(Object o) {
        cachedInitialValue = o;
    }

    private Object fetchCachedInitialValue() {
        Object result = cachedInitialValue;
        cachedInitialValue = NO_VALUE;

        return result;
    }

    private InplaceEditorListener getInplaceEditorListener() {
        if (ieListener == null) {
            ieListener = new InplaceEditorListener();
        }

        return ieListener;
    }

    private EnvListener getEnvListener() {
        if (envListener == null) {
            envListener = new EnvListener();
        }

        return envListener;
    }

    private boolean hasActionListeners() {
        return actionListenerCount > 0;
    }

    /**
     * Registers ActionListener to receive events.
     * @param listener The listener to register.
     */
    public synchronized void addActionListener(ActionListener listener) {
        if (listenerList == null) {
            listenerList = new EventListenerList();
        }

        listenerList.add(ActionListener.class, listener);
        actionListenerCount++;
    }

    /**
     * Removes ActionListener from the list of listeners.
     * @param listener The listener to remove.
     */
    public synchronized void removeActionListener(ActionListener listener) {
        listenerList.remove(ActionListener.class, listener);
        actionListenerCount = Math.max(0, actionListenerCount--);
    }

    /**
     * Notifies all registered listeners about the event.
     *
     * @param event The event to be fired
     */
    private void fireActionPerformed(boolean committed) {
        if (listenerList == null) {
            return;
        }

        CellEditorActionEvent event = new CellEditorActionEvent(this,
                                                                ActionEvent.ACTION_PERFORMED,
                                                                getActionCommand(),
                                                                committed);

        Object[] listeners = listenerList.getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ActionListener.class) {
                ((ActionListener) listeners[i + 1]).actionPerformed(event);
            }
        }
    }

    /**
     * Registers ChangeListener to receive events.
     * @param listener The listener to register.
     */
    public synchronized void addChangeListener(ChangeListener listener) {
        if (listenerList == null) {
            listenerList = new EventListenerList();
        }

        listenerList.add(ChangeListener.class, listener);
    }

    /**
     * Removes ChangeListener from the list of listeners.
     * @param listener The listener to remove.
     */
    public synchronized void removeChangeListener(ChangeListener listener) {
        listenerList.remove(ChangeListener.class, listener);
    }

    /**
     * Notifies all registered listeners about the event.
     *
     * @param event The event to be fired
     */
    private void fireStateChanged() {
        if (listenerList == null) {
            return;
        }

        Object[] listeners = listenerList.getListenerList();
        ChangeEvent event = new ChangeEvent(this);

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ChangeListener.class) {
                ((ChangeListener) listeners[i + 1]).stateChanged(event);
            }
        }
    }

    public String getActionCommand() {
        return actionCommand;
    }

    public void setActionCommand(String val) {
        actionCommand = val;
    }

    private boolean shouldIgnoreFocusEvents() {
        return customEditorIsOpening || inReplaceInner;
    }

    protected void configureButtonPanel(ButtonPanel bp) {
        bp.setButtonAction(getCustomEditorAction());
    }

    Action getCustomEditorAction() {
        if (customEditorAction == null) {
            PropertyModel mdl = null;

            if (modelRef != null) {
                mdl = modelRef.get();
            }

            customEditorAction = new CustomEditorAction(new Invoker(), mdl);

            getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, KeyEvent.CTRL_DOWN_MASK, false), "invokeCustomEditor"
            ); //NOI18N

            //XXX this could be done lazily
            getActionMap().put("invokeCustomEditor", customEditorAction); //NOI18N

            //            System.err.println("Installed custom editor action");
        }

        return customEditorAction;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("Inline editor for property "); //NOI18N
        sb.append(getProperty().getDisplayName());
        sb.append(" = "); //NOI18N
        sb.append(getProperty());
        sb.append(" inplace editor="); //NOI18N
        sb.append(getInplaceEditor());

        return sb.toString();
    }

    void setRemoteEnvListener(PropertyChangeListener l) {
        remoteEnvListener = l;
    }

    void setRemoteEnvVetoListener(VetoableChangeListener vl) {
        remotevEnvListener = vl;
    }

    public synchronized void dispose() {
        setPropertyEnv(null);
        setInplaceEditor(null);
        remotevEnvListener = null;
        remoteEnvListener = null;
        cachedInitialValue = null;
        editor = null;
    }

    private class InplaceEditorListener implements ActionListener, FocusListener {
        public void actionPerformed(ActionEvent e) {
            //            System.err.println("\n\nGot action from inplace editor for " + getProperty().getDisplayName() + " - " + e.getActionCommand());
            //See if it was enter or escape
            boolean isSuccess = InplaceEditor.COMMAND_SUCCESS.equals(e.getActionCommand()) ||
                "comboBoxEdited".equals(e.getActionCommand()); //NOI18N

            //if the value should get updated, do something
            if (isSuccess) {
                boolean committed = false;
                if ((getUpdatePolicy() == UPDATE_ON_CONFIRMATION) || (getUpdatePolicy() == UPDATE_ON_FOCUS_LOST)) { //XXX needed by property panel, but breaks API def.  Fine while this is not API.
                    commit();
                    committed = true;
                }

                //JTextField style behavior - fire a change unless there are
                //action listeners attached
                if (hasActionListeners()) {
                    fireActionPerformed(committed);
                } else {
                    //Try to close the dialog, if any on enter - this method
                    //will make sure we're really processing an enter-key event
                    trySendEnterToDialog();
                }
            } else if (!hasActionListeners()) {
                //Try to close the dialog, if any, and if we're really processing
                //an escape key event
                trySendEscToDialog();
            }
        }

        public void focusGained(java.awt.event.FocusEvent e) {
            if (shouldIgnoreFocusEvents()) {
                return;
            }

            //            System.err.println("Focus gained by editor " + e.getComponent());
        }

        public void focusLost(java.awt.event.FocusEvent e) {
            //don't let spurious focus changes while replacing the inner component
            //trigger additional work
            if (shouldIgnoreFocusEvents()) {
                return;
            }

            if (
                !e.isTemporary() && (getUpdatePolicy() == UPDATE_ON_FOCUS_LOST) &&
                    !getInplaceEditor().isKnownComponent(e.getOppositeComponent()) && isValueModified()
            ) {
                commit();
            }
        }
    }

    private class Invoker implements CustomEditorAction.Invoker {
        boolean failed = false;

        public boolean allowInvoke() {
            return true;
        }

        public void editorClosed() {
            if (failed) {
                requestFocus();
            }

            customEditorIsOpening = false;
        }

        public void editorOpened() {
            customEditorIsOpening = false;
            repaint();
        }

        public void editorOpening() {
            customEditorIsOpening = true;
        }

        public void failed() {
            failed = true;

            if (getInplaceEditor() != null) {
                getInplaceEditor().reset();
            }
        }

        public String getBeanName() {
            if (modelRef != null) {
                PropertyModel pm = modelRef.get();

                if (pm instanceof NodePropertyModel) {
                    return ((NodePropertyModel) pm).getBeanName();
                }
            }

            if (getProperty() instanceof ModelProperty.DPMWrapper) {
                return ((ModelProperty.DPMWrapper) getProperty()).getBeanName();
            }

            return findFeatureDescriptor(EditablePropertyDisplayer.this).getDisplayName();
        }

        public java.awt.Component getCursorChangeComponent() {
            return EditablePropertyDisplayer.this;
        }

        public Object getPartialValue() {
            Object pvalue = getEnteredValue();
            cancelEditor();
            return pvalue;
        }

        public java.beans.FeatureDescriptor getSelection() {
            return getProperty();
        }

        public void valueChanged(PropertyEditor editor) {
            failed = false;

            try {
                //                System.err.println("ValueChanged - new value " + editor.getValue());
                if (getInplaceEditor() != null) {
                    setEnteredValue(getProperty().getValue());
                } else {
                    //Handle case where our parent PropertyPanel is no longer showing, but
                    //the custom editor we invoked still is.  Issue 38004
                    PropertyModel mdl = (modelRef != null) ? modelRef.get() : null;

                    if (mdl != null) {
                        FeatureDescriptor fd = null;

                        if (mdl instanceof ExPropertyModel) {
                            fd = ((ExPropertyModel) mdl).getFeatureDescriptor();
                        }

                        String title = null;

                        if (fd != null) {
                            title = fd.getDisplayName();
                        }

                        failed = PropUtils.updateProp(mdl, editor, title); //XXX
                    }
                }
            } catch (Exception e) {
                throw (IllegalStateException) new IllegalStateException("Problem setting entered value from custom editor").initCause(e);
            }
        }

        public boolean wantAllChanges() {
            return true;
        }

        public ReusablePropertyEnv getReusablePropertyEnv() {
            return EditablePropertyDisplayer.this.getReusablePropertyEnv();
        }
    }

    private class EnvListener implements VetoableChangeListener, PropertyChangeListener {
        private boolean wantNextChange = false;

        public void vetoableChange(PropertyChangeEvent evt)
        throws PropertyVetoException {
            //            if (evt.getSource() != attachedEnv) {
            //                return;
            //            }
            //            System.err.println("Got vetoable change: " + evt + " oldvalue=" + evt.getOldValue() + " newvalue=" + evt.getNewValue());
            if (PropertyEnv.PROP_STATE.equals(evt.getPropertyName())) {
                wantNextChange = ((evt.getNewValue() != getPropertyEnv().getState()) &&
                    (getPropertyEnv().getState() != null)) &&
                    ((evt.getNewValue() != PropertyEnv.STATE_NEEDS_VALIDATION) ||
                    ((evt.getNewValue() == PropertyEnv.STATE_NEEDS_VALIDATION) &&
                    (evt.getOldValue() == PropertyEnv.STATE_VALID)));
            }

            if (!inReplaceInner && (remotevEnvListener != null)) {
                remotevEnvListener.vetoableChange(evt);
            }
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (inReplaceInner) {
                //                wantNextChange=false;
                return;
            }

            if (
                wantNextChange ||
                    ((evt.getNewValue() == PropertyEnv.STATE_VALID) && (evt.getNewValue() != lastKnownState))
            ) {
                wantNextChange = false;
                replaceInner();
                lastKnownState = ((PropertyEnv) evt.getSource()).getState();
            }

            if (remoteEnvListener != null) {
                remoteEnvListener.propertyChange(evt);
            }
        }
    }
}
