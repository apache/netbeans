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
* SheetCellEditor.java
*
* Created on December 17, 2002, 5:48 PM
*/
package org.openide.explorer.propertysheet;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.*;
import org.openide.nodes.Node.Property;

import java.awt.*;
import java.awt.event.*;

import java.beans.*;

import java.util.EventObject;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;


/** Table cell editor which wraps inplace editors in its
 *  table cell editor interface.
 * @author  Tim Boudreau */
final class SheetCellEditor implements TableCellEditor, ActionListener {
    /** A lazy, reusable change event. */
    ChangeEvent ce = null;
    ReusablePropertyEnv reusableEnv;

    /** Utility field used by event firing mechanism. */
    private javax.swing.event.EventListenerList listenerList = null;
    private InplaceEditorFactory factory = null;
    private ButtonPanel buttonPanel = null;
    InplaceEditor inplaceEditor = null;
    boolean lastUpdateSuccess = true;

    /** Private constructor;  only the default instance may be used. */
    SheetCellEditor(ReusablePropertyEnv env) {
        reusableEnv = env;
    }

    void setInplaceEditor(InplaceEditor ie) {
        if (ie == inplaceEditor) {
            return;
        }

        if (PropUtils.isLoggable(SheetCellEditor.class)) {
            PropUtils.log(SheetCellEditor.class, "  SheetCellEditor.setInplaceEditor " + ie); //NOI18N
        }

        if (ie == null) {
            if (inplaceEditor != null) {
                inplaceEditor.clear();
            }
        } else {
            ie.addActionListener(this);
        }

        if (inplaceEditor != null) {
            inplaceEditor.removeActionListener(this);
        }

        inplaceEditor = ie;
    }

    PropertyEditor getPropertyEditor() {
        PropertyEditor result;

        if (inplaceEditor == null) {
            result = null;
        } else {
            result = inplaceEditor.getPropertyEditor();
        }

        return result;
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        Component result = null;

        //since you can't change the model, no worries
        SheetTable stb = (SheetTable) table;
        lastUpdateSuccess = true;

        //fetch the property from the set model
        Property p = (Property) stb.getSheetModel().getPropertySetModel().getFeatureDescriptor(row);

        result = getEditorComponent(
                p, this, table.getForeground(), table.getBackground(), table.getSelectionBackground(),
                table.getSelectionForeground()
            );

        if (result instanceof ButtonPanel) {
            ((ButtonPanel) result).setButtonAction(stb.getCustomEditorAction());
        }

        if (result != null) {
            result.setFont(stb.getFont());
        }

        return result;
    }

    private InplaceEditorFactory factory() {
        if (factory == null) {
            factory = new InplaceEditorFactory(true, reusableEnv);
        }

        return factory;
    }

    private ButtonPanel buttonPanel() {
        if (buttonPanel == null) {
            buttonPanel = new ButtonPanel();
        }

        return buttonPanel;
    }

    public boolean isLastUpdateSuccessful() {
        return lastUpdateSuccess;
    }

    public Component getEditorComponent(
        Property p, ActionListener al, Color foreground, Color background, Color selBg, Color selFg
    ) {
        JComponent result = null;
        InplaceEditor inplace;

        //get an appropriate inplace editor connected to this property
        //store the value, attaching action listener the editor
        setInplaceEditor(inplace = factory().getInplaceEditor(p, false));

        //if it should have a custom editor button, embed it in the shared
        //instance of ButtonPanel
        PropertyEditor ped = inplaceEditor.getPropertyEditor();

        // Issue 35521 forget trying to edit things that don't have property 
        // editor
        if (ped instanceof PropUtils.NoPropertyEditorEditor) {
            setInplaceEditor(null);

            return null;
        }

        boolean propRequestsSuppressButton = Boolean.TRUE.equals(p.getValue("suppressCustomEditor")); //NOI18N

        JComponent realEditor = null;

        if (ped.supportsCustomEditor() && !propRequestsSuppressButton) {
            realEditor = inplaceEditor.getComponent();

            ButtonPanel bp = buttonPanel();

            //use our static instance of ButtonPanel
            bp.setInplaceEditor(inplace);

            //attach the table's custom editor action to the button
            result = bp;
        } else {
            result = inplaceEditor.getComponent();
        }

        return result;
    }

    /** Handler for action events thrown by the current inplaceEditor.
     *  An action event will cause stopCellEditing() to be called, and
     *  this instance of SheetCellEditor to stop listening for
     *  further action events on the inplace editor. */
    public void actionPerformed(ActionEvent ae) {
        if (PropUtils.isLoggable(SheetCellEditor.class)) {
            PropUtils.log(SheetCellEditor.class, "Editor received an action event - " + ae.getActionCommand()); //NOI18N
        }

        if (!(ae.getSource() instanceof InplaceEditor)) {
            if (PropUtils.isLoggable(SheetCellEditor.class)) {
                PropUtils.log(
                    SheetCellEditor.class,
                    " Event came from an unknown object type - assuming a legacy EnhancedPropertyEditor is the cause and updating property"
                ); //NOI18N
            }

            //Then we have a legacy inplace editor handled by wrapper editor.
            //Assume any action means we should update the property.
            if (inplaceEditor != null) {
                if (PropUtils.isLoggable(SheetCellEditor.class)) {
                    PropUtils.log(SheetCellEditor.class, "WRITING PROPERTY VALUE FROM EDITOR TO PROPERTY"); //NOI18N
                }

                PropUtils.updateProp(inplaceEditor.getPropertyModel(), inplaceEditor.getPropertyEditor(), ""); //NOI18N
            }

            cancelCellEditing();
        }

        if (ae.getActionCommand() == InplaceEditor.COMMAND_SUCCESS) {
            stopCellEditing();
        } else if (ae.getActionCommand() == InplaceEditor.COMMAND_FAILURE) {
            if (PropUtils.psCommitOnFocusLoss) {
                stopCellEditing();
            } else {
                cancelCellEditing();
            }
        } else {
            return;
        }
    }

    protected void fireEditingStopped() {
        if (PropUtils.isLoggable(SheetCellEditor.class)) {
            PropUtils.log(SheetCellEditor.class, "    SheetCellEditor firing editing stopped to table "); //NOI18N
        }

        if (listenerList == null) {
            return;
        }

        Object[] listeners = listenerList.getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == CellEditorListener.class) {
                if (ce == null) {
                    ce = new ChangeEvent(this);
                }

                ((CellEditorListener) listeners[i + 1]).editingStopped(ce);
            }
        }
    }

    protected void fireEditingCancelled() {
        if (PropUtils.isLoggable(SheetCellEditor.class)) {
            PropUtils.log(SheetCellEditor.class, "    SheetCellEditor firing editing cancelled to table "); //NOI18N
        }

        PropUtils.notifyEditingCancelled(reusableEnv);
        
        if (listenerList == null) {
            return;
        }

        Object[] listeners = listenerList.getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == CellEditorListener.class) {
                if (ce == null) {
                    ce = new ChangeEvent(this);
                }

                ((CellEditorListener) listeners[i + 1]).editingCanceled(ce);
            }
        }
    }

    /** Returns the last-provided inplace editor.  */
    public InplaceEditor getInplaceEditor() {
        return inplaceEditor;
    }

    public void cancelCellEditing() {
        if (inplaceEditor != null) {
            try {
                if (PropUtils.isLoggable(SheetCellEditor.class)) {
                    PropUtils.log(SheetCellEditor.class, "  SheetCellEditor.cancelCellEditing ", true); //NOI18N
                }

                fireEditingCancelled();
            } finally {
                setInplaceEditor(null);
            }
        }
    }

    public Object getCellEditorValue() {
        if (inplaceEditor != null) {
            return inplaceEditor.getValue();
        }

        return null;
    }

    public boolean isCellEditable(EventObject anEvent) {
        return true;
    }

    public boolean shouldSelectCell(EventObject anEvent) {
        if (anEvent instanceof MouseEvent) {
            MouseEvent e = (MouseEvent) anEvent;

            return e.getID() != MouseEvent.MOUSE_DRAGGED;
        }

        return true;
    }

    //#63842: stopCellEditing can be called second time when a new dialog window
    //opens while the property is being updated (causing the editor to loose input focus)
    private boolean inStopCellEditing = false;
    
    /* 
     * Flag which prevents the SheetCellEditor from stopping the editing mode
     * when one explicitly calls InlineEditor.setValue(...)
     */
    static boolean ignoreStopCellEditing = false;
    
    public boolean stopCellEditing() {
        if (PropUtils.isLoggable(SheetCellEditor.class)) {
            PropUtils.log(SheetCellEditor.class, "SheetCellEditor.StopCellEditing", true); //NOI18N
        }

        if (inplaceEditor != null && !inStopCellEditing ) {
            inStopCellEditing = true;
            try {
                Component c = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();

                //JTable with client property terminateEditOnFocusLost will try to
                //update the value.  That's not what we want, as it means you can
                //have a partial value, open a custom editor and get an error because
                //the table tried to write the partial value, when it lost focus
                //to a custom editor.
                if (!PropUtils.psCommitOnFocusLoss) {
                    if (
                        (!(c instanceof JTable)) && (!inplaceEditor.isKnownComponent(c)) &&
                            (c != inplaceEditor.getComponent())
                    ) {
                        if (PropUtils.isLoggable(SheetCellEditor.class)) {
                            PropUtils.log(SheetCellEditor.class, "Focused component is unknown - discarding"); //NOI18N
                        }

                        return false;
                    }
                }

                //get the model, updateProp will clear it
                PropertyModel mdl = inplaceEditor.getPropertyModel();

                try {
                    lastUpdateSuccess = PropUtils.updateProp(inplaceEditor);
                } catch (NullPointerException npe) {
                    if ((inplaceEditor == null) || (inplaceEditor.getPropertyEditor() == null)) {
                        String propID;

                        if (mdl instanceof NodePropertyModel) {
                            propID = ((NodePropertyModel) mdl).getProperty().toString() + " editor class " +
                                ((mdl.getPropertyEditorClass() != null) ? mdl.getPropertyEditorClass().getName()
                                                                        : " unknown editor class") + " ";
                        } else {
                            propID = "";
                        }

                        //c.f. issue 39249 - Really this sort of behavior is unacceptable
                        //and should throw an exception, but there is the faint chance
                        //that the user can be editing a property when some remote process
                        //causes it to change, in which case the exception would be wrong.
                        //May be possible to add a thread-check for the culprit change,
                        //and throw an exception only if it happened on the EQ, not if otherwise,
                        //but even that wouldn't be foolproof, and if all node changes are
                        //moved to EQ it won't work at all.
                        Logger.getAnonymousLogger().warning(
                            "Property " + propID + "value changed *while* the property sheet was setting its value " +
                            "but before it had been set.  This almost always means that the " +
                            "property editor has modified the property's value itself. " +
                            "Property editors should NEVER directly modify properties, it is " +
                            "up to the displayer to decide if/when the property should be " +
                            "updated.  This behavior may cause an exception in the " + "future."
                        );
                        Logger.getAnonymousLogger().log(Level.FINE, null, npe);

                        return false;
                    } else {
                        throw npe;
                    }
                }

                //Fire the action first - appears more repsonsive if the editor is
                //immediately removed, before running the post-set action (which,
                //for the form editor, will, for example, change the caret position
                //in the editor)
                if (PropUtils.isLoggable(SheetCellEditor.class)) {
                    PropUtils.log(SheetCellEditor.class, "  SheetCellEditor Firing editing stopped"); //NOI18N
                }

                if(!ignoreStopCellEditing) {
                    fireEditingStopped();
                }

                //            if (lastUpdateSuccess) {
                tryPostSetAction(mdl);

                //            }
            } finally {
                if(!ignoreStopCellEditing) {
                    setInplaceEditor(null);
                }
                inStopCellEditing = false;
            }

            return true;

            //            } else {
            //                return false;
            //            }
        } else {
            return false;
        }
    }

    /** Allow a post-set hook */
    void tryPostSetAction(PropertyModel mdl) {
        if (mdl instanceof ExPropertyModel) {
            FeatureDescriptor fd = ((ExPropertyModel) mdl).getFeatureDescriptor();

            if (fd != null) {
                Action a = (Action) fd.getValue("postSetAction"); //NOI18N

                if (a != null) {
                    if (PropUtils.isLoggable(SheetCellEditor.class)) {
                        PropUtils.log(SheetCellEditor.class, "  Running post-set action " + a); //NOI18N
                    }

                    ActionEvent ae = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, InplaceEditor.COMMAND_SUCCESS);
                    a.actionPerformed(ae);
                }
            }
        }
    }

    public synchronized void addCellEditorListener(javax.swing.event.CellEditorListener listener) {
        if (listenerList == null) {
            listenerList = new javax.swing.event.EventListenerList();
        }

        listenerList.add(javax.swing.event.CellEditorListener.class, listener);
    }

    public synchronized void removeCellEditorListener(javax.swing.event.CellEditorListener listener) {
        listenerList.remove(CellEditorListener.class, listener);

        //XXX this needs to be here to make editor tag caching
        //work (this is due to performance problems with
        //getTags() on ObjectEditor).  Once caching can be
        //removed, this call should be removed too.
        if (listenerList.getListenerCount(CellEditorListener.class) == 0) {
            if (inplaceEditor != null) {
                inplaceEditor.clear();
            }
        }
    }
}
