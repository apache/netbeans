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
package org.netbeans.jellytools;

import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import javax.swing.JDialog;

/**
 * Handles Build and Run Actions panel of New Project wizard
 * for J2SE Ant Project.<br>
 *
 * Usage:
 * <pre>
 * BuildAndRunActionsStepOperator brop = new BuildAndRunActionsStepOperator();
 * brop.selectBuild("clean");
 * brop.selectClean("clean");
 * brop.selectRun("clean");
 * brop.selectGenerateJavadoc("clean");
 * brop.selectTest("clean");
 * </pre>
 *
 * @author tb115823
 */
public class BuildAndRunActionsStepOperator extends NewProjectWizardOperator {
    
    private JLabelOperator _lblBuild;
    private JLabelOperator _lblClean;
    private JLabelOperator _lblRun;
    private JLabelOperator _lblGenerateJavadoc;
    private JLabelOperator _lblTest;
    private JLabelOperator _lblOnlineError;
    private JComboBoxOperator _cboBuild;
    private JComboBoxOperator _cboClean;
    private JComboBoxOperator _cboRun;
    private JComboBoxOperator _cboGenerateJavadoc;
    private JComboBoxOperator _cboTest;
    //TODO: test this class
    
    
    /** Tries to find "Build:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblBuild() {
        if (_lblBuild==null) {
            String buildProject = Bundle.getString("org.netbeans.modules.ant.freeform.ui.Bundle", "LBL_TargetMappingPanel_jLabel2");
            _lblBuild = new JLabelOperator(this, buildProject);//I18N
        }
        return _lblBuild;
    }

    /** Tries to find "Clean:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblClean() {
        if (_lblClean==null) {
            String cleanProject = Bundle.getString("org.netbeans.modules.ant.freeform.ui.Bundle", "LBL_TargetMappingPanel_jLabel4");
            _lblClean = new JLabelOperator(this, cleanProject);//I18N
        }
        return _lblClean;
    }

    /** Tries to find "Run:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblRun() {
        if (_lblRun==null) {
            String runProject = Bundle.getString("org.netbeans.modules.ant.freeform.ui.Bundle", "LBL_TargetMappingPanel_jLabel5");
            _lblRun = new JLabelOperator(this, runProject);//I18N
        }
        return _lblRun;
    }

    /** Tries to find "Generate Javadoc:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblGenerateJavadoc() {
        if (_lblGenerateJavadoc==null) {
            String generateJavadoc = Bundle.getString("org.netbeans.modules.ant.freeform.ui.Bundle", "LBL_TargetMappingPanel_jLabel6");
            _lblGenerateJavadoc = new JLabelOperator(this, generateJavadoc);//I18N
        }
        return _lblGenerateJavadoc;
    }

    /** Tries to find "Test:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblTest() {
        if (_lblTest==null) {
            String testProject = Bundle.getString("org.netbeans.modules.ant.freeform.ui.Bundle", "LBL_TargetMappingPanel_jLabel7");
            _lblTest = new JLabelOperator(this, testProject);//I18N
        }
        return _lblTest;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboBuild() {
        if (_cboBuild==null) {
            _cboBuild = new JComboBoxOperator(this);
        }
        return _cboBuild;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboClean() {
        if (_cboClean==null) {
            _cboClean = new JComboBoxOperator(this, 1);
        }
        return _cboClean;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboRun() {
        if (_cboRun==null) {
            _cboRun = new JComboBoxOperator(this, 2);
        }
        return _cboRun;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboGenerateJavadoc() {
        if (_cboGenerateJavadoc==null) {
            _cboGenerateJavadoc = new JComboBoxOperator(this, 3);
        }
        return _cboGenerateJavadoc;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboTest() {
        if (_cboTest==null) {
            _cboTest = new JComboBoxOperator(this, 4);
        }
        return _cboTest;
    }

    /** Tries to find " " JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblOnlineError() {
        if (_lblOnlineError==null) {
            _lblOnlineError = new JLabelOperator(this, " ", 3);
        }
        return _lblOnlineError;
    }


    
    /** returns selected item for cboBuild
     * @return String item
     */
    public String getSelectedBuild() {
        return cboBuild().getSelectedItem().toString();
    }

    /** selects item for cboBuild
     * @param item String item
     */
    public void selectBuild(String item) {
        cboBuild().selectItem(item);
    }

    /** returns selected item for cboClean
     * @return String item
     */
    public String getSelectedClean() {
        return cboClean().getSelectedItem().toString();
    }

    /** selects item for cboClean
     * @param item String item
     */
    public void selectClean(String item) {
        cboClean().selectItem(item);
    }

    /** returns selected item for cboRun
     * @return String item
     */
    public String getSelectedRun() {
        return cboRun().getSelectedItem().toString();
    }

    /** selects item for cboRun
     * @param item String item
     */
    public void selectRun(String item) {
        cboRun().selectItem(item);
    }

    /** returns selected item for cboGenerateJavadoc
     * @return String item
     */
    public String getSelectedGenerateJavadoc() {
        return cboGenerateJavadoc().getSelectedItem().toString();
    }

    /** selects item for cboGenerateJavadoc
     * @param item String item
     */
    public void selectGenerateJavadoc(String item) {
        cboGenerateJavadoc().selectItem(item);
    }

    /** returns selected item for cboTest
     * @return String item
     */
    public String getSelectedTest() {
        return cboTest().getSelectedItem().toString();
    }

    /** selects item for cboTest
     * @param item String item
     */
    public void selectTest(String item) {
        cboTest().selectItem(item);
    }


    /** Performs verification of NewJ2SEAntProject by accessing all its components.
     */
    public void verify() {
        lblBuild();
        lblClean();
        lblRun();
        lblGenerateJavadoc();
        lblTest();
        cboBuild();
        cboClean();
        cboRun();
        cboGenerateJavadoc();
        cboTest();
        lblOnlineError();
    }

}
