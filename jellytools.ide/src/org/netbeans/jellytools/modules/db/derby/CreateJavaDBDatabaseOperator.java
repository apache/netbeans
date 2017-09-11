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

package org.netbeans.jellytools.modules.db.derby;

import javax.swing.JTextField;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/** Class implementing all necessary methods for handling "Create Java DB Database" NbDialog.
 *
 * @author Martin.Schovanek@sun.com
 * @version 1.0
 */
public class CreateJavaDBDatabaseOperator extends JDialogOperator {

    /** Creates new CreateJavaDBDatabaseOperator that can handle it.
     */
    public CreateJavaDBDatabaseOperator() {
        super(Bundle.getStringTrimmed("org.netbeans.modules.derby.Bundle", "LBL_CreateDatabaseTitle"));
    }

    private JLabelOperator _lblTheDatabaseNameIsEmpty;
    private JLabelOperator _lblDatabaseName;
    private JLabelOperator _lblUserName;
    private JLabelOperator _lblPassword;
    private JLabelOperator _lblDatabaseLocation;
    private JTextFieldOperator _txtPassword;
    private JTextFieldOperator _txtUserName;
    private JTextFieldOperator _txtDatabaseLocation;
    private JButtonOperator _btSettings;
    private JTextFieldOperator _txtDatabaseName;
    private JButtonOperator _btCancel;
    private JButtonOperator _btOK;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "The database name is empty." JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblTheDatabaseNameIsEmpty() {
        if (_lblTheDatabaseNameIsEmpty==null) {
            _lblTheDatabaseNameIsEmpty = new JLabelOperator(this,
                Bundle.getStringTrimmed("org.netbeans.modules.derby.ui.Bundle", "ERR_DatabaseNameEmpty"));
        }
        return _lblTheDatabaseNameIsEmpty;
    }

    /** Tries to find "Database Name:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblDatabaseName() {
        if (_lblDatabaseName==null) {
            _lblDatabaseName = new JLabelOperator(this,
                Bundle.getStringTrimmed("org.netbeans.modules.derby.ui.Bundle", "LBL_DatabaseName"));
        }
        return _lblDatabaseName;
    }

    /** Tries to find "User Name:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblUserName() {
        if (_lblUserName==null) {
            _lblUserName = new JLabelOperator(this,
                Bundle.getStringTrimmed("org.netbeans.modules.derby.ui.Bundle", "LBL_UserName"));
        }
        return _lblUserName;
    }

    /** Tries to find "Password:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblPassword() {
        if (_lblPassword==null) {
            _lblPassword = new JLabelOperator(this,
                Bundle.getStringTrimmed("org.netbeans.modules.derby.ui.Bundle", "LBL_Password"));
        }
        return _lblPassword;
    }

    /** Tries to find "Database Location:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblDatabaseLocation() {
        if (_lblDatabaseLocation==null) {
            _lblDatabaseLocation = new JLabelOperator(this,
                Bundle.getStringTrimmed("org.netbeans.modules.derby.ui.Bundle", "LBL_DatabaseLocation"));
        }
        return _lblDatabaseLocation;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtPassword() {
        if (_txtPassword==null) {
            _txtPassword = new JTextFieldOperator((JTextField) lblPassword().getLabelFor());
        }
        return _txtPassword;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtUserName() {
        if (_txtUserName==null) {
            _txtUserName = new JTextFieldOperator((JTextField) lblUserName().getLabelFor());
        }
        return _txtUserName;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtDatabaseLocation() {
        if (_txtDatabaseLocation==null) {
            _txtDatabaseLocation = new JTextFieldOperator((JTextField) lblDatabaseLocation().getLabelFor());
        }
        return _txtDatabaseLocation;
    }

    /** Tries to find "Properties..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btSettings() {
        if (_btSettings==null) {
            _btSettings = new JButtonOperator(this,
                Bundle.getStringTrimmed("org.netbeans.modules.derby.ui.Bundle", "LBL_Properties"));
        }
        return _btSettings;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtDatabaseName() {
        if (_txtDatabaseName==null) {
            _txtDatabaseName = new JTextFieldOperator((JTextField) lblDatabaseName().getLabelFor());
        }
        return _txtDatabaseName;
    }

    /** Tries to find "Cancel" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btCancel() {
        if (_btCancel==null) {
            _btCancel = new JButtonOperator(this, Bundle.getStringTrimmed("org.openide.Bundle", "CTL_CANCEL"));
        }
        return _btCancel;
    }

    /** Tries to find "OK" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btOK() {
        if (_btOK==null) {
            _btOK = new JButtonOperator(this, Bundle.getStringTrimmed("org.openide.Bundle", "CTL_OK"));
        }
        return _btOK;
    }


    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** gets text for txtPassword
     * @return String text
     */
    public String getPassword() {
        return txtPassword().getText();
    }

    /** sets text for txtPassword
     * @param text String text
     */
    public void setPassword(String text) {
        txtPassword().setText(text);
    }

    /** types text for txtPassword
     * @param text String text
     */
    public void typePassword(String text) {
        txtPassword().typeText(text);
    }

    /** gets text for txtUserName
     * @return String text
     */
    public String getUserName() {
        return txtUserName().getText();
    }

    /** sets text for txtUserName
     * @param text String text
     */
    public void setUserName(String text) {
        txtUserName().setText(text);
    }

    /** types text for txtUserName
     * @param text String text
     */
    public void typeUserName(String text) {
        txtUserName().typeText(text);
    }

    /** gets text for txtDatabaseLocation
     * @return String text
     */
    public String getDatabaseLocation() {
        return txtDatabaseLocation().getText();
    }

    /** sets text for txtDatabaseLocation
     * @param text String text
     */
    public void setDatabaseLocation(String text) {
        txtDatabaseLocation().setText(text);
    }

    /** types text for txtDatabaseLocation
     * @param text String text
     */
    public void typeDatabaseLocation(String text) {
        txtDatabaseLocation().typeText(text);
    }

    /** clicks on "Settings..." JButton
     */
    public void settings() {
        btSettings().push();
    }

    /** gets text for txtDatabaseName
     * @return String text
     */
    public String getDatabaseName() {
        return txtDatabaseName().getText();
    }

    /** sets text for txtDatabaseName
     * @param text String text
     */
    public void setDatabaseName(String text) {
        txtDatabaseName().setText(text);
    }

    /** types text for txtDatabaseName
     * @param text String text
     */
    public void typeDatabaseName(String text) {
        txtDatabaseName().typeText(text);
    }

    /** clicks on "Cancel" JButton
     */
    public void cancel() {
        btCancel().push();
    }

    /** clicks on "OK" JButton
     */
    public void ok() {
        btOK().push();
    }


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of CreateJavaDBDatabaseOperator by accessing all its components.
     */
    public void verify() {
        lblTheDatabaseNameIsEmpty();
        lblDatabaseName();
        lblUserName();
        lblPassword();
        lblDatabaseLocation();
        txtPassword();
        txtUserName();
        txtDatabaseLocation();
        btSettings();
        txtDatabaseName();
        btCancel();
        btOK();
    }

    /** Performs simple test of CreateJavaDBDatabaseOperator
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        new CreateJavaDBDatabaseOperator().verify();
        System.out.println("CreateJavaDBDatabaseOperator verification finished.");
    }
}

