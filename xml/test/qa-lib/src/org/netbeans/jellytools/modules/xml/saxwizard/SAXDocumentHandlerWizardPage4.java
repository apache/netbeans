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
 * SAXDocumentHandlerWizardPage4.java
 *
 * Created on 9/3/02 7:44 PM
 */
package org.netbeans.jellytools.modules.xml.saxwizard;

import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling "SAX Document Handler Wizard" NbDialog.
 *
 * @author ms113234
 * @version 1.0
 */
public class SAXDocumentHandlerWizardPage4 extends WizardOperator {

    /** Creates new SAXDocumentHandlerWizardPage4 that can handle it.
     */
    public SAXDocumentHandlerWizardPage4() {
        super("SAX Document Handler Wizard");
    }

    private JLabelOperator _lblSteps;
    private JListOperator _lstSteps;
    private JLabelOperator _lbl4Of4OutputFileNames;
    private JTextAreaOperator _txtJTextArea;
    private JLabelOperator _lblHandlerInterface;
    private JTextFieldOperator _txtHandlerInterface;
    private JLabelOperator _lblHandlerImplementation;
    private JTextFieldOperator _txtHandlerImplementation;
    private JLabelOperator _lblGeneratedParser;
    private JTextFieldOperator _txtGeneratedParser;
    private JLabelOperator _lblDataConvertorInterface;
    private JTextFieldOperator _txtDataConvertorInterface;
    private JLabelOperator _lblDataConvertorImplementation;
    private JTextFieldOperator _txtDataConvertorImplementation;
    private JCheckBoxOperator _cbSaveCustomizedBindings;
    private JLabelOperator _lblLocation;
    private JTextFieldOperator _txtLocation;
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

    /** Tries to find null JList in this dialog.
     * @return JListOperator
     */
    public JListOperator lstSteps() {
        if (_lstSteps==null) {
            _lstSteps = new JListOperator(this);
        }
        return _lstSteps;
    }

    /** Tries to find "4 of 4 - Output File Names" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lbl4Of4OutputFileNames() {
        if (_lbl4Of4OutputFileNames==null) {
            _lbl4Of4OutputFileNames = new JLabelOperator(this, "4 of 4 - Output File Names");
        }
        return _lbl4Of4OutputFileNames;
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

    /** Tries to find "Handler Interface:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblHandlerInterface() {
        if (_lblHandlerInterface==null) {
            _lblHandlerInterface = new JLabelOperator(this, "Handler Interface:");
        }
        return _lblHandlerInterface;
    }

    /** Tries to find null ValidatingTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtHandlerInterface() {
        if (_txtHandlerInterface==null) {
            _txtHandlerInterface = new JTextFieldOperator(this);
        }
        return _txtHandlerInterface;
    }

    /** Tries to find "Handler Implementation:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblHandlerImplementation() {
        if (_lblHandlerImplementation==null) {
            _lblHandlerImplementation = new JLabelOperator(this, "Handler Implementation:");
        }
        return _lblHandlerImplementation;
    }

    /** Tries to find null ValidatingTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtHandlerImplementation() {
        if (_txtHandlerImplementation==null) {
            _txtHandlerImplementation = new JTextFieldOperator(this, 1);
        }
        return _txtHandlerImplementation;
    }

    /** Tries to find "Generated Parser:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblGeneratedParser() {
        if (_lblGeneratedParser==null) {
            _lblGeneratedParser = new JLabelOperator(this, "Generated Parser:");
        }
        return _lblGeneratedParser;
    }

    /** Tries to find null ValidatingTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtGeneratedParser() {
        if (_txtGeneratedParser==null) {
            _txtGeneratedParser = new JTextFieldOperator(this, 2);
        }
        return _txtGeneratedParser;
    }

    /** Tries to find "Data Convertor Interface:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblDataConvertorInterface() {
        if (_lblDataConvertorInterface==null) {
            _lblDataConvertorInterface = new JLabelOperator(this, "Data Convertor Interface:");
        }
        return _lblDataConvertorInterface;
    }

    /** Tries to find null ValidatingTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtDataConvertorInterface() {
        if (_txtDataConvertorInterface==null) {
            _txtDataConvertorInterface = new JTextFieldOperator(this, 3);
        }
        return _txtDataConvertorInterface;
    }

    /** Tries to find "Data Convertor Implementation:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblDataConvertorImplementation() {
        if (_lblDataConvertorImplementation==null) {
            _lblDataConvertorImplementation = new JLabelOperator(this, "Data Convertor Implementation:");
        }
        return _lblDataConvertorImplementation;
    }

    /** Tries to find null ValidatingTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtDataConvertorImplementation() {
        if (_txtDataConvertorImplementation==null) {
            _txtDataConvertorImplementation = new JTextFieldOperator(this, 4);
        }
        return _txtDataConvertorImplementation;
    }

    /** Tries to find " Save Customized Bindings" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbSaveCustomizedBindings() {
        if (_cbSaveCustomizedBindings==null) {
            _cbSaveCustomizedBindings = new JCheckBoxOperator(this, " Save Customized Bindings");
        }
        return _cbSaveCustomizedBindings;
    }

    /** Tries to find "Location:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblLocation() {
        if (_lblLocation==null) {
            _lblLocation = new JLabelOperator(this, "Location:");
        }
        return _lblLocation;
    }

    /** Tries to find null ValidatingTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtLocation() {
        if (_txtLocation==null) {
            _txtLocation = new JTextFieldOperator(this, 5);
        }
        return _txtLocation;
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

    /** gets text for txtHandlerInterface
     * @return String text
     */
    public String getHandlerInterface() {
        return txtHandlerInterface().getText();
    }

    /** sets text for txtHandlerInterface
     * @param text String text
     */
    public void setHandlerInterface(String text) {
        txtHandlerInterface().setText(text);
    }

    /** types text for txtHandlerInterface
     * @param text String text
     */
    public void typeHandlerInterface(String text) {
        txtHandlerInterface().typeText(text);
    }

    /** gets text for txtHandlerImplementation
     * @return String text
     */
    public String getHandlerImplementation() {
        return txtHandlerImplementation().getText();
    }

    /** sets text for txtHandlerImplementation
     * @param text String text
     */
    public void setHandlerImplementation(String text) {
        txtHandlerImplementation().setText(text);
    }

    /** types text for txtHandlerImplementation
     * @param text String text
     */
    public void typeHandlerImplementation(String text) {
        txtHandlerImplementation().typeText(text);
    }

    /** gets text for txtGeneratedParser
     * @return String text
     */
    public String getGeneratedParser() {
        return txtGeneratedParser().getText();
    }

    /** sets text for txtGeneratedParser
     * @param text String text
     */
    public void setGeneratedParser(String text) {
        txtGeneratedParser().setText(text);
    }

    /** types text for txtGeneratedParser
     * @param text String text
     */
    public void typeGeneratedParser(String text) {
        txtGeneratedParser().typeText(text);
    }

    /** gets text for txtDataConvertorInterface
     * @return String text
     */
    public String getDataConvertorInterface() {
        return txtDataConvertorInterface().getText();
    }

    /** sets text for txtDataConvertorInterface
     * @param text String text
     */
    public void setDataConvertorInterface(String text) {
        txtDataConvertorInterface().setText(text);
    }

    /** types text for txtDataConvertorInterface
     * @param text String text
     */
    public void typeDataConvertorInterface(String text) {
        txtDataConvertorInterface().typeText(text);
    }

    /** gets text for txtDataConvertorImplementation
     * @return String text
     */
    public String getDataConvertorImplementation() {
        return txtDataConvertorImplementation().getText();
    }

    /** sets text for txtDataConvertorImplementation
     * @param text String text
     */
    public void setDataConvertorImplementation(String text) {
        txtDataConvertorImplementation().setText(text);
    }

    /** types text for txtDataConvertorImplementation
     * @param text String text
     */
    public void typeDataConvertorImplementation(String text) {
        txtDataConvertorImplementation().typeText(text);
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkSaveCustomizedBindings(boolean state) {
        if (cbSaveCustomizedBindings().isSelected()!=state) {
            cbSaveCustomizedBindings().push();
        }
    }

    /** gets text for txtLocation
     * @return String text
     */
    public String getBindingsLocation() {
        return txtLocation().getText();
    }

    /** sets text for txtLocation
     * @param text String text
     */
    public void setLocation(String text) {
        txtLocation().setText(text);
    }

    /** types text for txtLocation
     * @param text String text
     */
    public void typeLocation(String text) {
        txtLocation().typeText(text);
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


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of SAXDocumentHandlerWizardPage4 by accessing all its components.
     */
    public void verify() {
        lblSteps();
        lstSteps();
        lbl4Of4OutputFileNames();
        txtJTextArea();
        lblHandlerInterface();
        txtHandlerInterface();
        lblHandlerImplementation();
        txtHandlerImplementation();
        lblGeneratedParser();
        txtGeneratedParser();
        lblDataConvertorInterface();
        txtDataConvertorInterface();
        lblDataConvertorImplementation();
        txtDataConvertorImplementation();
        cbSaveCustomizedBindings();
        lblLocation();
        txtLocation();
        btCancel();
        btHelp();
    }

    /** Performs simple test of SAXDocumentHandlerWizardPage4
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        new SAXDocumentHandlerWizardPage4().verify();
        System.out.println("SAXDocumentHandlerWizardPage4 verification finished.");
    }
}

