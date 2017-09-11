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

import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.*;
import org.netbeans.test.subversion.operators.actions.MergeAction;

/** Class implementing all necessary methods for handling "Merge AnagramGame to..." NbDialog.
 *
 * @author peter
 * @version 1.0
 */
public class MergeOperator extends JDialogOperator {

    /**
     * Creates new MergeOperator that can handle it.
     */
    public MergeOperator() {
        super("Merge");
    }

    /** Selects nodes and call merge action on them.
     * @param nodes an array of nodes
     * @return MergeOperator instance
     */
    public static MergeOperator invoke(Node[] nodes) {
        new MergeAction().perform(nodes);
        return new MergeOperator();
    }
    
    /** Selects node and call merge action on it.
     * @param node node to be selected
     * @return CommitOperator instance
     */
    public static MergeOperator invoke(Node node) {
        return invoke(new Node[] {node});
    }

    private JComboBoxOperator _cboMergeFrom;
    private JButtonOperator _btMerge;
    private JButtonOperator _btCancel;
    private JButtonOperator _btHelp;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboMergeFrom() {
        if (_cboMergeFrom==null) {
            _cboMergeFrom = new JComboBoxOperator(this, 1);
        }
        return _cboMergeFrom;
    }

    
    /** Tries to find "Merge" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btMerge() {
        if (_btMerge==null) {
            _btMerge = new JButtonOperator(this, "Merge");
        }
        return _btMerge;
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

    /**
     * returns selected item for cboMergeFrom
     * 
     * @return String item
     */
    public String getSelectedMergeFrom() {
        return cboMergeFrom().getSelectedItem().toString();
    }

    /**
     * selects item for cboMergeFrom
     * 
     * @param item String item
     */
    public void selectMergeFrom(String item) {
        cboMergeFrom().selectItem(item);
    }

    /**
     * types text for cboMergeFrom
     * 
     * @param text String text
     */
    public void setMergeFrom(String text) {
        cboMergeFrom().clearText();
        cboMergeFrom().typeText(text);
    }
}

