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
    
    /********************************** Actions ****************************/
    
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
