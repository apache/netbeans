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
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SESetup;

/**
 * Test of Internationalization Window
 *
 * @author  anebuzelsky@netbeans.org, mmirilovic@netbeans.org
 */
public class InternationalizeDialogTest extends PerformanceTestCase {

    private static EditorOperator editor;
    private String BUNDLE, MENU, TITLE;
    
    /** Creates a new instance of InternationalizeDialog */
    public InternationalizeDialogTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN = 2000;
    }
    
    /** Creates a new instance of InternationalizeDialog */
    public InternationalizeDialogTest(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN = 2000;
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.createConfiguration(J2SESetup.class)
                .addTest(InternationalizeDialogTest.class)
                .enableModules(".*").clusters(".*").suite());
        return suite;
    }
    
    public void testInternationalizeDialog() {
        doMeasurement();
    }
    
    @Override
    public void initialize() {
        BUNDLE = "org.netbeans.modules.i18n.Bundle";
        MENU = Bundle.getStringTrimmed("org.netbeans.core.ui.resources.Bundle","Menu/Tools") + "|" + Bundle.getStringTrimmed(BUNDLE,"LBL_I18nGroupActionName") + "|" + Bundle.getStringTrimmed(BUNDLE,"CTL_I18nAction");
        TITLE = Bundle.getStringTrimmed(BUNDLE,"CTL_I18nDialogTitle");
        editor = CommonUtilities.openFile("PerformanceTestData","org.netbeans.test.performance","Main.java", true);
    }
    
    public void prepare() {
   }
    
    public ComponentOperator open() {
        JMenuBarOperator jmbo = new JMenuBarOperator(MainWindowOperator.getDefault().getJMenuBar());
        jmbo.pushMenu("Tools"); //NOI18N
        jmbo.closeSubmenus();
        jmbo.pushMenuNoBlock(MENU);        
        return new NbDialogOperator(TITLE);
    }
 
    @Override
    public void shutdown(){
        if(editor!=null && editor.isShowing()) {
            editor.closeDiscard();
        }
    }
    
}
