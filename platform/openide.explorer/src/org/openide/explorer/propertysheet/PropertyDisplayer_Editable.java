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
 * PropertyDisplayer_Editable.java
 * Refactored from PropertyDisplayer.Editable to keep the interface private.
 * Created on December 13, 2003, 7:17 PM
 */
package org.openide.explorer.propertysheet;

import java.awt.event.ActionListener;

import javax.swing.event.ChangeListener;


/** Basic definition of a property displayer which allows editing.
 * @author Tim Boudreau  */
interface PropertyDisplayer_Editable extends PropertyDisplayer {
    /** Set the enabled state of the component, determining if it can
     * be edited or not.  Clients should use this method, not the
     * setEnabled() method on the result of getComponent() to ensure
     * the enabled state is correctly set for the editor - in the case
     * of the current implementation for custom editors, a call to this
     * method will disable all children of the custom editor */
    public void setEnabled(boolean enabled);

    /** Reset the value to that of the property, discarding any edits in
     * progress */
    public void reset();

    /** Determine if the value has been modified by the user */
    public boolean isValueModified();

    /** Determine if the modified value can be written to the property
     * without errors.  This method will return a localized message
     * describing the problem, or null if the value is legal */
    public String isModifiedValueLegal();

    /** Writes the edited value to the property.  If the update policy
     * is UPDATE_ON_EXPLICIT_REQUEST, this method will throw an exception
     * if the value cannot be written; for other update policies, a dialog
     * will be shown to the user if there is a problem */
    public boolean commit() throws IllegalArgumentException;

    /** Get the value the user has entered.  This generally follows the
     * contract of InplaceEditor.getValue() - it may either return a
     * String (which may be compatible with the property's property editor's
     * setAsText()) method, or it may return an object directly writable
     * to the property. */
    public Object getEnteredValue();

    /** Set the value to be displayed */
    public void setEnteredValue(Object o);

    /** Get the update policy for the editor.  */
    public int getUpdatePolicy();

    /** Set the update policy for the editor.  Not all possible states
     * are meaningful to all possible implementations; in particular,
     * it is impossible to determine when a custom editor will choose to
     * write its value to the property. Inline editors have more predictable
     * semantics in this regard.  */
    public void setUpdatePolicy(int i);

    /** Set the action command to be fired if the user performs an action*/
    public void setActionCommand(String val);

    /** Get the action command to be fired if the user performs an action*/
    public String getActionCommand();

    /** Add an action listener.  Implementations should follow the general
     * contract of JTextField - if an action listener is not present,
     * performing an action with the Enter or Esc keys should close any
     * parent dialog */
    public void addActionListener(ActionListener al);

    /** Remove an action listener */
    public void removeActionListener(ActionListener al);

    /** Add a change listener.  State changes are to be fired on either
     * changes in the writability of the entered value, or committing of
     * the value to the property */
    public void addChangeListener(ChangeListener cl);

    /** Remove a change listener */
    public void removeChangeListener(ChangeListener cl);

    //XXX remove the propertyEnv method if this interface is to be made public -
    //it's just needed to support PropertyPanel.getPropertyEnv - it can be
    //done by reflection instead.
    public PropertyEnv getPropertyEnv();
}
