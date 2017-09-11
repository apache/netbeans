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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

