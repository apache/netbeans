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
package org.netbeans.jellytools.modules.xml;

import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling "XSL Transformation" NbDialog.
 *
 * @author ms113234
 * @version 1.0
 */
public class XSLTransformationDialog extends JDialogOperator {

    /** Creates new XSLTransformationDialog that can handle it.
     * @throws TimeoutExpiredException when NbDialog not found
     */
    public XSLTransformationDialog() {
        super( "XSL Transformation" );
    }

    private JCheckBoxOperator _cbOverwriteOutput;
    private JLabelOperator _lblOutput;
    private JButtonOperator _btBrowseSource;
    private JComboBoxOperator _cboXMLSource;
    private JLabelOperator _lblXSLTScript;
    private JComboBoxOperator _cboJComboBox;
    public static final String ITEM_DONOTHING = "Do Nothing";
    public static final String ITEM_APPLYDEFAULTACTION = "Apply Default Action";
    public static final String ITEM_OPENINBROWSER = "Open in Browser";
    private JButtonOperator _btBrowseScript;
    private JButtonOperator _btCancel;
    private JComboBoxOperator _cboXSLTScript;
    private JButtonOperator _btHelp;
    private JButtonOperator _btOK;
    private JComboBoxOperator _cboOutput;
    public static final String ITEM_BOOKSHTML = "Books.html";
    public static final String ITEM_PREVIEWOPENINBROWSER = "Preview (open in browser)";
    private JLabelOperator _lblProcessOutput;
    private JLabelOperator _lblXMLSource;
    
    
    //******************************
    // Subcomponents definition part
    //******************************
    
    /** Tries to find "Overwrite Output" JCheckBox in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbOverwriteOutput() {
        if (_cbOverwriteOutput==null) {
            _cbOverwriteOutput = new JCheckBoxOperator( this, "Overwrite Output", 0 );
        }
        return _cbOverwriteOutput;
    }
    
    /** Tries to find "Output:" JLabel in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JLabelOperator
     */
    public JLabelOperator lblOutput() {
        if (_lblOutput==null) {
            _lblOutput = new JLabelOperator( this, "Output:", 0 );
        }
        return _lblOutput;
    }
    
    /** Tries to find "Browse..." JButton in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JButtonOperator
     */
    public JButtonOperator btBrowseSource() {
        if (_btBrowseSource==null) {
            _btBrowseSource = new JButtonOperator( this, "Browse...", 0 );
        }
        return _btBrowseSource;
    }
    
    /** Tries to find null JComboBox in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboXMLSource() {
        if (_cboXMLSource==null) {
            _cboXMLSource = new JComboBoxOperator(this, 0);
        }
        return _cboXMLSource;
    }
    
    /** Tries to find "XSLT Script:" JLabel in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JLabelOperator
     */
    public JLabelOperator lblXSLTScript() {
        if (_lblXSLTScript==null) {
            _lblXSLTScript = new JLabelOperator( this, "XSLT Script:", 0 );
        }
        return _lblXSLTScript;
    }
    
    /** Tries to find null JComboBox in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboJComboBox() {
        if (_cboJComboBox==null) {
            _cboJComboBox = new JComboBoxOperator(this, 3);
        }
        return _cboJComboBox;
    }
    
    /** Tries to find "Browse..." JButton in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JButtonOperator
     */
    public JButtonOperator btBrowseScript() {
        if (_btBrowseScript==null) {
            _btBrowseScript = new JButtonOperator( this, "Browse...", 1 );
        }
        return _btBrowseScript;
    }
    
    /** Tries to find "Cancel" JButton in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JButtonOperator
     */
    public JButtonOperator btCancel() {
        if (_btCancel==null) {
            _btCancel = new JButtonOperator( this, "Cancel", 0 );
        }
        return _btCancel;
    }
    
    /** Tries to find null JComboBox in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboXSLTScript() {
        if (_cboXSLTScript==null) {
            _cboXSLTScript = new JComboBoxOperator(this, 1);
        }
        return _cboXSLTScript;
    }
    
    /** Tries to find "Help" JButton in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JButtonOperator
     */
    public JButtonOperator btHelp() {
        if (_btHelp==null) {
            _btHelp = new JButtonOperator( this, "Help", 0 );
        }
        return _btHelp;
    }
    
    /** Tries to find "OK" JButton in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JButtonOperator
     */
    public JButtonOperator btOK() {
        if (_btOK==null) {
            _btOK = new JButtonOperator( this, "OK", 0 );
        }
        return _btOK;
    }
    
    /** Tries to find null JComboBox in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboOutput() {
        if (_cboOutput==null) {
            _cboOutput = new JComboBoxOperator(this, 2);
        }
        return _cboOutput;
    }
    
    /** Tries to find "Process Output:" JLabel in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JLabelOperator
     */
    public JLabelOperator lblProcessOutput() {
        if (_lblProcessOutput==null) {
            _lblProcessOutput = new JLabelOperator( this, "Process Output:", 0 );
        }
        return _lblProcessOutput;
    }
    
    /** Tries to find "XML Source:" JLabel in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JLabelOperator
     */
    public JLabelOperator lblXMLSource() {
        if (_lblXMLSource==null) {
            _lblXMLSource = new JLabelOperator( this, "XML Source:", 0 );
        }
        return _lblXMLSource;
    }
    
    
    //****************************************
    // Low-level functionality definition part
    //****************************************
    
    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkOverwriteOutput( boolean state ) {
        if (cbOverwriteOutput().isSelected()!=state) {
            cbOverwriteOutput().push();
        }
    }
    
    /** clicks on "Browse..." JButton
     * @throws TimeoutExpiredException when JButton not found
     */
    public void browseSource() {
        btBrowseSource().push();
    }
    
    /** tries to find cboXMLSource and select item
     * @param item String item
     */
    public void setXMLSource( String item ) {
        cboXMLSource().selectItem(item, true, true);
    }
    
    /** tries to find cboJComboBox and select item
     * @param item String item
     */
    public void setJComboBox( String item ) {
        cboJComboBox().selectItem(item, true, true);
    }
    
    /** clicks on "Browse..." JButton
     * @throws TimeoutExpiredException when JButton not found
     */
    public void browseScript() {
        btBrowseScript().push();
    }
    
    /** clicks on "Cancel" JButton
     * @throws TimeoutExpiredException when JButton not found
     */
    public void cancel() {
        btCancel().push();
    }
    
    /** tries to find cboXSLTScript and select item
     * @param item String item
     */
    public void setXSLTScript( String item ) {
        cboXSLTScript().selectItem(item, true, true);
    }
    
    /** clicks on "Help" JButton
     * @throws TimeoutExpiredException when JButton not found
     */
    public void help() {
        btHelp().push();
    }
    
    /** clicks on "OK" JButton
     * @throws TimeoutExpiredException when JButton not found
     */
    public void oK() {
        btOK().push();
    }
    
    /** tries to find cboOutput and select item
     * @param item String item
     */
    public void setOutput( String item ) {
        cboOutput().selectItem(item, true, true);
    }
    
    
    //*****************************************
    // High-level functionality definition part
    //*****************************************
    
    /** Performs verification of XSLTransformationDialog by accessing all its components.
     * @throws TimeoutExpiredException when any component not found
     */
    public void verify() {
        cbOverwriteOutput();
        lblOutput();
        btBrowseSource();
        cboXMLSource();
        lblXSLTScript();
        cboJComboBox();
        btBrowseScript();
        btCancel();
        cboXSLTScript();
        btHelp();
        btOK();
        cboOutput();
        lblProcessOutput();
        lblXMLSource();
    }
    
    /** Performs simple test of XSLTransformationDialog
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        new XSLTransformationDialog().verify();
        System.out.println("XSLTransformationDialog verification finished.");
    }
}
