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

package org.netbeans.jellytools.modules.web;

import javax.swing.JComboBox;
import javax.swing.JTable;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jemmy.operators.*;

//TODO: This dialog seems identical to the one in java freeform project wizard. Move to jellytools.java and rename?

/** Class implementing all necessary methods for handling "New Web Application
 * with Existing Ant Script - Source Package Folder" wizard step.
 *
 * @author Martin.Schovanek@sun.com
 * @version 1.0
 */
public class NewWebFreeFormSrcFoldersStepOperator extends WizardOperator{

    /** Creates new NewWebFreeFormSrcFoldersStepOperator that can handle it.
     */
    public NewWebFreeFormSrcFoldersStepOperator() {
        super(Helper.freeFormWizardTitle());
    }
    
    private JLabelOperator _lblSourcePackageFolders;
    private JLabelOperator _lblSourceLevel;
    private JComboBoxOperator _cboSourceLevel;
    public static final String ITEM_JDK13 = Bundle.getStringTrimmed(
            "org.netbeans.modules.java.freeform.ui.Bundle",
            "LBL_SourceFoldersPanel_JDK13");
    public static final String ITEM_JDK14 = Bundle.getStringTrimmed(
            "org.netbeans.modules.java.freeform.ui.Bundle",
            "LBL_SourceFoldersPanel_JDK14");
    public static final String ITEM_JDK15 = Bundle.getStringTrimmed(
            "org.netbeans.modules.java.freeform.ui.Bundle",
            "LBL_SourceFoldersPanel_JDK15");
    private JButtonOperator _btAddFolderSrc;
    private JButtonOperator _btRemoveSrc;
    private JTableOperator _tabSourcePackageFolders;
    private JLabelOperator _lblTestPackageFolders;
    private JButtonOperator _btAddFolderTest;
    private JButtonOperator _btRemoveTest;
    private JButtonOperator _btMoveUpSrc;
    private JButtonOperator _btMoveDownSrc;
    private JButtonOperator _btMoveDownTest;
    private JButtonOperator _btMoveUpTest;
    
    
    //******************************
    // Subcomponents definition part
    //******************************
    
    /** Tries to find "Source Package Folders:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSourcePackageFolders() {
        if (_lblSourcePackageFolders==null) {
            String sourcePkg = Bundle.getStringTrimmed(
                    "org.netbeans.modules.java.freeform.ui.Bundle",
                    "LBL_SourceFoldersPanel_jLabel2");
            _lblSourcePackageFolders = new JLabelOperator(this, sourcePkg);
        }
        return _lblSourcePackageFolders;
    }
    
    /** Tries to find "Source Level:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSourceLevel() {
        if (_lblSourceLevel==null) {
            String sourceLevel = Bundle.getStringTrimmed(
                    "org.netbeans.modules.java.freeform.ui.Bundle",
                    "LBL_SourceFoldersPanel_jLabel3");
            _lblSourceLevel = new JLabelOperator(this, sourceLevel);
        }
        return _lblSourceLevel;
    }
    
    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboSourceLevel() {
        if (_cboSourceLevel==null) {
            if (lblSourceLevel().getLabelFor()!=null) {
                _cboSourceLevel = new JComboBoxOperator(
                        (JComboBox) lblSourceLevel().getLabelFor());
            } else {
                _cboSourceLevel = new JComboBoxOperator(this);
            }
        }
        return _cboSourceLevel;
    }
    
    /** Tries to find "Add Folder..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btAddFolderSrc() {
        if (_btAddFolderSrc==null) {
            String addFolder = Bundle.getStringTrimmed(
                    "org.netbeans.modules.java.freeform.ui.Bundle",
                    "BTN_SourceFoldersPanel_addFolder");
            _btAddFolderSrc = new JButtonOperator(this, addFolder);
        }
        return _btAddFolderSrc;
    }
    
    /** Tries to find "Remove" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btRemoveSrc() {
        if (_btRemoveSrc==null) {
            String remove = Bundle.getStringTrimmed(
                    "org.netbeans.modules.java.freeform.ui.Bundle",
                    "BTN_SourceFoldersPanel_removeFolder");
            _btRemoveSrc = new JButtonOperator(this, remove);
        }
        return _btRemoveSrc;
    }
    
    /** Tries to find null JTable in this dialog.
     * @return JTableOperator
     */
    public JTableOperator tabSourcePackageFolders() {
        if (_tabSourcePackageFolders==null) {
            if (lblSourcePackageFolders().getLabelFor()!=null) {
                _tabSourcePackageFolders = new JTableOperator(
                        (JTable) lblSourcePackageFolders().getLabelFor());
            } else {
                _tabSourcePackageFolders = new JTableOperator(this);
            }
        }
        return _tabSourcePackageFolders;
    }
    
    /** Tries to find "Test Package Folders:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblTestPackageFolders() {
        if (_lblTestPackageFolders==null) {
            String testPkg = Bundle.getStringTrimmed(
                    "org.netbeans.modules.java.freeform.ui.Bundle",
                    "LBL_TestSourceFoldersPanel");
            _lblTestPackageFolders = new JLabelOperator(this, testPkg);
        }
        return _lblTestPackageFolders;
    }
    
    /** Tries to find "Add Folder..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btAddFolderTest() {
        if (_btAddFolderTest==null) {
            String addFolder = Bundle.getStringTrimmed(
                    "org.netbeans.modules.java.freeform.ui.Bundle",
                    "BTN_SourceFoldersPanel_addTestFolder");
            _btAddFolderTest = new JButtonOperator(this, addFolder, 1);
        }
        return _btAddFolderTest;
    }
    
    /** Tries to find "Remove" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btRemoveTest() {
        if (_btRemoveTest==null) {
            String remove = Bundle.getStringTrimmed(
                    "org.netbeans.modules.java.freeform.ui.Bundle",
                    "BTN_SourceFoldersPanel_removeTestFolder");
            _btRemoveTest = new JButtonOperator(this, remove, 1);
        }
        return _btRemoveTest;
    }
    
    /** Tries to find "Move Up" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btMoveUpSrc() {
        if (_btMoveUpSrc==null) {
            String moveUp = Bundle.getStringTrimmed(
                    "org.netbeans.modules.java.freeform.ui.Bundle",
                    "BTN_SourceFoldersPanel_upFolder");
            _btMoveUpSrc = new JButtonOperator(this, moveUp);
        }
        return _btMoveUpSrc;
    }
    
    /** Tries to find "Move Down" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btMoveDownSrc() {
        if (_btMoveDownSrc==null) {
            String moveDown = Bundle.getStringTrimmed(
                    "org.netbeans.modules.java.freeform.ui.Bundle",
                    "BTN_SourceFoldersPanel_downFolder");
            _btMoveDownSrc = new JButtonOperator(this, moveDown);
        }
        return _btMoveDownSrc;
    }
    
    /** Tries to find "Move Down" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btMoveDownTest() {
        if (_btMoveDownTest==null) {
            String moveDown = Bundle.getStringTrimmed(
                    "org.netbeans.modules.java.freeform.ui.Bundle",
                    "BTN_SourceFoldersPanel_downTestFolder");
            _btMoveDownTest = new JButtonOperator(this, moveDown, 1);
        }
        return _btMoveDownTest;
    }
    
    /** Tries to find "Move Up" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btMoveUpTest() {
        if (_btMoveUpTest==null) {
            String moveUp = Bundle.getStringTrimmed(
                    "org.netbeans.modules.java.freeform.ui.Bundle",
                    "BTN_SourceFoldersPanel_upTestFolder");
            _btMoveUpTest = new JButtonOperator(this, moveUp, 1);
        }
        return _btMoveUpTest;
    }
    
    
    //****************************************
    // Low-level functionality definition part
    //****************************************
    
    /** returns selected item for cboSourceLevel
     * @return String item
     */
    public String getSelectedSourceLevel() {
        return cboSourceLevel().getSelectedItem().toString();
    }
    
    /** selects item for cboSourceLevel
     * @param item String item
     */
    public void selectSourceLevel(String item) {
        cboSourceLevel().selectItem(item);
    }
    
    /** clicks on "Add Folder..." JButton
     */
    public void addFolderSrc() {
        btAddFolderSrc().push();
    }
    
    /** clicks on "Remove" JButton
     */
    public void removeSrc() {
        btRemoveSrc().push();
    }
    
    /** clicks on "Add Folder..." JButton
     */
    public void addFolderTest() {
        btAddFolderTest().push();
    }
    
    /** clicks on "Remove" JButton
     */
    public void removeTest() {
        btRemoveTest().push();
    }
    
    /** clicks on "Move Up" JButton
     */
    public void moveUpSrc() {
        btMoveUpSrc().push();
    }
    
    /** clicks on "Move Down" JButton
     */
    public void moveDownSrc() {
        btMoveDownSrc().push();
    }
    
    /** clicks on "Move Down" JButton
     */
    public void moveDownTest() {
        btMoveDownTest().push();
    }
    
    /** clicks on "Move Up" JButton
     */
    public void moveUpTest() {
        btMoveUpTest().push();
    }
    
    
    //*****************************************
    // High-level functionality definition part
    //*****************************************
    
    /** Performs verification of NewWebFreeFormSrcFoldersStepOperator by
     * accessing all its components.
     */
    public void verify() {
        lblSourcePackageFolders();
        lblSourceLevel();
        cboSourceLevel();
        btAddFolderSrc();
        btRemoveSrc();
        tabSourcePackageFolders();
        lblTestPackageFolders();
        btAddFolderTest();
        btRemoveTest();
        btMoveUpSrc();
        btMoveDownSrc();
        btMoveDownTest();
        btMoveUpTest();
    }
}
