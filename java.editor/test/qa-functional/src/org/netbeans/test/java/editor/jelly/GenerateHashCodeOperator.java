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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.test.java.editor.jelly;

import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling "Generate equals() and hashCode()" NbDialog.
 *
 * @author Jiri Prox
 * @version 1.0
 */
public class GenerateHashCodeOperator extends JDialogOperator {

    /** Creates new GenerateEqualsAndHashCode that can handle it.
     */
    public GenerateHashCodeOperator() {
        super("Generate hashCode()");
    }

    private JLabelOperator _lblSelectFieldsToBeIncludedInHashCode;
    private JTreeOperator _treeTreeView$ExplorerTree2;
    private JButtonOperator _btGenerate;
    private JButtonOperator _btCancel;


    //******************************
    // Subcomponents definition part
    //******************************
   
    /** Tries to find "Select fields to be included in hashCode():" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSelectFieldsToBeIncludedInHashCode() {
        if (_lblSelectFieldsToBeIncludedInHashCode==null) {
            _lblSelectFieldsToBeIncludedInHashCode = new JLabelOperator(this, "Select fields to be included in hashCode():");
        }
        return _lblSelectFieldsToBeIncludedInHashCode;
    }

    /** Tries to find null TreeView$ExplorerTree in this dialog.
     * @return JTreeOperator
     */
    public JTreeOperator hashCodeTreeOperator() {
        if (_treeTreeView$ExplorerTree2==null) {
            _treeTreeView$ExplorerTree2 = new JTreeOperator(this);
        }
        return _treeTreeView$ExplorerTree2;
    }

    /** Tries to find "Generate" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btGenerate() {
        if (_btGenerate==null) {
            _btGenerate = new JButtonOperator(this, "Generate");
        }
        return _btGenerate;
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

    /** clicks on "Generate" JButton
     */
    public void generate() {
        btGenerate().push();
    }

    /** clicks on "Cancel" JButton
     */
    public void cancel() {
        btCancel().push();
    }


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of GenerateEqualsAndHashCode by accessing all its components.
     */
    public void verify() {
        lblSelectFieldsToBeIncludedInHashCode();
        hashCodeTreeOperator();
        btGenerate();
        btCancel();
    }

    /** Performs simple test of GenerateEqualsAndHashCode
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        new GenerateHashCodeOperator().verify();
        System.out.println("GenerateEqualsAndHashCode verification finished.");
    }
}

