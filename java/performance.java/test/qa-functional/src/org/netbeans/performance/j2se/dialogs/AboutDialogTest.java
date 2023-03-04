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

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SEBaseSetup;

/**
 * Test of About dialog.
 *
 * @author  mmirilovic@netbeans.org
 */
public class AboutDialogTest extends PerformanceTestCase {

    protected String MENU, ABOUT, DETAIL;

    /** Creates a new instance of About */
    public AboutDialogTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of About */
    public AboutDialogTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.createConfiguration(J2SEBaseSetup.class)
                .addTest(AboutDialogTest.class)
                .enableModules(".*").clusters("ide").suite());
        return suite;
    }

    @Override
    public void initialize() {
        MENU = Bundle.getStringTrimmed("org.netbeans.core.ui.resources.Bundle","Menu/Help") + "|" + Bundle.getStringTrimmed("org.netbeans.core.actions.Bundle" , "About");
        ABOUT = Bundle.getStringTrimmed("org.netbeans.core.startup.Bundle", "CTL_About_Title");
        DETAIL = Bundle.getStringTrimmed("org.netbeans.core.startup.Bundle", "CTL_About_Detail");
    }
    
    public void prepare(){
    }
    
    public void testAbout() {
        doMeasurement();
    }
    
    public ComponentOperator open(){
        JMenuBarOperator jmbo = new JMenuBarOperator(MainWindowOperator.getDefault().getJMenuBar());
        jmbo.pushMenu("help"); //NOI18N
        jmbo.closeSubmenus();
        jmbo.pushMenuNoBlock(MENU);
        return new org.netbeans.jellytools.NbDialogOperator(ABOUT);
    }
    
}
