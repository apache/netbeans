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

package org.netbeans.jellytools.modules.debugger;

import java.awt.Component;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.modules.debugger.actions.BreakpointsWindowAction;
import org.netbeans.jellytools.modules.debugger.actions.DeleteAllBreakpointsAction;
import org.netbeans.jemmy.ComponentChooser;

/**
 * Provides access to the Breakpoints window.
 * <p>
 * Usage:<br>
 * <pre>
 *      BreakpointsWindowOperator bwo = new BreakpointsWindowOperator().invoke();
 *      bwo.deleteAll();
 *      bwo.close();
 * </pre>
 *
 * @author Martin.Schovanek@sun.com
 * @see org.netbeans.jellytools.OutputTabOperator
 */
public class BreakpointsWindowOperator extends TopComponentOperator {
    
    private static final Action invokeAction = new BreakpointsWindowAction();
    
    /** Waits for Breakpoints window top component and creates a new operator
     * for it. */
    public BreakpointsWindowOperator() {
        super(waitTopComponent(null, 
                Bundle.getStringTrimmed ("org.netbeans.modules.debugger.ui.views.Bundle",
                                         "CTL_Breakpoints_view"),
                0, viewSubchooser));
    }
    
    /**
     * Opens Breakpoints window from main menu Window|Debugging|Breakpoints and
     * returns BreakpointsWindowOperator.
     * @return instance of BreakpointsWindowOperator
     */
    public static BreakpointsWindowOperator invoke() {
        invokeAction.perform();
        return new BreakpointsWindowOperator();
    }
    
    /********************************** Actions ****************************/
    
    /** Performs Delete All action on active tab. */
    public void deleteAll() {
        new DeleteAllBreakpointsAction().perform(this);
    }
    
    /**
     * Performs verification of BreakpointsWindowOperator by accessing its
     * components.
     */
    public void verify() {    
        // TBD
    }
    
    /** SubChooser to determine OutputWindow TopComponent
     * Used in constructor.
     */
    private static final ComponentChooser viewSubchooser = new ComponentChooser() {
        private static final String CLASS_NAME="org.netbeans.modules.debugger.ui.views.View";
        
        public boolean checkComponent(Component comp) {
            return comp.getClass().getName().endsWith(CLASS_NAME);
        }
        
        public String getDescription() {
            return "component instanceof "+CLASS_NAME;// NOI18N
        }
    };
}
