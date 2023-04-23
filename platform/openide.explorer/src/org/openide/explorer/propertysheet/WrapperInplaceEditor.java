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
/*
 * WrapperInplaceEditor.java
 *
 * Created on January 4, 2003, 4:30 PM
 */
package org.openide.explorer.propertysheet;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.explorer.propertysheet.editors.EnhancedPropertyEditor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.*;

import java.lang.reflect.*;

import java.util.*;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.swing.text.JTextComponent;


/** Wrapper for legacy inplace custom editors supplied the deprecated
 * <code>EnhancedPropertyEditor</code>.  Attempts to allow them to behave
 * correctly, but does not guarantee it.
 * <P>Note that this class does <strong>not</strong>
 * support using AWT components as inplace editors.
 * <P> Note that this class is not considered reusable, unlike other inplace
 *  editors.  Each time a legacy inline editor is needed, a new instance of
 *  this class should be created.
 *
 * @author  Tim Boudreau
 */
class WrapperInplaceEditor extends JPanel implements InplaceEditor, ActionListener, FocusListener {
    /** The enhanced property editor we will wrap */
    private EnhancedPropertyEditor enh;

    /** The obligatory property model */
    private PropertyModel mdl;

    /** The legacy component returned by enh.getCustomInPlaceEditor() */
    private Component legacy = null;

    /** Listener list */
    private transient List<ActionListener> actionListenerList;

    /** True if adding an ActionListener to the legacy component using
     * reflection succeeded */
    private boolean listenerAdded = false;
    private boolean suspendEvents = false;

    /**
     * Utility field used by event firing mechanism.
     */
    private javax.swing.event.EventListenerList listenerList = null;

    /** Create a new instance */
    WrapperInplaceEditor(EnhancedPropertyEditor enh) {
        this.enh = enh;
        setLayout(new BorderLayout());
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enter"); //NOI18N
        getActionMap().put("enter", new EnterKbdAction()); //NOI18N
    }

    /** Forward focus events from within the legacy component to the
     * infrastructure, so editor will be properly removed if focus changes */
    public void focusGained(FocusEvent e) {
        e.setSource(this);
        fireFocusGained(e);
    }

    /** Forward focus events from within the legacy component to the
     * infrastructure, so editor will be properly removed if focus changes */
    public void focusLost(FocusEvent e) {
        e.setSource(this);
        fireFocusLost(e);
    }

    public void clear() {
        if (legacy != null) {
            removeAll();

            if (listenerAdded) {
                tryRemoveActionListener(legacy);
            }

            legacy.removeFocusListener(this);
            legacy = null;
        }

        enh = null;
        listenerAdded = false;
    }

    /** Uses reflection to find an addActionListener method if present, and
     * if so, adds this WrapperInplaceEditor as an action listener */
    private boolean tryAddActionListener(Component comp) {
        try {
            Method m = comp.getClass().getMethod("addActionListener", new Class[] { ActionListener.class }); //NOI18N

            if (m != null) {
                m.invoke(comp, this);

                return true;
            }
        } catch (Exception e) {
            //No big deal
        }

        return false;
    }

    /** Uses reflection to try to remove an this WrapperInplaceEditor as an
     * action listener on the component in question */
    private boolean tryRemoveActionListener(Component comp) {
        try {
            Method m = comp.getClass().getMethod("removeActionListener", new Class[] { ActionListener.class }); //NOI18N

            if (m != null) {
                m.invoke(comp, this);

                return true;
            }
        } catch (Exception e) {
            //This is ok
        }

        return false;
    }

    /** Action listener implementation.  The WarpperInplaceEditor is added
     * as an action listener to the legacy inplace editor component.  Any
     * action event coming from the legacy component will trigger refiring as
     * InplaceEditor.ACTION_SUCCESS indicating a completed edit. */
    public void actionPerformed(ActionEvent ae) {
        fireAction(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, InplaceEditor.COMMAND_SUCCESS));
    }

    /** Connect to the underlying property editor.  Will add the component
     * to this WrapperInplaceEditor's component hierarchy and start listening
     * on it for action and focus events */
    public void connect(java.beans.PropertyEditor pe, PropertyEnv env) {
        if (legacy != null) {
            //Should never be called twice without a clear(), but just in case
            clear();
        }

        if (pe != enh) {
            enh = (EnhancedPropertyEditor) pe;
        }

        Component comp = getLegacyInplaceEditor();
        add(comp, BorderLayout.CENTER);
        listenerAdded = tryAddActionListener(comp);
        comp.addFocusListener(this);
    }

    public JComponent getComponent() {
        return this;
    }

    /** Will check if the underlying component is a combo box, and if so
     * suppress the arrow keys.  Doesn't try to do anything special for
     * other components. */
    public KeyStroke[] getKeyStrokes() {
        if (getLegacyInplaceEditor() instanceof JComboBox) {
            return ComboInplaceEditor.cbKeyStrokes;
        } else {
            return null;
        }
    }

    public java.beans.PropertyEditor getPropertyEditor() {
        return enh;
    }

    public PropertyModel getPropertyModel() {
        return mdl;
    }

    public Object getValue() {
        return enh.getValue();
    }

    public void handleInitialInputEvent(InputEvent e) {
        //do something?
    }

    public boolean isKnownComponent(Component c) {
        return isAncestorOf(c);
    }

    /** Makes a best effort attempt to set the value appropriately on the
     * custom editor if it is a combo box or text component. */
    public void reset() {
        suspendEvents = true;

        try {
            if (legacy instanceof JTextComponent) {
                ((JTextComponent) legacy).setText(enh.getAsText());
            } else if (legacy instanceof JComboBox) {
                if (((JComboBox) legacy).isEditable()) {
                    if (((JComboBox) legacy).getEditor().getEditorComponent().isShowing()) {
                        ((JComboBox) legacy).getEditor().setItem(enh.getValue());
                    }
                } else {
                    ((JComboBox) legacy).setSelectedItem(enh.getValue());
                }
            }
        } catch (Exception e) {
            //What we are doing here is dangerous and may fail depending on 
            //the implementation of the legacy editor, so log the exception
            //but don't notify the user
            Logger.getLogger(WrapperInplaceEditor.class.getName()).log(Level.WARNING, "Failure resetting legacy editor", e); //NOI18N
        } finally {
            suspendEvents = false;
        }
    }

    public void setPropertyModel(PropertyModel pm) {
        mdl = pm;
    }

    public void setValue(Object o) {
        suspendEvents = true;

        try {
            if (legacy instanceof JTextComponent) {
                ((JTextComponent) legacy).setText(o.toString());
            } else if (legacy instanceof JComboBox) {
                if (((JComboBox) legacy).isEditable()) {
                    if (((JComboBox) legacy).getEditor().getEditorComponent().isShowing()) {
                        ((JComboBox) legacy).getEditor().setItem(o.toString());
                    }
                } else {
                    ((JComboBox) legacy).setSelectedItem(o);
                }
            }
        } catch (Exception e) {
            //What we are doing here is dangerous and may fail depending on 
            //the implementation of the legacy editor, so log the exception
            //but don't notify the user
            Logger.getLogger(WrapperInplaceEditor.class.getName()).log(Level.WARNING, "Failure resetting legacy editor", e); //NOI18N
        } finally {
            suspendEvents = false;
        }
    }

    /** Attempts to give a reasonable answer for text components and
     * combo boxes;  for everything else, returns false */
    public boolean supportsTextEntry() {
        if (legacy instanceof JTextComponent) {
            return true;
        } else if ((legacy instanceof JComboBox) && ((JComboBox) legacy).isEditable()) {
            return true;
        } else {
            return false;
        }
    }

    /** Fetch (and cache, in case no caching is done in the property editor)
     * the component we want to wrap */
    private Component getLegacyInplaceEditor() {
        if (legacy == null) {
            legacy = enh.getInPlaceCustomEditor();
        }

        return legacy;
    }

    public synchronized void addActionListener(ActionListener listener) {
        if (actionListenerList == null) {
            actionListenerList = new ArrayList<ActionListener>();
        }

        actionListenerList.add(listener);
    }

    public synchronized void removeActionListener(ActionListener listener) {
        if (actionListenerList != null) {
            actionListenerList.remove(listener);
        }
    }

    void fireAction(ActionEvent event) {
        if (suspendEvents) {
            return;
        }

        List list;

        synchronized (this) {
            if (actionListenerList == null) {
                return;
            }

            list = (List) ((ArrayList) actionListenerList).clone();
        }

        for (int i = 0; i < list.size(); i++) {
            ((ActionListener) list.get(i)).actionPerformed(event);
        }
    }

    /**
     * Registers FocusListener to receive events.
     * @param listener The listener to register.
     */
    @Override
    public synchronized void addFocusListener(FocusListener listener) {
        if (listenerList == null) {
            listenerList = new EventListenerList();
        }

        listenerList.add(FocusListener.class, listener);
        super.addFocusListener(listener);
    }

    /**
     * Removes FocusListener from the list of listeners.
     * @param listener The listener to remove.
     */
    @Override
    public synchronized void removeFocusListener(FocusListener listener) {
        listenerList.remove(java.awt.event.FocusListener.class, listener);
        super.removeFocusListener(listener);
    }

    /**
     * Notifies all registered listeners about the event.
     *
     * @param event The event to be fired
     */
    private void fireFocusGained(FocusEvent event) {
        if (listenerList == null) {
            return;
        }

        Object[] listeners = listenerList.getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == java.awt.event.FocusListener.class) {
                ((java.awt.event.FocusListener) listeners[i + 1]).focusGained(event);
            }
        }
    }

    /**
     * Notifies all registered listeners about the event.
     *
     * @param event The event to be fired
     */
    private void fireFocusLost(FocusEvent event) {
        if (listenerList == null) {
            return;
        }

        Object[] listeners = listenerList.getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == java.awt.event.FocusListener.class) {
                ((java.awt.event.FocusListener) listeners[i + 1]).focusLost(event);
            }
        }
    }

    /** A keyboard action to listen for pressing enter to take the value and
     * close the editor */
    private class EnterKbdAction extends AbstractAction {
        public void actionPerformed(ActionEvent ae) {
            fireAction(new ActionEvent(WrapperInplaceEditor.this, ActionEvent.ACTION_PERFORMED, COMMAND_SUCCESS));
        }
    }
}
