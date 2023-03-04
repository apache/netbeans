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

import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;
import org.openide.nodes.Node.*;
import org.openide.util.NbBundle;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;

import java.lang.reflect.InvocationTargetException;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.event.*;
import org.openide.util.Exceptions;

/** An implementation of PropertyDisplayer.EDITABLE which manages communication
 * with a custom editor, to replace that aspect of PropertyPanel's behavior.
 *
 * @author  Tim Boudreau
 */
final class CustomEditorDisplayer implements PropertyDisplayer_Editable {
    private int updatePolicy = UPDATE_ON_CONFIRMATION;
    private Property prop;
    private PropertyEnv env = null;
    private PropertyEditor editor = null;
    private Component customEditor = null;
    boolean ignoreChanges = false;
    private PropertyChangeListener editorListener = null;
    private EnvListener envListener = null;
    private PropertyModel model = null;
    private Object originalValue = null;

    /**
     * Utility field used by event firing mechanism.
     */
    private javax.swing.event.EventListenerList listenerList = null;
    private boolean ignoreChanges2 = false;

    //Some property panel specific, package private hacks
    private PropertyChangeListener remoteEnvListener = null;
    private VetoableChangeListener remotevEnvListener = null;

    /** Creates a new instance of CustomEditorDisplayer */
    public CustomEditorDisplayer(Property prop) {
        this.prop = prop;
    }

    public CustomEditorDisplayer(Property prop, PropertyModel mdl) {
        this(prop);
        model = mdl;
    }

    public void setUpdatePolicy(int i) {
        this.updatePolicy = i;

        if (env != null) {
            env.setChangeImmediate(i != UPDATE_ON_EXPLICIT_REQUEST);
        }
    }

    private Component getCustomEditor() {
        if (customEditor == null) {
            customEditor = getPropertyEditor().getCustomEditor();
        }

        return customEditor;
    }

    PropertyEditor getPropertyEditor() { //Package private for unit tests

        if (editor == null) {
            setPropertyEditor(PropUtils.getPropertyEditor(getProperty()));
        }

        return editor;
    }

    private void setPropertyEditor(PropertyEditor editor) {
        if (this.editor != null) {
            detachFromPropertyEditor(this.editor);

            //set ignore changes even so - we may get the same property editor
            //again, in which case we're still listening to it
            ignoreChanges = true;
        }

        this.editor = editor;

        try {
            if (editor != null) {
                if (!editor.supportsCustomEditor()) {
                    Class<?> type = prop.getValueType();
                    throw new IllegalArgumentException(
                        "#177688: property editor " + editor + " for property " + prop +
                        " does not support a custom editor; valueType=" + type.getName() +
                        "; PropertyEditorManager says: " + PropertyEditorManager.findEditor(type) +
                        "; search path is: " + Arrays.toString(PropertyEditorManager.getEditorSearchPath()) +
                        "; CCL: " + Thread.currentThread().getContextClassLoader()
                    );
                }

                try {
                    originalValue = editor.getValue();
                } catch (Exception e) {
                    //dve or other, don't worry
                }

                //Issue 39437 - PropertyPanel in custom editor mode
                //expects a PropertyEnv even if the editor is not
                //an ExPropertyEditor.
                PropertyEnv env = new PropertyEnv();

                //Use the hack to access the real underlying FD, for, e.g.,
                //core.projects.FileStateEditor
                env.setFeatureDescriptor(EditorPropertyDisplayer.findFeatureDescriptor(this));
                setPropertyEnv(env);

                if (editor instanceof ExPropertyEditor) {
                    ((ExPropertyEditor) editor).attachEnv(env);
                }

                attachToPropertyEditor(editor);
            }
        } finally {
            ignoreChanges = false;
        }
    }

    private void setPropertyEnv(PropertyEnv env) {
        if (this.env != null) {
            detachFromEnv(this.env);
        }

        this.env = env;

        if (env != null) {
            env.setChangeImmediate(getUpdatePolicy() != UPDATE_ON_EXPLICIT_REQUEST);
            attachToEnv(env);
        }
    }

    private void attachToEnv(PropertyEnv env) {
        env.addPropertyChangeListener(getEnvListener());
        env.addVetoableChangeListener(getEnvListener());
        env.setBeans(EditorPropertyDisplayer.findBeans(this));
    }

    private void detachFromEnv(PropertyEnv env) {
        env.removePropertyChangeListener(getEnvListener());
        env.removeVetoableChangeListener(getEnvListener());
    }

    private void attachToPropertyEditor(PropertyEditor editor) {
        //        editor.addPropertyChangeListener(WeakListeners.propertyChange(getEditorListener(), editor));
        editor.addPropertyChangeListener(getEditorListener());
    }

    private void detachFromPropertyEditor(PropertyEditor editor) {
        editor.removePropertyChangeListener(getEditorListener());
    }

    private PropertyChangeListener getEditorListener() {
        if (editorListener == null) {
            editorListener = new EditorListener();
        }

        return editorListener;
    }

    private EnvListener getEnvListener() {
        if (envListener == null) {
            envListener = new EnvListener();
        }

        return envListener;
    }

    public boolean commit() throws IllegalArgumentException {
        //        System.err.println("COMMIT - " + getProperty().getDisplayName());
        try {
            ignoreChanges = true;

            PropertyEditor editor = getPropertyEditor();
            Object entered = getEnteredValue();

            //            System.err.println("COMMIT - entered value: " + entered);
            try {
                if ((entered != null) && entered.equals(getProperty().getValue())) {
                    //                    System.err.println("  entered value matches property value, return false");
                    return false;
                }
            } catch (Exception e) {
                //IllegalAccessException, etc.
                //                System.err.println("  caught an exception, aborting");
                Logger.getLogger(CustomEditorDisplayer.class.getName()).log(Level.WARNING, null, e);

                try {
                    if (getProperty().canRead()) {
                        editor.setValue(model.getValue());
                    }
                } catch (ProxyNode.DifferentValuesException dve) {
                    // ok - no problem here, it was just the old value
                } catch (Exception ex) {
                    PropertyDialogManager.notify(ex);
                }

                return false;
            }

            PropertyEnv env = getPropertyEnv();

            Exception exception = PropUtils.updatePropertyEditor(editor, entered);

            if (exception == null) {
                if ((env != null) && PropertyEnv.STATE_NEEDS_VALIDATION.equals(env.getState())) {
                    String msg = env.silentlySetState(env.STATE_VALID, entered);

                    System.err.println("  result of silent set state: " + msg);

                    //something vetoed the change
                    if ((msg != null) && !PropertyEnv.STATE_VALID.equals(env.getState())) {
                        IllegalArgumentException iae = new IllegalArgumentException("Error setting value"); //NOI18N

                        Exceptions.attachLocalizedMessage(iae, msg);

                        //set the state to invalid
                        if (!env.STATE_INVALID.equals(env.getState())) {
                            env.silentlySetState(env.STATE_INVALID, null);
                        }

                        throw iae;
                    }
                }
            }

            Object res = Boolean.FALSE;

            if (exception == null) {
                res = PropUtils.noDlgUpdateProp(getModel(), editor);
                originalValue = editor.getValue();

                if (res instanceof Exception && (!(res instanceof ProxyNode.DifferentValuesException))) {
                    exception = (Exception) res;
                }

                if (res instanceof InvocationTargetException || res instanceof IllegalAccessException) {
                    PropertyDialogManager.notify((Exception) res);
                }
            }

            if (exception != null) {
                if (exception instanceof IllegalArgumentException) {
                    throw (IllegalArgumentException) exception;
                } else {
                    PropertyDialogManager.notify(exception);

                    IllegalArgumentException iae = new IllegalArgumentException("Error setting value"); //NOI18N
                    Exceptions.attachLocalizedMessage(iae,
                                             PropUtils.findLocalizedMessage(exception,
                                                                            entered,
                                                                            getProperty().getDisplayName()));
                    throw iae;
                }
            }

            boolean result = Boolean.TRUE.equals(res);

            if (result) {
                fireActionPerformed();
            }

            return result;
        } finally {
            ignoreChanges = false;
        }
    }

    PropertyModel getModel() {
        if (model == null) {
            return new NodePropertyModel(getProperty(), null);
        } else {
            return model;
        }
    }

    void setModel(PropertyModel mdl) {
        model = mdl;
    }

    public PropertyEnv getPropertyEnv() {
        return env;
    }

    public Component getComponent() {
        return getCustomEditor();
    }

    public Object getEnteredValue() {
        PropertyEditor editor = getPropertyEditor();
        Object result;

        if (customEditor  instanceof EnhancedCustomPropertyEditor) {
            result = ((EnhancedCustomPropertyEditor) customEditor ).getPropertyValue();
        } else {
            result = editor.getValue(); //editor.getAsText(); //XXX getValue?
        }

        return result;
    }

    public Property getProperty() {
        return prop;
    }

    public int getUpdatePolicy() {
        return updatePolicy;
    }

    public String isModifiedValueLegal() {
        boolean legal = true;
        String msg = null;
        PropertyEditor editor = getPropertyEditor();

        //        System.err.println("IS MODIFIED VALUE LEGAL");
        if (env != null) {
            legal = env.getState() != env.STATE_INVALID;

            System.err.println(" Attempting to validate env");

            if (legal && env.STATE_NEEDS_VALIDATION.equals(env.getState())) {
                msg = env.silentlySetState(env.STATE_VALID, getEnteredValue());

                //                System.err.println("  silentlySetState returned: " + msg);
                legal = msg == null;
            }
        } else if (editor instanceof EnhancedCustomPropertyEditor) {
            Object entered = ((EnhancedCustomPropertyEditor) editor).getPropertyValue();

            try {
                editor.setValue(entered);
            } catch (IllegalStateException ise) {
                legal = false;
                msg = PropUtils.findLocalizedMessage(ise, entered, getProperty().getDisplayName());
            }
        }

        if (!legal && (msg == null)) {
            //            System.err.println(" not legal, constructing message");
            msg = NbBundle.getMessage(
                    CustomEditorDisplayer.class, "FMT_CannotUpdateProperty",editor.getValue(), getProperty().getDisplayName()); //NOI18N
        }

        return msg;
    }

    public boolean isValueModified() {
        PropertyEditor editor = getPropertyEditor();
        boolean result = editor.getValue() != originalValue;

        if (!result && editor instanceof EnhancedCustomPropertyEditor) {
            Object entered = ((EnhancedCustomPropertyEditor) editor).getPropertyValue();

            if (entered != null) {
                result = entered.equals(originalValue);
            } else {
                result = originalValue == null;
            }
        }

        return result;
    }

    public void refresh() {
        //do nothing
    }

    public void reset() {
        try {
            originalValue = getProperty().getValue();
            getPropertyEditor().setValue(originalValue);
        } catch (Exception e) {
            //should not happen - the value came from the property
            Logger.getLogger(CustomEditorDisplayer.class.getName()).log(Level.WARNING, null, e);
        }
    }

    /** Sets whether or not this component is enabled.
     *
     * all panel components gets disabled when enabled parameter is set false
     * @param enabled flag defining the action.
     */
    public void setEnabled(boolean enabled) {
        //        System.err.println("SET ENABLED:" + enabled);
        Component custEditor = (Container) getComponent();

        if (custEditor instanceof Container) {
            setEnabled((Container) custEditor, enabled);
        }

        custEditor.setEnabled(enabled);
    }

    public void setEnabled(Container c, boolean enabled) {
        Component[] comp = c.getComponents();

        for (int i = 0; i < comp.length; i++) {
            if (!(comp[i] instanceof JScrollBar)) {
                comp[i].setEnabled(false);
            } else {
                ((JScrollBar) comp[i]).setFocusable(enabled);
            }

            if (comp[i] instanceof Container) {
                boolean ignore = false;

                if (comp[i] instanceof JComponent) {
                    //Issue 38065 - form editor doesn't want checkbox enabled,
                    //but for compatibility we need to drill through the entire
                    //subtree (otherwise JFileChoosers, etc., will have enabled
                    //components even though setEnabled(false) was called on them).
                    Boolean val = (Boolean) ((JComponent) comp[i]).getClientProperty("dontEnableMe"); //NOI18N

                    if (val != null) {
                        ignore = val.booleanValue();
                    }
                }

                if (!ignore) {
                    setEnabled((Container) comp[i], enabled);
                }
            }
        }

        c.setEnabled(enabled);
    }

    public void setEnteredValue(Object o) {
        PropUtils.updatePropertyEditor(getPropertyEditor(), o);
    }

    public void setActionCommand(String val) {
    }

    public String getActionCommand() {
        return null;
    }

    /**
     * Registers ActionListener to receive events.
     * @param listener The listener to register.
     */
    public synchronized void addActionListener(java.awt.event.ActionListener listener) {
        if (listenerList == null) {
            listenerList = new EventListenerList();
        }

        listenerList.add(ActionListener.class, listener);
    }

    /**
     * Removes ActionListener from the list of listeners.
     * @param listener The listener to remove.
     */
    public synchronized void removeActionListener(java.awt.event.ActionListener listener) {
        listenerList.remove(ActionListener.class, listener);
    }

    /**
     * Notifies all registered listeners about the event.
     *
     * @param event The event to be fired
     */
    private void fireActionPerformed() {
        ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "userChangedValue"); //NOI18N

        if (listenerList == null) {
            return;
        }

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
        ChangeEvent event = new ChangeEvent(this);

        if (listenerList == null) {
            return;
        }

        Object[] listeners = listenerList.getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ChangeListener.class) {
                ((ChangeListener) listeners[i + 1]).stateChanged(event);
            }
        }
    }

    void setRemoteEnvListener(PropertyChangeListener l) {
        //        System.err.println(" setRemoteEnvListener on " + System.identityHashCode(this) + " to " + l);
        remoteEnvListener = l;
    }

    void setRemoteEnvVetoListener(VetoableChangeListener vl) {
        remotevEnvListener = vl;
    }

    private class EnvListener implements PropertyChangeListener, VetoableChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            fireStateChanged();

            //            System.err.println(" Custom displayer got a property change");
            //Propagate changes to a property panel
            if (remoteEnvListener != null) {
                remoteEnvListener.propertyChange(evt);

                //            } else {
                //                System.err.println("But nobody is listening!");
            }
        }

        public void vetoableChange(PropertyChangeEvent evt)
        throws PropertyVetoException {
            if (remotevEnvListener != null) {
                remotevEnvListener.vetoableChange(evt);
            }
        }
    }

    private class EditorListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            //            System.err.println("Property change on CustomEditorDisplayer from " + evt.getSource() + " new value=" + evt.getNewValue());
            if (ignoreChanges) {
                //                System.err.println("  ignoring");
                return;
            }

            if (ExPropertyEditor.PROP_VALUE_VALID.equals(evt.getPropertyName())) {
                //                System.err.println("  value valid - ignoring");
                return;
            }

            if (ignoreChanges2) {
                return;
            }

            ignoreChanges2 = true;

            if (getUpdatePolicy() != UPDATE_ON_EXPLICIT_REQUEST) {
                commit();

                //            } else {
                //                System.err.println("  policy is UPDATE_ON_EXPLICIT_REQUEST - ignoring");
            }

            fireStateChanged();
            ignoreChanges2 = false;
        }
    }

}
