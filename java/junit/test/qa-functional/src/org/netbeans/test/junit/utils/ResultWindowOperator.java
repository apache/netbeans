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
