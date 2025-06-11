/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.jellytools;

import java.awt.Component;
import java.awt.Container;
import javax.swing.JComponent;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.OutputWindowViewAction;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.ComponentSearcher;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;

/**
 * Provides access to the Output window and it's subcomponents.
 * Output window might contain one or more output tabs.
 * It is better to use {@link OutputTabOperator} if you want to work
 * with a particular output tab.
 * <p>
 * Usage:<br>
 * <pre>
 *      OutputOperator oo = new OutputOperator();
 *      System.out.println("TEXT from active output tab="+oo.getText().substring(0, 10));
 *      // get OutputTabOperator instance
 *      OutputTabOperator oto = oo.getOutputTab("myoutput");
 *      // or
 *      // OutputTabOperator oto = new OutputTabOperator("myoutput");
 *      // call an action from context menu
 *      oo.find();
 *      // close output
 *      oo.close();
 * </pre>
 *
 * @author Jiri.Skrivanek@sun.com
 * @see OutputTabOperator
 */
public class OutputOperator extends TopComponentOperator {
    
    private static final Action invokeAction = new OutputWindowViewAction();
    
    /** Waits for output window top component and creates a new operator for it. */
    public OutputOperator() {
        /* In IDE OutputWindow top component is singleton but in sense of
         jellytools, it is not singleton. It can be closed/hidden and
         again opened/shown, so it make sense to wait for OutputWindow
         top component again.
         */
        super(waitTopComponent(null, null, 0, outputSubchooser));
    }
    
    /** Opens Output from main menu Window|Output and returns OutputOperator.
     * @return instance of OutputOperator
     */
    public static OutputOperator invoke() {
        invokeAction.perform();
        return new OutputOperator();
    }
    
    /** Returns active OutputTabOperator instance regardless it is the only one in
     * output or it is in tabbed pane.
     * @return active OutputTabOperator instance
     */
    private OutputTabOperator getActiveOutputTab() {
        OutputTabOperator outputTabOper;
        if(null != JTabbedPaneOperator.findJTabbedPane((Container)getSource(), ComponentSearcher.getTrueChooser(""))) {
            outputTabOper = new OutputTabOperator(((JComponent)new JTabbedPaneOperator(this).getSelectedComponent()));
            outputTabOper.copyEnvironment(this);
        } else {
            outputTabOper = new OutputTabOperator("");
        }
        return outputTabOper;
    }

    /**
     * Returns instance of OutputTabOperator of given name.
     * It is activated by default.
     * @param tabName name of tab to be selected
     * @return instance of OutputTabOperator
     */
    public OutputTabOperator getOutputTab(String tabName) {
        return new OutputTabOperator(tabName);
    }
    
    /**
     * Returns text from the active tab.
     * @return text from the active tab
     */
    public String getText() {
        return getActiveOutputTab().getText();
    }
    
    /** Performs copy action on active tab. */
    public void copy() {
        getActiveOutputTab().copy();
    }
    
    /** Performs find action on active tab. */
    public void find() {
        getActiveOutputTab().find();
    }
    
    /** Performs find next action on active tab. */
    public void findNext() {
        getActiveOutputTab().findNext();
    }
    
    /** Performs select all action on active tab. */
    public void selectAll() {
        getActiveOutputTab().selectAll();
    }    
    
    /** Performs next error action on active tab. */
    public void nextError() {
        getActiveOutputTab().nextError();
    }
    
    /** Performs next error action on active tab. */
    public void previousError() {
        getActiveOutputTab().previousError();
    }

    /** Performs wrap text action on active tab. */
    public void wrapText() {
        getActiveOutputTab().wrapText();
    }
    
    /** Performs clear action on active tab. */
    public void clear() {
        getActiveOutputTab().clear();
    }

    /** Performs save as action on active tab. */
    public void saveAs() {
        getActiveOutputTab().saveAs();
    }
    
    /** Performs verification by accessing all sub-components. */
    public void verify() {
        // do nothing because output top component can be empty
    }
    
    /** SubChooser to determine OutputWindow TopComponent
     * Used in constructor.
     */
    private static final ComponentChooser outputSubchooser = new ComponentChooser() {
        public boolean checkComponent(Component comp) {
            return comp.getClass().getName().endsWith("IOWindowImpl"); //NOI18N
        }
        
        public String getDescription() {
            return "component instanceof org.netbeans.core.io.ui.IOWindow$IOWindowImpl";// NOI18N
        }
    };
}
