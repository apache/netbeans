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
 * SAXDocumentHandlerWizard2.java
 *
 * Created on 8/5/02 4:15 PM
 */
package org.netbeans.jellytools.modules.xml.saxwizard;

import java.awt.Component;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.util.Dumper;

/** Class implementing all necessary methods for handling "SAX Document Handler Wizard" NbDialog.
 *
 * @author ms113234
 * @version 1.0
 */
public class SAXDocumentHandlerWizardPage2 extends WizardOperator {
    static final int ELEMENT_COLUMN = 0;
    static final int TYPE_COLUMN = 1;
    static final int METHOD_COLUMN = 2;
    
    public static final String IGNORE = "Ignore";
    public static final String DATA = "Data";
    public static final String CONTAINER = "Container";
    public static final String MIXED = "Mixed";
    public static final String MIXED_CONTAINER = "Mixed Container";
    
    /** Creates new SAXDocumentHandlerWizard2 that can handle it.
     */
    public SAXDocumentHandlerWizardPage2() {
        super("SAX Document Handler Wizard");
    }
    
    private JLabelOperator _lblSteps;
    private JLabelOperator _lbl2Of4ElementMappings;
    private JTextAreaOperator _txtJTextArea;
    private JTableOperator _tabElementMappings;
    private JButtonOperator _btCancel;
    private JButtonOperator _btHelp;
    
    
    //******************************
    // Subcomponents definition part
    //******************************
    
    /** Tries to find "Steps" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSteps() {
        if (_lblSteps==null) {
            _lblSteps = new JLabelOperator(this, "Steps");
        }
        return _lblSteps;
    }
    
    /** Tries to find "2 of 4 - Element Mappings" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lbl2Of4ElementMappings() {
        if (_lbl2Of4ElementMappings==null) {
            _lbl2Of4ElementMappings = new JLabelOperator(this, "2 of 4 - Element Mappings");
        }
        return _lbl2Of4ElementMappings;
    }
    
    /** Tries to find null JTextArea in this dialog.
     * @return JTextAreaOperator
     */
    public JTextAreaOperator txtJTextArea() {
        if (_txtJTextArea==null) {
            _txtJTextArea = new JTextAreaOperator(this);
        }
        return _txtJTextArea;
    }
    
    /** Tries to find null SAXGeneratorMethodPanel$MethodsTable in this dialog.
     * @return JTableOperator
     */
    public JTableOperator tabElementMappings() {
        if (_tabElementMappings==null) {
            _tabElementMappings = new JTableOperator(this);
        }
        return _tabElementMappings;
    }
    
    /** Tries to find "Cancel" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btCancel() {
        if (_btCancel==null) {
            _btCancel = new JButtonOperator(this, "Cancel");
        }
        return _btCancel;
    }
    
    /** Tries to find "Help" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btHelp() {
        if (_btHelp==null) {
            _btHelp = new JButtonOperator(this, "Help");
        }
        return _btHelp;
    }
    
    //==========================================================================
    
    /** Tries to find ValidatingTextField in Hnadler Method column in Element Mappings table.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtElement(int row) {
        //return new JTextFieldOperator((JTextField) getTableCellEditorComponent(row, ELEMENT_COLUMN));
        tabElementMappings().clickForEdit(row, ELEMENT_COLUMN);
        return new JTextFieldOperator(tabElementMappings());
    }

    /** Tries to find JComboBox in Handler Type column in Element Mappings table.
     * @return JComboBoxOperator
     * @param row row nuber
     */
    public JComboBoxOperator cboHandlerType(int row) {
        tabElementMappings().clickForEdit(row, TYPE_COLUMN);
        return new JComboBoxOperator(tabElementMappings());
    }
    
    /** Tries to find ValidatingTextField in Hnadler Method column in Element Mappings table.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtHandlerMethod(int row) {
        tabElementMappings().clickForEdit(row, METHOD_COLUMN);
        return new JTextFieldOperator(tabElementMappings());
    }
    
    //****************************************
    // Low-level functionality definition part
    //****************************************
    
    /** gets text for txtJTextArea
     * @return String text
     */
    public String getJTextArea() {
        return txtJTextArea().getText();
    }
    
    /** sets text for txtJTextArea
     * @param text String text
     */
    public void setJTextArea(String text) {
        txtJTextArea().setText(text);
    }
    
    /** types text for txtJTextArea
     * @param text String text
     */
    public void typeJTextArea(String text) {
        txtJTextArea().typeText(text);
    }
    
    /** clicks on "Cancel" JButton
     */
    public void cancel() {
        btCancel().push();
    }
    
    /** clicks on "Help" JButton
     */
    public void help() {
        btHelp().push();
    }
    
    //==========================================================================
    
    /** returns selected item for cboHandlerType
     * @return String item
     */
    public String getSelectedHandlerType(int row) {
        return cboHandlerType(row).getSelectedItem().toString();
    }
    
    /** selects item for cboHandlerType
     * @param item String item
     */
    public void selectHandlerType(int row, String item) {
        cboHandlerType(row).selectItem(item);
    }
    
    /** gets text for txtHandlerMethod
     * @return String text
     */
    public String getHandlerMethod(int row) {
        return txtHandlerMethod(row).getText();
    }
    
    /** sets text for txtHandlerMethod
     * @param text String text
     */
    public void setHandlerMethod(int row, String text) {
        txtHandlerMethod(row).enterText(text);
    }
    
    /** gets text for txtElement
     * @return String text
     */
    public String getElement(int row) {
        return txtElement(row).getText();
    }
    
    //*****************************************
    // High-level functionality definition part
    //*****************************************
    
    /** finds row in Element Mappings table by element, if element is not present
     * returns -1
     */
    public int findTabRow(String element) {
        int rows = tabElementMappings().getRowCount();
        for (int i = 0; i < rows; i++) {
            String cellVal = tabElementMappings().getCellEditor(i, ELEMENT_COLUMN).getCellEditorValue().toString();
            if (element.equals(cellVal)) return i;
        }
        return -1;
    }
    
    /** Performs verification of SAXDocumentHandlerWizard2 by accessing all its components.
     */
    public void verify() {
        lblSteps();
        lbl2Of4ElementMappings();
        txtJTextArea();
        tabElementMappings();
        btCancel();
        btHelp();
    }
    
    /** Performs simple test of SAXDocumentHandlerWizard2
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        new SAXDocumentHandlerWizardPage2().verify();
        System.out.println("SAXDocumentHandlerWizardPage2 verification finished.");
    }
}

