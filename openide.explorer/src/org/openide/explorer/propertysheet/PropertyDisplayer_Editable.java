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
