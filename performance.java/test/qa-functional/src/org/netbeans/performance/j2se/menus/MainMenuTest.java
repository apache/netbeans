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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
