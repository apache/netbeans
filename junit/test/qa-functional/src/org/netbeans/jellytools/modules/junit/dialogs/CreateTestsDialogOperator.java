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

package org.netbeans.jellytools.modules.junit.dialogs;

import java.io.PrintStream;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling "Create Tests" NbDialog.
 *
 * @author dave
 * @version 1.0
 */
public class CreateTestsDialogOperator extends NbDialogOperator {

    /** Creates new CreateTestsDialogOperator that can handle it.
     */
    public CreateTestsDialogOperator() {
        super(Bundle.getString("org/netbeans/modules/junit/Bundle", "JUnitCfgOfCreate.Title"));
    }

    private JLabelOperator _lblFileSystem;
    private JComboBoxOperator _cboFileSystem;
    private JLabelOperator _lblSuiteClassTemplate;
    private JLabelOperator _lblTestClass;
    private JComboBoxOperator _cboSuiteClassTemplate;
    public static final String ITEM_SIMPLEJUNITTEST = "SimpleJUnitTest";
    private JComboBoxOperator _cboTestClass;
    private JCheckBoxOperator _cbPublicMethods;
    private JCheckBoxOperator _cbProtectedMethods;
    private JCheckBoxOperator _cbPackageMethods;
    private JCheckBoxOperator _cbComments;
    private JCheckBoxOperator _cbDefaultBodies;
    private JCheckBoxOperator _cbJavaDoc;
    private JCheckBoxOperator _cbIncludeExceptionClasses;
    private JCheckBoxOperator _cbIncludeAbstractClasses;
    private JCheckBoxOperator _cbGenerateSuites;
    private JCheckBoxOperator _cbIncludePackagePrivateClasses;
    private JCheckBoxOperator _cbShowCreateTestsConfigurationDialog;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "File System:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblFileSystem() {
        if (_lblFileSystem==null) {
            _lblFileSystem = new JLabelOperator(this, Bundle.getString("org/netbeans/modules/junit/Bundle", "JUnitCfgOfCreate.lblFileSystem.text"));
        }
        return _lblFileSystem;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboFileSystem() {
        if (_cboFileSystem==null) {
            _cboFileSystem = new JComboBoxOperator(this);
        }
        return _cboFileSystem;
    }

    /** Tries to find "Suite Class Template:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSuiteClass() {
        if (_lblSuiteClassTemplate==null) {
            _lblSuiteClassTemplate = new JLabelOperator(this, Bundle.getString("org/netbeans/modules/junit/Bundle", "JUnitCfgOfCreate.lblSuiteClass.text"));
        }
        return _lblSuiteClassTemplate;
    }

    /** Tries to find "Test Class:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblTestClass() {
        if (_lblTestClass==null) {
            _lblTestClass = new JLabelOperator(this, Bundle.getString("org/netbeans/modules/junit/Bundle", "JUnitCfgOfCreate.lblTestClass.text"));
        }
        return _lblTestClass;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboSuiteClass() {
        if (_cboSuiteClassTemplate==null) {
            _cboSuiteClassTemplate = new JComboBoxOperator(this, 1);
        }
        return _cboSuiteClassTemplate;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboTestClass() {
        if (_cboTestClass==null) {
            _cboTestClass = new JComboBoxOperator(this, 2);
        }
        return _cboTestClass;
    }

    /** Tries to find " Public Methods" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbPublicMethods() {
        if (_cbPublicMethods==null) {
            _cbPublicMethods = new JCheckBoxOperator(this, Bundle.getString("org/netbeans/modules/junit/Bundle", "JUnitCfgOfCreate.chkPublic.text"));
        }
        return _cbPublicMethods;
    }

    /** Tries to find " Protected Methods" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbProtectedMethods() {
        if (_cbProtectedMethods==null) {
            _cbProtectedMethods = new JCheckBoxOperator(this, Bundle.getString("org/netbeans/modules/junit/Bundle", "JUnitCfgOfCreate.chkProtected.text"));
        }
        return _cbProtectedMethods;
    }

    /** Tries to find " Package Methods" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbPackageMethods() {
        if (_cbPackageMethods==null) {
            _cbPackageMethods = new JCheckBoxOperator(this, Bundle.getString("org/netbeans/modules/junit/Bundle", "JUnitCfgOfCreate.chkPackage.text"));
        }
        return _cbPackageMethods;
    }

    /** Tries to find " Comments" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbComments() {
        if (_cbComments==null) {
            _cbComments = new JCheckBoxOperator(this, Bundle.getString("org/netbeans/modules/junit/Bundle", "JUnitCfgOfCreate.chkComments.text"));
        }
        return _cbComments;
    }

    /** Tries to find " Default Bodies" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbDefaultBodies() {
        if (_cbDefaultBodies==null) {
            _cbDefaultBodies = new JCheckBoxOperator(this, Bundle.getString("org/netbeans/modules/junit/Bundle", "JUnitCfgOfCreate.chkContent.text"));
        }
        return _cbDefaultBodies;
    }

    /** Tries to find " JavaDoc" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbJavaDoc() {
        if (_cbJavaDoc==null) {
            _cbJavaDoc = new JCheckBoxOperator(this, Bundle.getString("org/netbeans/modules/junit/Bundle", "JUnitCfgOfCreate.chkJavaDoc.text"));
        }
        return _cbJavaDoc;
    }

    /** Tries to find " Include Exception Classes" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbIncludeExceptionClasses() {
        if (_cbIncludeExceptionClasses==null) {
            _cbIncludeExceptionClasses = new JCheckBoxOperator(this, Bundle.getString("org/netbeans/modules/junit/Bundle", "JUnitCfgOfCreate.chkExceptions.text"));
        }
        return _cbIncludeExceptionClasses;
    }

    /** Tries to find " Include Abstract Classes" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbIncludeAbstractClasses() {
        if (_cbIncludeAbstractClasses==null) {
            _cbIncludeAbstractClasses = new JCheckBoxOperator(this, Bundle.getString("org/netbeans/modules/junit/Bundle", "JUnitCfgOfCreate.chkAbstractImpl.text"));
        }
        return _cbIncludeAbstractClasses;
    }

    /** Tries to find " Generate Suites" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbGenerateSuites() {
        if (_cbGenerateSuites==null) {
            _cbGenerateSuites = new JCheckBoxOperator(this, Bundle.getString("org/netbeans/modules/junit/Bundle", "JUnitCfgOfCreate.chkGenerateSuites.text"));
        }
        return _cbGenerateSuites;
    }

    /** Tries to find " Include Package Private Classes" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbIncludePackagePrivateClasses() {
        if (_cbIncludePackagePrivateClasses==null) {
            _cbIncludePackagePrivateClasses = new JCheckBoxOperator(this, Bundle.getString("org/netbeans/modules/junit/Bundle", "JUnitCfgOfCreate.chkPackagePrivateClasses.text"));
        }
        return _cbIncludePackagePrivateClasses;
    }

    /** Tries to find " Show Create Tests Configuration Dialog" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbShowCreateTestsConfigurationDialog() {
        if (_cbShowCreateTestsConfigurationDialog==null) {
            _cbShowCreateTestsConfigurationDialog = new JCheckBoxOperator(this, Bundle.getString("org/netbeans/modules/junit/Bundle", "JUnitCfgOfCreate.chkEnabled.text"));
        }
        return _cbShowCreateTestsConfigurationDialog;
    }


    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** returns selected item for cboFileSystem
     * @return String item
     */
    public String getSelectedFileSystem() {
        return cboFileSystem().getSelectedItem().toString();
    }

    /** selects item for cboFileSystem
     * @param item String item
     */
    public void selectFileSystem(String item) {
        cboFileSystem().selectItem(item);
    }

    /** types text for cboFileSystem
     * @param text String text
     */
    public void typeFileSystem(String text) {
        cboFileSystem().typeText(text);
    }

    /** returns selected item for cboSuiteClassTemplate
     * @return String item
     */
    public String getSelectedSuiteClass() {
        return cboSuiteClass().getSelectedItem().toString();
    }

    /** selects item for cboSuiteClassTemplate
     * @param item String item
     */
    public void selectSuiteClass(String item) {
        cboSuiteClass().selectItem(item);
    }

    /** types text for cboSuiteClassTemplate
     * @param text String text
     */
    public void typeSuiteClass(String text) {
        cboSuiteClass().typeText(text);
    }

    /** returns selected item for cboTestClass
     * @return String item
     */
    public String getSelectedTestClass() {
        return cboTestClass().getSelectedItem().toString();
    }

    /** selects item for cboTestClass
     * @param item String item
     */
    public void selectTestClass(String item) {
        cboTestClass().selectItem(item);
    }

    /** types text for cboTestClass
     * @param text String text
     */
    public void typeTestClass(String text) {
        cboTestClass().typeText(text);
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkPublicMethods(boolean state) {
        if (cbPublicMethods().isSelected()!=state) {
            cbPublicMethods().push();
        }
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkProtectedMethods(boolean state) {
        if (cbProtectedMethods().isSelected()!=state) {
            cbProtectedMethods().push();
        }
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkPackageMethods(boolean state) {
        if (cbPackageMethods().isSelected()!=state) {
            cbPackageMethods().push();
        }
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkComments(boolean state) {
        if (cbComments().isSelected()!=state) {
            cbComments().push();
        }
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkDefaultBodies(boolean state) {
        if (cbDefaultBodies().isSelected()!=state) {
            cbDefaultBodies().push();
        }
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkJavaDoc(boolean state) {
        if (cbJavaDoc().isSelected()!=state) {
            cbJavaDoc().push();
        }
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkIncludeExceptionClasses(boolean state) {
        if (cbIncludeExceptionClasses().isSelected()!=state) {
            cbIncludeExceptionClasses().push();
        }
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkIncludeAbstractClasses(boolean state) {
        if (cbIncludeAbstractClasses().isSelected()!=state) {
            cbIncludeAbstractClasses().push();
        }
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkGenerateSuites(boolean state) {
        if (cbGenerateSuites().isSelected()!=state) {
            cbGenerateSuites().push();
        }
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkIncludePackagePrivateClasses(boolean state) {
        if (cbIncludePackagePrivateClasses().isSelected()!=state) {
            cbIncludePackagePrivateClasses().push();
        }
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkShowCreateTestsConfigurationDialog(boolean state) {
        if (cbShowCreateTestsConfigurationDialog().isSelected()!=state) {
            cbShowCreateTestsConfigurationDialog().push();
        }
    }


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of CreateTestsDialogOperator by accessing all its components.
     */
    public void verify() {
        lblFileSystem();
        cboFileSystem();
        lblSuiteClass();
        lblTestClass();
        cboSuiteClass();
        cboTestClass();
        cbPublicMethods();
        cbProtectedMethods();
        cbPackageMethods();
        cbComments();
        cbDefaultBodies();
        cbJavaDoc();
        cbIncludeExceptionClasses();
        cbIncludeAbstractClasses();
        cbGenerateSuites();
        cbIncludePackagePrivateClasses();
        cbShowCreateTestsConfigurationDialog();
    }

    /** Performs simple test of CreateTestsDialogOperator
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        new CreateTestsDialogOperator().verify();
        System.out.println("CreateTestsDialogOperator verification finished.");
    }

    public void dumpAll (PrintStream out, String fs) {
        Object o = cboFileSystem().getSelectedItem();
        String str = (o != null) ? o.toString () : "<NULL_VALUE>";
        if (str.startsWith (fs))
            str = "<TARGET_FS>" + str.substring (fs.length ());
        out.println ("FileSystem: " + str);
        out.println ("SuiteClass: " + cboSuiteClass().getSelectedItem());
        out.println ("TestClass: " + cboTestClass().getSelectedItem());
        out.println ("PublicMethod: " + cbPublicMethods().isSelected());
        out.println ("ProtectedMethod: " + cbProtectedMethods().isSelected());
        out.println ("PackageMethod: " + cbPackageMethods().isSelected());
        out.println ("IncludeAbstract: " + cbIncludeAbstractClasses().isSelected());
        out.println ("IncludeException: " + cbIncludeExceptionClasses().isSelected());
        out.println ("IncludePackagePrivateClass: " + cbIncludePackagePrivateClasses().isSelected());
        out.println ("Comments: " + cbComments().isSelected());
        out.println ("JavaDoc: " + cbJavaDoc().isSelected());
        out.println ("DefaultBodies: " + cbDefaultBodies().isSelected());
        out.println ("GenerateSuites: " + cbGenerateSuites().isSelected());
        out.println ("ShowDialog: " + cbShowCreateTestsConfigurationDialog().isSelected());
    }

}
