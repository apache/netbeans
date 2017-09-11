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

package org.netbeans.jellytools.properties.editors;

import javax.swing.JDialog;
import javax.swing.ListModel;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/** Class implementing all necessary methods for handling String Array Custom Editor
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @version 1.0 */
public class StringArrayCustomEditorOperator extends NbDialogOperator {

    /** Creates new StringArrayCustomEditorOperator
     * @throws TimeoutExpiredException when NbDialog not found
     * @param title String title of custom editor */
    public StringArrayCustomEditorOperator(String title) {
        super(title);
    }

    /** Creates new StringArrayCustomEditorOperator
     * @param wrapper JDialogOperator wrapper for custom editor */    
    public StringArrayCustomEditorOperator(JDialogOperator wrapper) {
        super((JDialog)wrapper.getSource());
    }

    private JButtonOperator _btAdd;
    private JButtonOperator _btRemove;
    private JButtonOperator _btEdit;
    private JButtonOperator _btUp;
    private JButtonOperator _btDown;
    private JListOperator _lstItemList;
    private JTextFieldOperator _txtItemText;

    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "Edit" JButton in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JButtonOperator
     */
    public JButtonOperator btEdit() {
        if (_btEdit==null) {
            _btEdit = new JButtonOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.beaninfo.editors.Bundle",
                    "CTL_Change_StringArrayCustomEditor"));
        }
        return _btEdit;
    }

    /** Tries to find "Down" JButton in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JButtonOperator
     */
    public JButtonOperator btDown() {
        if (_btDown==null) {
            _btDown = new JButtonOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.beaninfo.editors.Bundle",
                    "CTL_MoveDown"));
        }
        return _btDown;
    }

    /** Tries to find "Up" JButton in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JButtonOperator
     */
    public JButtonOperator btUp() {
        if (_btUp==null) {
            _btUp = new JButtonOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.beaninfo.editors.Bundle",
                    "CTL_MoveUp"));
        }
        return _btUp;
    }

    /** Tries to find "Add" JButton in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JButtonOperator
     */
    public JButtonOperator btAdd() {
        if (_btAdd==null) {
            _btAdd = new JButtonOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.beaninfo.editors.Bundle",
                    "CTL_Add_StringArrayCustomEditor"));
        }
        return _btAdd;
    }

    /** Tries to find JList in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JListOperator
     */
    public JListOperator lstItemList() {
        if (_lstItemList==null) {
            _lstItemList = new JListOperator(this);
        }
        return _lstItemList;
    }

    /** Tries to find "Remove" JButton in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JButtonOperator
     */
    public JButtonOperator btRemove() {
        if (_btRemove==null) {
            _btRemove = new JButtonOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.beaninfo.editors.Bundle",
                    "CTL_Remove"));
        }
        return _btRemove;
    }

    /** Tries to find null JTextField in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtItemText() {
        if (_txtItemText==null) {
            _txtItemText = new JTextFieldOperator(this);
        }
        return _txtItemText;
    }

    /** clicks on "Edit" JButton
     * @throws TimeoutExpiredException when JButton not found
     */
    public void edit() {
        btEdit().push();
    }

    /** clicks on "Down" JButton
     * @throws TimeoutExpiredException when JButton not found
     */
    public void down() {
        btDown().push();
    }

    /** clicks on "Up" JButton
     * @throws TimeoutExpiredException when JButton not found
     */
    public void up() {
        btUp().push();
    }

    /** clicks on "Add" JButton
     * @throws TimeoutExpiredException when JButton not found
     */
    public void add() {
        btAdd().push();
    }

    /** clicks on "Remove" JButton
     * @throws TimeoutExpiredException when JButton not found
     */
    public void remove() {
        btRemove().push();
    }

    /** Gets text from text field.
     * @return String edited item text */
    public String getItemText() {
        return txtItemText().getText();
    }
    
    /** tries to find and set text of txtItem
     * @param text String text
     */
    public void setItemText( String text ) {
        txtItemText().clearText();
        txtItemText().typeText(text);
    }

    
    /** getter for array of Strings
     * @return String[] array of strings from custom editor */    
    public String[] getStringArrayValue() {
        ListModel lm=lstItemList().getModel();
        String value[]=new String[lm.getSize()];
        for (int i=0; i<lm.getSize(); i++) {
            value[i]=lm.getElementAt(i).toString();
        }
        return value;
    }

    /** adds String to the edited array
     * @param value String value to be added */    
    public void add(String value) {
        setItemText(value);
        add();
    }
    
    /** removes String from array
     * @param value String value to be removed */    
    public void remove(String value) {
        lstItemList().selectItem(value);
        remove();
    }
    
    /** removes all Strings from array */    
    public void removeAll() {
        while (lstItemList().getModel().getSize()>0) {
            lstItemList().selectItem(0);
            remove();
        }
    }
    
    /** Replaces oldValue by newValue. It selects oldValue item in the list,
     * types newValue into text field and pushes Edit button.
     * @param oldValue value from list to be replaced
     * @param newValue new value
     */
    public void edit(String oldValue, String newValue) {
        lstItemList().selectItem(oldValue);
        setItemText(newValue);
        edit();
    }

    /** Moves given item one position up. It select value item and pushes
     * Up button. It fails, if item is the topmost and Up button is disabled.
     * @param value item to be moved up
     */
    public void up(String value) {
        lstItemList().selectItem(value);
        up();
    }

    /** Moves given item one position down. It select value item and pushes
     * Down button. It fails, if item is the bottommost and Down button is disabled.
     * @param value item to be moved down
     */
    public void down(String value) {
        lstItemList().selectItem(value);
        down();
    }

    /** setter for String array
     * @param value String[] array os Strings to be set */    
    public void setStringArrayValue(String[] value) {
        removeAll();
        for (int i=0; i<value.length; i++)
            add(value[i]);
    }
    
    /** Performs verification by accessing all sub-components */    
    public void verify() {
        btAdd();
        btRemove();
        btEdit();
        btUp();
        btDown();
        lstItemList();
        txtItemText();
    }
}

