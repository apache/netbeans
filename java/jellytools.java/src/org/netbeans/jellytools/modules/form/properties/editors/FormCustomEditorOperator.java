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

package org.netbeans.jellytools.modules.form.properties.editors;

/*
 * CustomEditorDialogOperator.java
 *
 * Created on 6/13/02 11:58 AM
 */

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.*;

/** Dialog opened after click on "..." button in Component Inspector
 * (or property sheet can be docked to a different window).<p>
 * Contains Default, OK and Cancel buttons,
 * combobox enabling to change editor (DimensionEditor/Value from existing component).
 * <p>
 * Example:<p>
 * <pre>
 *  ...
 *  property.openEditor();
 *  FormCustomEditorOperator fceo = new FormCustomEditorOperator(property.getName());
 *  fceo.setMode("PointEditor");
 *  PointCustomEditorOperator pceo = new PointCustomEditorOperator(fceo);
 *  pceo.setPointValue(...);
 *  fceo.ok(); //or pceo.ok(); it does not matter
 * </pre>
 * @author as103278
 * @version 1.0 */
public class FormCustomEditorOperator extends NbDialogOperator {

    /** Search for FormCustomEditor with defined title
     * @throws TimeoutExpiredException when NbDialog not found
     * @param title title of FormCustomEditor (mostly property name) */
    public FormCustomEditorOperator(String title) {
        super(title);
    }

    private JButtonOperator _btDefault;
    private JComboBoxOperator _cboMode;

    /** Tries to find "Default" JButton in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JButtonOperator
     */
    public JButtonOperator btDefault() {
        if (_btDefault==null) {
            _btDefault = new JButtonOperator(this, Bundle.getString(
                                    "org.openide.explorer.propertysheet.Bundle",
                                    "CTL_Default"));
        }
        return _btDefault;
    }

    /** Tries to find JComboBox in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JComboBoxOperator */
    public JComboBoxOperator cboMode() {
        if (_cboMode==null) {
            _cboMode = new JComboBoxOperator(this);
        }
        return _cboMode;
    }

    /** clicks on "Default" JButton
     * @throws TimeoutExpiredException when JButton not found
     */
    public void setDefault() {
        btDefault().push();
    }

    /** getter for currenlty selected mode
     * @return String mode name */    
    public String getMode() {
        return cboMode().getSelectedItem().toString();
    }
    
    /** tries to find cboMode and select item
     * @param mode String FormCustomEditor mode name */
    public void setMode(String mode) {
        // need to wait a little
        new EventTool().waitNoEvent(300);
        cboMode().selectItem(mode);
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        btDefault();
        cboMode();
    }

}

