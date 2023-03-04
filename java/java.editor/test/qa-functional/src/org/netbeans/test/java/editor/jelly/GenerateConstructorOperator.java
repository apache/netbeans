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
package org.netbeans.test.java.editor.jelly;

import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.util.NameComponentChooser;

/** Class implementing all necessary methods for handling "Generate Constructor" NbDialog.
 *
 * @author Jiri Prox Jiri.Prox@Sun.COM
 * @version 1.0
 */
public class GenerateConstructorOperator extends JDialogOperator {

    /** Creates new GenerateConstructor that can handle it.
     */
    public GenerateConstructorOperator() {
        super("Generate Constructor");
    }

    private JLabelOperator _lblSelectSuperConstructor;
    private JTreeOperator _treeTreeView$ExplorerTree;
    private JLabelOperator _lblSelectFieldsToBeInitalizedByConstructor;
    private JTreeOperator _treeTreeView$ExplorerTree2;
    private JButtonOperator _btOK;
    private JButtonOperator _btCancel;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "Select super constructor:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSelectSuperConstructor() {
        if (_lblSelectSuperConstructor==null) {
            _lblSelectSuperConstructor = new JLabelOperator(this, "Select super constructor:");
        }
        return _lblSelectSuperConstructor;
    }

    /** Tries to find null TreeView$ExplorerTree in this dialog.
     * @return JTreeOperator
     */
    public JTreeOperator treeTreeView$ExplorerTree() {
        if (_treeTreeView$ExplorerTree==null) {
            _treeTreeView$ExplorerTree = new JTreeOperator(this);
        }
        return _treeTreeView$ExplorerTree;
    }    

    /** Tries to find "Select fields to be initalized by constructor:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSelectFieldsToBeInitalizedByConstructor() {
        if (_lblSelectFieldsToBeInitalizedByConstructor==null) {
            _lblSelectFieldsToBeInitalizedByConstructor = new JLabelOperator(this, "Select fields to be initalized by constructor:");
        }
        return _lblSelectFieldsToBeInitalizedByConstructor;
    }

    /** Tries to find null TreeView$ExplorerTree in this dialog.
     * @return JTreeOperator
     */
    public JTreeOperator treeTreeView$ExplorerTree2() {
        if (_treeTreeView$ExplorerTree2==null) {
            _treeTreeView$ExplorerTree2 = new JTreeOperator(this, 1);
        }
        return _treeTreeView$ExplorerTree2;
    }

    /** Tries to find "OK" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btOK() {
        if (_btOK==null) {
            _btOK = new JButtonOperator(this, "Generate");
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


    //****************************************
    // Low-level functionality definition part
    //****************************************
  
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


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of GenerateConstructor by accessing all its components.
     */
    public void verify() {
        lblSelectSuperConstructor();
        treeTreeView$ExplorerTree();
        lblSelectFieldsToBeInitalizedByConstructor();
        treeTreeView$ExplorerTree2();
        btOK();
        btCancel();
    }

    /** Performs simple test of GenerateConstructor
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        new GenerateConstructorOperator().verify();
        System.out.println("GenerateConstructor verification finished.");
    }
}

