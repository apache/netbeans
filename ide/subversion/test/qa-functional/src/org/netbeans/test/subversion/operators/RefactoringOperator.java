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
