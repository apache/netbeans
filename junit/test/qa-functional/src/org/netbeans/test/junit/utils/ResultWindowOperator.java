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
package org.netbeans.test.junit.utils;

import java.awt.Component;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.modules.junit.actions.ResultWindowViewAction;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.JToggleButtonOperator;

/**
 * This Operator operates JUnit Tests Results Window
 * @author Max Sauer
 */
public class ResultWindowOperator extends TopComponentOperator {
    
    private static final Action invokeAction = new ResultWindowViewAction();
    
    /**
     * Creates a new instance of ResultWindowOperator
     */
    public ResultWindowOperator() {
        /* In IDE ResultWindow top component is singleton but in sense of
         * jellytools, it is not singleton. It can be closed/hidden and
         * again opened/shown, so it make sense to wait for OutputWindow
         * top component again.
         */
        super(waitTopComponent(null, null, 0, resultsSubchooser));
    }
    
    /**
     *
     * Opens JUnit Test Results from main menu Window|JUnit Test Results and
     * returns ResultWindowOperator.
     *
     * @return instance of ResultsWindowOperatorOperator
     */
    public static ResultWindowOperator invoke() {
        new Action("Window|Output|JUnit Test Results" , null).perform();
        return new ResultWindowOperator();
    }
    
    /**
     * Returns operator of Statistics Panel
     * @return the operator of statistics panel 
     * (left of the two JUnit Test Result Window panels in a JSplitPane)
     */ 
    public StatisticsPanelOperator getLeftPanelOperator() {
        return new StatisticsPanelOperator();
    }
    
    /**
     * Test whether the Results Filter toggle button is enabled
     * @return true if the button os enabled
     */ 
    public boolean isFilterButtonEnabled() {
        return (new JToggleButtonOperator(new StatisticsPanelOperator(), 0).isEnabled());
    }
    
    /**
     * Pushes Filter Button
     */
    public void pushFilterButton() {
        new JToggleButtonOperator(new StatisticsPanelOperator(), 0).push();
    }
    
    /**
     * SubChooser to determine ResultsWindow TopComponent
     * Used in constructor.
     */
    private static final ComponentChooser resultsSubchooser = new ComponentChooser() {
        public boolean checkComponent(Component comp) {
            return comp.getClass().getName().endsWith("ResultWindow"); //NOI18N
        }
        
        public String getDescription() {
            return "component instanceof org.netbeans.modules.junit.output.ResultWindow";// NOI18N
        }
    };
}
