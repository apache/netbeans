/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.test.subversion.operators;

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling "Create a new folder" NbPresenter.
 *
 * @author peter
 * @version 1.0
 */
public class CreateNewFolderOperator extends NbDialogOperator {

    /**
     * Creates new CreateNewFolderOperator that can handle it.
     */
    public CreateNewFolderOperator() {
        super("Specify a new folder");
    }

    private JLabelOperator _lblFolderName;
    private JTextFieldOperator _txtFolderName;
    private JButtonOperator _btOK;
    private JButtonOperator _btCancel;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "Folder name:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblFolderName() {
        if (_lblFolderName==null) {
            _lblFolderName = new JLabelOperator(this, "Folder name:");
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

    /** Tries to find "OK" JButton in this dialog.
     * @return JButtonOperator
     */
    @Override
    public JButtonOperator btOK() {
        if (_btOK==null) {
            _btOK = new JButtonOperator(this, "OK");
        }
        return _btOK;
    }

    /** Tries to find "Cancel" JButton in this dialog.
     * @return JButtonOperator
     */
    @Override
    public JButtonOperator btCancel() {
        if (_btCancel==null) {
            _btCancel = new JButtonOperator(this, "Cancel");
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

    /** clicks on "OK" JButton
     */
    @Override
    public void ok() {
        btOK().push();
    }

    /** clicks on "Cancel" JButton
     */
    @Override
    public void cancel() {
        btCancel().push();
    }


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /**
     * Performs verification of CreateNewFolderOperator by accessing all its components.
     */
    public void verify() {
        lblFolderName();
        txtFolderName();
        btOK();
        btCancel();
    }
}

