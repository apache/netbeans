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
package org.netbeans.qa.form.jda;

import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling "Choose a Class" NbDialog.
 *
 * @author Jiri Vagner
 */
public class ChooseClassOperator extends JDialogOperator {

    /** Creates new ChooseAClass that can handle it.
     */
    public ChooseClassOperator() {
        super("Choose a Class"); // NOI18N
    }

    private JLabelOperator _lblFolderName;
    private JTextFieldOperator _txtFolderName;
    private JTreeOperator _treeTreeView;
    private JButtonOperator _btCreateNew;
    private JButtonOperator _btOK;
    private JButtonOperator _btCancel;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "Folder Name:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblFolderName() {
        if (_lblFolderName==null) {
            _lblFolderName = new JLabelOperator(this, "Folder Name:"); // NOI18N
        }
        return _lblFolderName;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtFolderName() {
        if (_txtFolderName==null) {
            _txtFolderName = new JTextFieldOperator(this);
        }
        return _txtFolderName;
    }

    /** Tries to find null TreeView$ExplorerTree in this dialog.
     * @return JTreeOperator
     */
    public JTreeOperator treeTreeView() {
        if (_treeTreeView==null) {
            _treeTreeView = new JTreeOperator(this);
        }
        return _treeTreeView;
    }

    /** Tries to find "Create New" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btCreateNew() {
        if (_btCreateNew==null) {
            _btCreateNew = new JButtonOperator(this, "Create New"); // NOI18N
        }
        return _btCreateNew;
    }

    /** Tries to find "OK" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btOK() {
        if (_btOK==null) {
            _btOK = new JButtonOperator(this, "OK"); // NOI18N
        }
        return _btOK;
    }

    /** Tries to find "Cancel" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btCancel() {
        if (_btCancel==null) {
            _btCancel = new JButtonOperator(this, "Cancel"); // NOI18N
        }
        return _btCancel;
    }


    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** gets text for txtFolderName
     * @return String text
     */
    public String getFolderName() {
        return txtFolderName().getText();
    }

    /** sets text for txtFolderName
     * @param text String text
     */
    public void setFolderName(String text) {
        txtFolderName().setText(text);
    }

    /** types text for txtFolderName
     * @param text String text
     */
    public void typeFolderName(String text) {
        txtFolderName().typeText(text);
    }

    /** clicks on "Create New" JButton
     */
    public void createNew() {
        btCreateNew().push();
    }

    /** clicks on "OK" JButton
     */
    public void ok() {
        btOK().push();
    }

    /** clicks on "Cancel" JButton
     */
    public void cancel() {
        btCancel().push();
    }
}

