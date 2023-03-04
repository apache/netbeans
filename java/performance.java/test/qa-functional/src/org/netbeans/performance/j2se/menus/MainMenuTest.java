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
package org.netbeans.performance.j2se.menus;

import junit.framework.Test;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JMenuOperator;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SESetup;

/**
 * Performance test of application main menu.</p>
 * <p>
 * Each test method reads the label of tested menu and pushes it (using mouse).
 * The menu is then close using escape key.
 *
 * @author mmirilovic@netbeans.org
 */
public class MainMenuTest extends PerformanceTestCase {

    protected static String menuPath;
    private JMenuBarOperator menuBar;
    private JMenuOperator testedMenu;

    /**
     * Creates a new instance of MainMenu
     *
     * @param testName test name
     */
    public MainMenuTest(String testName) {
        super(testName);
        expectedTime = UI_RESPONSE;
        WAIT_AFTER_OPEN = 200;
    }

    /**
     * Creates a new instance of MainMenu
     *
     * @param testName test name
     * @param performanceDataName data name
     */
    public MainMenuTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = UI_RESPONSE;
        WAIT_AFTER_OPEN = 200;
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(J2SESetup.class, "testCloseMemoryToolbar")
                .addTest(MainMenuTest.class)
                .suite();
    }

    public void testFileMenu() {
        testMenu("org.netbeans.core.ui.resources.Bundle", "Menu/File");
    }

    public void testEditMenu() {
        testMenu("org.netbeans.core.ui.resources.Bundle", "Menu/Edit");
    }

    public void testViewMenu() {
        testMenu("org.netbeans.core.ui.resources.Bundle", "Menu/View");
    }

    public void testNavigateMenu() {
        testMenu("org.netbeans.core.ui.resources.Bundle", "Menu/GoTo");
    }

    public void testSourceMenu() {
        testMenu("org.netbeans.modules.editor.Bundle", "Menu/Source");
    }

    public void testRefactorMenu() {
        testMenu("org.netbeans.modules.refactoring.spi.impl.Bundle", "Menu/Refactoring");
    }

    public void testBuildMenu() {
        testMenu("org.netbeans.modules.project.ui.Bundle", "Menu/BuildProject");
    }

    public void testMenuRun() {
        testMenu("org.netbeans.modules.project.ui.Bundle", "Menu/RunProject");
    }

    public void testProfileMenu() {
        testMenu("org.netbeans.modules.profiler.actions.Bundle", "Menu/Profile");
    }

    public void testVersioningMenu() {
        testMenu("org.netbeans.modules.versioning.Bundle", "Menu/Versioning");
    }

    public void testToolsMenu() {
        testMenu("org.netbeans.core.ui.resources.Bundle", "Menu/Tools");
    }

    public void testWindowMenu() {
        testMenu("org.netbeans.core.windows.resources.Bundle", "Menu/Window");
    }

    public void testHelpMenu() {
        testMenu("org.netbeans.core.ui.resources.Bundle", "Menu/Help");
    }

    protected void testMenu(String bundle, String menu) {
        menuPath = org.netbeans.jellytools.Bundle.getStringTrimmed(bundle, menu);
        doMeasurement();
    }

    @Override
    public void prepare() {
    }

    @Override
    public ComponentOperator open() {
        menuBar.pushMenu(menuPath, "|");
        return testedMenu;
    }

    /**
     * Prepare method have to contain everything that needs to be done prior to
     * repeated invocation of test. Default implementation is empty.
     */
    @Override
    protected void initialize() {
        menuBar = MainWindowOperator.getDefault().menuBar();
        testedMenu = new JMenuOperator(MainWindowOperator.getDefault());
    }
}
