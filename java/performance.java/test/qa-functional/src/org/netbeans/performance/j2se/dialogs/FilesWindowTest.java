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

import org.netbeans.jellytools.FilesTabOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.actions.FilesViewAction;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SEBaseSetup;

/**
 * Test opening Files Tab.
 * @author  mmirilovic@netbeans.org
 */
public class FilesWindowTest extends PerformanceTestCase {

    
    /** Creates a new instance of FilesWindow */
    public FilesWindowTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }

    /** Creates a new instance of FilesWindow*/
    public FilesWindowTest(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.createConfiguration(J2SEBaseSetup.class)
                .addTest(FilesWindowTest.class)
                .enableModules(".*").clusters("ide").suite());
        return suite;
    }
    
    public void prepare() {
    }

    @Override
    public void initialize() {
        JMenuBarOperator jmbo = new JMenuBarOperator(MainWindowOperator.getDefault().getJMenuBar());
        jmbo.pushMenu("Window"); //NOI18N
        jmbo.closeSubmenus();
        jmbo.pushMenuNoBlock("Window|Files"); //NOI18N
        FilesTabOperator fto = new FilesTabOperator();
        if (fto!=null) {
            fto.close();
        }
    }
    
    public ComponentOperator open() {
        // invoke Files from the main menu
        FilesViewAction fva = new FilesViewAction();
        fva.performMenu();
        return new FilesTabOperator();
    }
    
    public void testFilesWindow() {
        doMeasurement();
    }    
    
    @Override
    public void close() {
        if(testedComponentOperator!=null && testedComponentOperator.isShowing()) {
            ((FilesTabOperator)testedComponentOperator).close();
        }
    }
    
    @Override
    public void shutdown() {
        new FilesViewAction().perform();
    }
}
