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

package org.netbeans.performance.j2se.dialogs;

import java.awt.Component;
import javax.swing.JButton;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SESetup;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class AddProfilingPointWizardTest  extends PerformanceTestCase {

    private static final String menuPrefix = "Window|Profiling|"; //NOI18N
    private String commandName;
    private String windowName;
    private TopComponentOperator ppointsPane;
    private JButtonOperator addPointButton;
    private NbDialogOperator wizard;
    
    /**
     * @param testName 
     */
    public AddProfilingPointWizardTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }

    /**
     * @param testName 
     * @param performanceDataName
     */
    public AddProfilingPointWizardTest(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.createConfiguration(J2SESetup.class)
                .addTest(AddProfilingPointWizardTest.class)
                .enableModules(".*").clusters("ide|java|apisupport|profiler").suite());
        return suite;
    }

    public void testAddProfilingPointWizard() {
        doMeasurement();
    }
        
    @Override
    public void initialize() {
        commandName = "Profiling Points"; //NOI18N
        windowName = "Profiling Points"; ////NOI18N
        new Action(menuPrefix+commandName,null).performMenu(); // NOI18N  
        ppointsPane = new TopComponentOperator(windowName);
        addPointButton = new JButtonOperator(ppointsPane,new ComponentChooser() {

            public boolean checkComponent(Component component) {
                try{
                    if ( (((JButton)component).getToolTipText()).equals("Add Profiling Point") ) {
                        return true;
                    }
                    else {
                        return false;
                    }
                } catch (java.lang.NullPointerException npe) {}
                 return false;
            }

            public String getDescription() {
                return "Selecting button by tooltip";
            }
            });
    }

    public void prepare() {
    }

    public ComponentOperator open() {
        addPointButton.pushNoBlock();
        wizard =new NbDialogOperator("New Profiling Point"); //NOI18N
        return null;
    }

    @Override
    public void close() {
        wizard.close();
    }
    
    @Override
    public void shutdown() {
        ppointsPane.closeWindow();
    }

}
