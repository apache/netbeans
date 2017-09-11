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
package org.netbeans.test.java.editor.jelly;

import org.netbeans.jemmy.operators.*;
import org.netbeans.modules.java.editor.codegen.GetterSetterGenerator;

/** 
 * @author Jiri Prox
 * @version 1.0
 */
public class GenerateGettersAndSettersOperator extends JDialogOperator {

    /** Creates new GenerateGettersAndSetters that can handle it.
     */
    
    public static final String GETTERS_AND_SETTERS = org.openide.util.NbBundle.getMessage(GetterSetterGenerator.class, "LBL_generate_getter_and_setter"); //NOI18N
    
    public static final String GETTERS_ONLY = org.openide.util.NbBundle.getMessage(GetterSetterGenerator.class, "LBL_generate_getter"); //NOI18N
    
    public static final String SETTERS_ONLY = org.openide.util.NbBundle.getMessage(GetterSetterGenerator.class, "LBL_generate_setter"); //NOI18N
    
    public GenerateGettersAndSettersOperator(String name) {
        super(name);
    }

    private JLabelOperator _lblSelectFieldsToGenerateGettersAndSettersFor;
    private JTreeOperator _treeTreeView$ExplorerTree;
    private JButtonOperator _btGenerate;
    private JButtonOperator _btCancel;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "Select fields to generate getters and setters for:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSelectFieldsToGenerateGettersAndSettersFor() {
        if (_lblSelectFieldsToGenerateGettersAndSettersFor==null) {
            _lblSelectFieldsToGenerateGettersAndSettersFor = new JLabelOperator(this, "Select fields to generate getters and setters for:");
        }
        return _lblSelectFieldsToGenerateGettersAndSettersFor;
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

    /** Performs verification of GenerateGettersAndSetters by accessing all its components.
     */
    public void verify() {
        lblSelectFieldsToGenerateGettersAndSettersFor();
        treeTreeView$ExplorerTree();
        btGenerate();
        btCancel();
    }
   
}

