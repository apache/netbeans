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

package org.netbeans.test.subversion.operators;

import java.awt.Component;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.test.subversion.operators.actions.RefactoringAction;

public class RefactoringOperator extends TopComponentOperator {
    
    private static final Action invokeAction = new RefactoringAction();
    private JButtonOperator _btDoRefactoring;
    private JButtonOperator _btCancel;
    
    public JButtonOperator btDoRefactoring() {
        if (_btDoRefactoring == null) {
            _btDoRefactoring = new JButtonOperator(this, "Refactor");
        }
        return _btDoRefactoring;
    }
    
    public JButtonOperator btCancel() {
        if (_btCancel == null) {
            _btCancel = new JButtonOperator(this, "Cancel");
        }
        return _btCancel;
    }
    
    public void doRefactoring() {
        btDoRefactoring().pushNoBlock();
    }
    
    public void cancel() {
        btCancel().pushNoBlock();
    }
    
    /** Waits for refactoring window top component and creates a new operator for it. */
    public RefactoringOperator() {
        super(waitTopComponent(null, null, 0, renameChooser));
    }
    
    /**
     * Opens Output from main menu Window|Output and returns RefactoringOperator.
     * 
     * @return instance of RefactoringOperator
     */
    public static RefactoringOperator invoke() {
        invokeAction.perform();
        return new RefactoringOperator();
    }
    
    /** Returns active OutputTabOperator instance regardless it is the only one in
     * output or it is in tabbed pane.
     * @return active OutputTabOperator instance
     *
    private OutputTabOperator getActiveOutputTab() {
        OutputTabOperator outputTabOper;
        if(null != JTabbedPaneOperator.findJTabbedPane((Container)getSource(), ComponentSearcher.getTrueChooser(""))) {
            outputTabOper = new OutputTabOperator(((JComponent)new JTabbedPaneOperator(this).getSelectedComponent()));
            outputTabOper.copyEnvironment(this);
        } else {
            outputTabOper = new OutputTabOperator("");
        }
        return outputTabOper;
    }*/

    /**
     * Returns instance of OutputTabOperator of given name.
     * It is activated by default.
     * @param tabName name of tab to be selected
     * @return instance of OutputTabOperator
     *
    public OutputTabOperator getOutputTab(String tabName) {
        return new OutputTabOperator(tabName);
    }*/
    
    /**
     * Returns text from the active tab.
     * @return text from the active tab
     *
    public String getText() {
        return getActiveOutputTab().getText();
    }*/
    
    /********************************** Actions ****************************/
    
    /** Performs copy action on active tab. 
    public void copy() {
        getActiveOutputTab().copy();
    }*/
    
    /** Performs find action on active tab. 
    public void find() {
        getActiveOutputTab().find();
    }*/
    
    /** Performs find next action on active tab. 
    public void findNext() {
        getActiveOutputTab().findNext();
    }*/
    
    /** Performs select all action on active tab. 
    public void selectAll() {
        getActiveOutputTab().selectAll();
    }*/    
    
    /** Performs next error action on active tab. 
    public void nextError() {
        getActiveOutputTab().nextError();
    }*/
    
    /** Performs next error action on active tab. 
    public void previousError() {
        getActiveOutputTab().previousError();
    }*/

    /** Performs wrap text action on active tab. 
    public void wrapText() {
        getActiveOutputTab().wrapText();
    }*/ 
    
    /** Performs clear action on active tab. 
    public void clear() {
        getActiveOutputTab().clear();
    }*/

    /** Performs save as action on active tab. 
    public void saveAs() {
        getActiveOutputTab().saveAs();
    }*/
    
    /** Performs verification by accessing all sub-components. 
    public void verify() {
        // do nothing because output top component can be empty
    }*/
    
    /** SubChooser to determine OutputWindow TopComponent
     * Used in constructor.
     */
    private static final ComponentChooser renameChooser = new ComponentChooser() {
        public boolean checkComponent(Component comp) {
            return comp.getClass().getName().endsWith("RefactoringPanelContainer"); //NOI18N
        }
        
        public String getDescription() {
            return "component instanceof org.netbeans.refactoring.ui.RefactoringPanelContainer";// NOI18N
        }
    };
}
