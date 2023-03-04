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
package org.netbeans.jellytools.modules.xml;

import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling "Select XML Document" FileSelector.
 *
 * @author ms113234
 * @version 1.0
 */
public class SelectXSLTSourceDialog extends JDialogOperator {

    /** Creates new SelectXSLTSourceDialog that can handle it.
     * @throws TimeoutExpiredException when FileSelector not found
     */
    public SelectXSLTSourceDialog() {
        super( "Select XML Document" );
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

    /** Performs verification of SelectXSLTSourceDialog by accessing all its components.
     * @throws TimeoutExpiredException when any component not found
     */
    public void verify() {
        treeTreeView();
        lblSelect();
        btOK();
        btCancel();
        cboSelect();
    }

    /** Performs simple test of SelectXSLTSourceDialog
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        new SelectXSLTSourceDialog().verify();
        System.out.println("SelectXSLTSourceDialog verification finished.");
    }
}
