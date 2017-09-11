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

/** Class implementing all necessary methods for handling "Select XSLT Script" FileSelector.
 *
 * @author ms113234
 * @version 1.0
 */
public class SelectXSLTScriptDialog extends JDialogOperator {

    /** Creates new SelectXSLTScriptDialog that can handle it.
     * @throws TimeoutExpiredException when FileSelector not found
     */
    public SelectXSLTScriptDialog() {
        super( "Select XSLT Script" );
    }

    private JTreeOperator _treeTreeView;
    private JLabelOperator _lblSelect;
    private JButtonOperator _btOK;
    private JButtonOperator _btCancel;
    private JComboBoxOperator _cboSelect;
    public static final String ITEM_HOMEMS113234NETBEANS34BETASAMPLEDIR = "/home/ms113234/.netbeans/3.4beta/sampledir";
    
    
    //******************************
    // Subcomponents definition part
    //******************************
    
    /** Tries to find null TreeView$ExplorerTree in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JTreeOperator
     */
    public JTreeOperator treeTreeView() {
        if (_treeTreeView==null) {
            _treeTreeView = new JTreeOperator(this, 0);
        }
        return _treeTreeView;
    }
    
    /** Tries to find "Select:" JLabel in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JLabelOperator
     */
    public JLabelOperator lblSelect() {
        if (_lblSelect==null) {
            _lblSelect = new JLabelOperator( this, "Select:", 0 );
        }
        return _lblSelect;
    }
    
    /** Tries to find "OK" ButtonBarButton in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JButtonOperator
     */
    public JButtonOperator btOK() {
        if (_btOK==null) {
            _btOK = new JButtonOperator( this, "OK", 0 );
        }
        return _btOK;
    }
    
    /** Tries to find "Cancel" ButtonBarButton in this dialog.
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
    public JComboBoxOperator cboSelect() {
        if (_cboSelect==null) {
            _cboSelect = new JComboBoxOperator(this, 0);
        }
        return _cboSelect;
    }
    
    
    //****************************************
    // Low-level functionality definition part
    //****************************************
    
    /** clicks on "OK" ButtonBarButton
     * @throws TimeoutExpiredException when ButtonBarButton not found
     */
    public void oK() {
        btOK().push();
    }
    
    /** clicks on "Cancel" ButtonBarButton
     * @throws TimeoutExpiredException when ButtonBarButton not found
     */
    public void cancel() {
        btCancel().push();
    }
    
    /** tries to find cboSelect and select item
     * @param item String item
     */
    public void setSelect( String item ) {
        cboSelect().selectItem(item, true, true);
    }
    
    
    //*****************************************
    // High-level functionality definition part
    //*****************************************
    
    /** Performs verification of SelectXSLTScriptDialog by accessing all its components.
     * @throws TimeoutExpiredException when any component not found
     */
    public void verify() {
        treeTreeView();
        lblSelect();
        btOK();
        btCancel();
        cboSelect();
    }
    
    /** Performs simple test of SelectXSLTScriptDialog
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        new SelectXSLTScriptDialog().verify();
        System.out.println("SelectXSLTScriptDialog verification finished.");
    }
}
