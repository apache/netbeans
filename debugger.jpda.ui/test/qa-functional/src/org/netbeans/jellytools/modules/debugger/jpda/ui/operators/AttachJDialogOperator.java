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
 * The Original Software is NetBeans.
 * The Initial Developer of the Original Software is Sun Microsystems, Inc.
 * Portions created by Sun Microsystems, Inc. are Copyright (C) 2003
 * All Rights Reserved.
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
 */

package org.netbeans.jellytools.modules.debugger.jpda.ui.operators;

import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling "Attach" NbDialog.
 *
 * @author mg105252
 * @version 1.0
 */
public class AttachJDialogOperator extends JDialogOperator {

    /** Creates new AttachJDialogOperator that can handle it.
     */
    public AttachJDialogOperator() {
        super("Attach");
    }

    private JComboBoxOperator _cboDebugger;
    private JComboBoxOperator _cboConnector;
//    public static final String ITEM_DEFAULTDEBUGGERJPDA = "Default Debugger (JPDA)"; 
    private JTextFieldOperator _txtTransport;
    private JTextFieldOperator _txtHost;
    private JTextFieldOperator _txtPort;
    private JButtonOperator _btOK;
    private JButtonOperator _btCancel;
    private JButtonOperator _btHelp;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboDebugger() {
        if (_cboDebugger==null) {
            _cboDebugger = new JComboBoxOperator(this);
        }
        return _cboDebugger;
    }

///////////////////////////////////////////////////////////////////////////////    
    public JComboBoxOperator cboConnector() {
        if (_cboConnector==null) {
            _cboConnector = new JComboBoxOperator(this,1);
        }
        return _cboConnector;
    }
///////////////////////////////////////////////////////////////////////////////
    
    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtTransport() {
        if (_txtTransport==null) {
            _txtTransport = new JTextFieldOperator(this);
        }
        return _txtTransport;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtHost() {
        if (_txtHost==null) {
            _txtHost = new JTextFieldOperator(this, 1);
        }
        return _txtHost;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtPort() {
        if (_txtPort==null) {
            _txtPort = new JTextFieldOperator(this, 2);
        }
        return _txtPort;
    }

    /** Tries to find "OK" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btOK() {
        if (_btOK==null) {
            _btOK = new JButtonOperator(this, "OK");
        }
        return _btOK;
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

    /** returns selected item for cboDebugger
     * @return String item
     */
    public String getSelectedDebugger() {
        return cboDebugger().getSelectedItem().toString();
    }

    /** selects item for cboDebugger
     * @param item String item
     */
    public void selectDebugger(String item) {
        cboDebugger().selectItem(item);
    }

///////////////////////////////////////////////////////////////////////////////
    public void selectConnector(String item) {
        cboConnector().selectItem(item);
    }

    public void selectConnector(int item) {
        cboConnector().selectItem(1);
    }
///////////////////////////////////////////////////////////////////////////////
    /** types text for cboDebugger
     * @param text String text
     */
    public void typeDebugger(String text) {
        cboDebugger().typeText(text);
    }

    /** gets text for txtTransport
     * @return String text
     */
    public String getTransport() {
        return txtTransport().getText();
    }

    /** sets text for txtTransport
     * @param text String text
     */
    public void setTransport(String text) {
        txtTransport().setText(text);
    }

    /** types text for txtTransport
     * @param text String text
     */
    public void typeTransport(String text) {
        txtTransport().typeText(text);
    }

    /** gets text for txtHost
     * @return String text
     */
    public String getHost() {
        return txtHost().getText();
    }

    /** sets text for txtHost
     * @param text String text
     */
    public void setHost(String text) {
        txtHost().setText(text);
    }

    /** types text for txtHost
     * @param text String text
     */
    public void typeHost(String text) {
        txtHost().typeText(text);
    }

    /** gets text for txtPort
     * @return String text
     */
    public String getPort() {
        return txtPort().getText();
    }

    /** sets text for txtPort
     * @param text String text
     */
    public void setPort(String text) {
        txtPort().setText(text);
    }

    /** types text for txtPort
     * @param text String text
     */
    public void typePort(String text) {
        txtPort().typeText(text);
    }

    /** clicks on "OK" JButton
     */
    public void oK() {
        btOK().push();
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

    /** Performs verification of AttachJDialogOperator by accessing all its components.
     */
    public void verify() {
        cboDebugger();
        txtTransport();
        txtHost();
        txtPort();
        btOK();
        btCancel();
        btHelp();
    }

    /** Performs simple test of AttachJDialogOperator
    * @param args the command line arguments
    */
    public static void main(String args[]) {
//        new AttachJDialogOperator().verify();
        System.out.println("AttachJDialogOperator verification finished.");
        java.util.Enumeration enumeration =  System.getProperties().keys();
        String property;
        while (enumeration.hasMoreElements()) {
            property = (String) enumeration.nextElement();
            System.out.println(property + "\t" + System.getProperty(property) );
        }
        
        
    }
}

