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
 * SAXDocumentHandlerWizardPage3.java
 *
 * Created on 8/5/02 4:17 PM
 */
package org.netbeans.jellytools.modules.xml.saxwizard;

import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling "SAX Document Handler Wizard" NbDialog.
 *
 * @author ms113234
 * @version 1.0
 */
public class SAXDocumentHandlerWizardPage3 extends WizardOperator {
    static final int ELEMENT_COLUMN = 0;
    static final int METHOD_COLUMN = 1;
    static final int RET_TYPE_COLUMN = 2;
    
    public static final String NO_METHOD = "[none]";
    
    /** Creates new SAXDocumentHandlerWizardPage3 that can handle it.
     */
    public SAXDocumentHandlerWizardPage3() {
        super("SAX Document Handler Wizard");
    }
    
    private JLabelOperator _lblSteps;
    private JLabelOperator _lbl3Of4DataConvertorsOptional;
    private JTextAreaOperator _txtJTextArea;
    private JTableOperator _tabDataConvertors;
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
    
    /** Tries to find "3 of 4 - Data Convertors (Optional)" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lbl3Of4DataConvertorsOptional() {
        if (_lbl3Of4DataConvertorsOptional==null) {
            _lbl3Of4DataConvertorsOptional = new JLabelOperator(this, "3 of 4 - Data Convertors (Optional)");
        }
        return _lbl3Of4DataConvertorsOptional;
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
    
    /** Tries to find null SAXGeneratorParsletPanel$ParsletsTable in this dialog.
     * @return JTableOperator
     */
    public JTableOperator tabDataConvertors() {
        if (_tabDataConvertors==null) {
            _tabDataConvertors = new JTableOperator(this);
        }
        return _tabDataConvertors;
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
    
    /** Tries to find JTextField in Element column in Data Convertors table.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtElement(int row) {
        tabDataConvertors().clickForEdit(row, ELEMENT_COLUMN);
        return new JTextFieldOperator(tabDataConvertors());
    }
    
    /** Tries to find JTextField in Convertor Method column in Data Convertors table.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtConvertorMethod(int row) {
        tabDataConvertors().clickForEdit(row, METHOD_COLUMN);
        return new JTextFieldOperator(tabDataConvertors());
    }
    
    /** Tries to find JComboBox in Convertor Method column in Data Convertors table.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboConvertorMethod(int row) {
        tabDataConvertors().clickForEdit(row, METHOD_COLUMN);
        return new JComboBoxOperator(tabDataConvertors());
    }
    
    /** Tries to find JTextField in Return Type column in Data Convertors table.
     * @return JTextFieldOperator
     * @param row row nuber
     */
    public JTextFieldOperator txtReturnType(int row) {
        tabDataConvertors().clickForEdit(row, RET_TYPE_COLUMN);
        return new JTextFieldOperator(tabDataConvertors());
    }
    
    /** Tries to find JComboBox in Return Type column in Data Convertors table.
     * @return JComboBoxOperator
     * @param row row nuber
     */
    public JComboBoxOperator cboReturnType(int row) {
        tabDataConvertors().clickForEdit(row, RET_TYPE_COLUMN);
        return new JComboBoxOperator(tabDataConvertors());
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
    
    /** gets text for txtElement
     * @return String text
     */
    public String getElement(int row) {
        return txtElement(row).getText();
    }
    
    /** gets text for txtConvertorMethod
     * @return String text
     */
    public String getConvertorMethod(int row) {
        return txtConvertorMethod(row).getText();
    }
    
    /** sets text for txtConvertorMethod
     * @param text String text
     */
    public void setConvertorMethod(int row, String text) {
        txtConvertorMethod(row).enterText(text);
    }
    
    /** returns selected item for cboConvertorMethod
     * @return String item
     */
    public String getSelectedConvertorMethod(int row) {
        return cboConvertorMethod(row).getSelectedItem().toString();
    }
    
    /** selects item for cboConvertorMethod
     * @param item String item
     */
    public void selectConvertorMethod(int row, String item) {
        cboConvertorMethod(row).selectItem(item);
    }
    
    /** gets text for txtReturnType
     * @return String text
     */
    public String getReturnType(int row) {
        return txtReturnType(row).getText();
    }
    
    /** sets text for txtReturnType
     * @param text String text
     */
    public void setReturnType(int row, String text) {
        txtReturnType(row).enterText(text);
    }
    
    /** returns selected item for cboReturnType
     * @return String item
     */
    public String getSelectedReturnType(int row) {
        return cboReturnType(row).getSelectedItem().toString();
    }
    
    /** selects item for cboReturnType
     * @param item String item
     */
    public void selectReturnType(int row, String item) {
        cboReturnType(row).selectItem(item);
    }

    //*****************************************
    // High-level functionality definition part
    //*****************************************
    
   /** finds row in Element Mappings table by element, if element is not present
     * returns -1
     */
    public int findTabRow(String element) {
        int rows = tabDataConvertors().getRowCount();
        for (int i = 0; i < rows; i++) {
            String cellVal = tabDataConvertors().getCellEditor(i, ELEMENT_COLUMN).getCellEditorValue().toString();
            if (element.equals(cellVal)) return i;
        }
        return -1;
    }
    
    /** Performs verification of SAXDocumentHandlerWizardPage3 by accessing all its components.
     */
    public void verify() {
        lblSteps();
        lbl3Of4DataConvertorsOptional();
        txtJTextArea();
        tabDataConvertors();
        btCancel();
        btHelp();
    }
    
    /** Performs simple test of SAXDocumentHandlerWizardPage3
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        new SAXDocumentHandlerWizardPage3().verify();
        System.out.println("SAXDocumentHandlerWizardPage3 verification finished.");
    }
}

