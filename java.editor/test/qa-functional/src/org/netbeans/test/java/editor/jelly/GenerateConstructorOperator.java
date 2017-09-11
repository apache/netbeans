/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
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

