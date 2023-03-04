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

import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SESetup;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

/**
 * Test of NetBeans Platform Manager invoked from main menu.
 *
 * @author  mmirilovic@netbeans.org
 */
public class NetBeansPlatformManagerTest extends PerformanceTestCase {

   
    /** Creates a new instance of NetBeansPlatformManager */
    public NetBeansPlatformManagerTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of NetBeansPlatformManager */
    public NetBeansPlatformManagerTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(J2SESetup.class)
             .addTest(NetBeansPlatformManagerTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }
    
    public void testNetBeansPlatformManager() {
        doMeasurement();
    }
        
    public void prepare(){
    }
    
    public ComponentOperator open(){
        String menu = Bundle.getStringTrimmed("org.netbeans.core.ui.resources.Bundle","Menu/Tools") +
        "|" +
        Bundle.getStringTrimmed("org.netbeans.modules.apisupport.project.ui.platform.Bundle","CTL_NbPlatformManager_Menu");

        MainWindowOperator.getDefault().menuBar().pushMenu(menu);
        
        return new NbDialogOperator(Bundle.getStringTrimmed("org.netbeans.modules.apisupport.project.ui.platform.Bundle","CTL_NbPlatformManager_Title"));
    }

}
