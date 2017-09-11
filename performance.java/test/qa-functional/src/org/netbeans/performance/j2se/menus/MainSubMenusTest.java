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
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SESetup;

/**
 * Performance test of application main menu.</p>
 * <p>
 * Each test method reads the label of tested menu and pushes it (using mouse).
 * The menu is then close using escape key.
 *
 * @author Radim Kubacki, mmirilovic@netbeans.org
 */
public class MainSubMenusTest extends PerformanceTestCase {

    protected static String mainMenuPath;
    protected static String subMenuPath;

    private JMenuBarOperator menuBar;
    private TopComponentOperator editor;

    /**
     * Creates a new instance of MainSubMenus
     *
     * @param testName test name
     */
    public MainSubMenusTest(String testName) {
        super(testName);
        expectedTime = UI_RESPONSE;
        WAIT_AFTER_OPEN = 200;
    }

    /**
     * Creates a new instance of MainSubMenus
     *
     * @param testName test name
     * @param performanceDataName data name
     */
    public MainSubMenusTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = UI_RESPONSE;
        WAIT_AFTER_OPEN = 200;
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(J2SESetup.class, "testCloseMemoryToolbar", "testOpenDataProject")
                .addTest(MainSubMenusTest.class)
                .suite();
    }

    public void testFileOpenRecentFileMenu() {
        editor = CommonUtilities.openFile("PerformanceTestData", "org.netbeans.test.performance", "Main20kB.java", true);

        if (editor != null) {
            editor.close();
            editor = null;
        }
        testSubMenu("org.netbeans.core.ui.resources.Bundle", "Menu/File", "org.netbeans.modules.openfile.Bundle", "LBL_RecentFileAction_Name");
        editor = CommonUtilities.openFile("PerformanceTestData", "org.netbeans.test.performance", "Main20kB.java", true);

    }

    public void testViewCodeFoldsMenu() {
        editor = CommonUtilities.openFile("PerformanceTestData", "org.netbeans.test.performance", "Main20kB.java", true);
        testSubMenu("View", "Code Folds");
        if (editor != null) {
            editor.close();
            editor = null;
        }
    }

    public void testViewToolbarsMenu() {
        testSubMenu("org.netbeans.core.ui.resources.Bundle", "Menu/View", "org.netbeans.core.windows.actions.Bundle", "CTL_ToolbarsListAction");
    }

    public void testVersioningMercurialMenu() {
        testSubMenu("org.netbeans.modules.versioning.Bundle", "Menu/Versioning", "org.netbeans.modules.mercurial.Bundle", "CTL_Mercurial_DisplayName");
    }

    public void testToolsI18nMenu() {
        testSubMenu("org.netbeans.core.ui.resources.Bundle", "Menu/Tools", "org.netbeans.modules.i18n.Bundle", "LBL_I18nGroupActionName");
    }

    public void testToolsPaletteMenu() {
        expectedTime = 400;
        testSubMenu("org.netbeans.core.ui.resources.Bundle", "Menu/Tools", "org.netbeans.modules.palette.resources.Bundle", "Menu/Tools/PaletteManager");
    }

    public void testWinDebuggingMenu() {
        testSubMenu("org.netbeans.core.windows.resources.Bundle", "Menu/Window", "org.netbeans.modules.debugger.resources.Bundle", "Menu/Window/Debug");
    }

    public void testWinProfilingMenu() {
        testSubMenu("org.netbeans.core.windows.resources.Bundle", "Menu/Window", "org.netbeans.modules.profiler.actions.Bundle", "Menu/Window/Profile");
    }

    public void testWinWebMenu() {
        testSubMenu("Window", "Web");
    }

    public void testWinIDEToolsMenu() {
        testSubMenu("Window", "IDE Tools");
    }

    public void testHelpJavadoc() {
        testSubMenu("org.netbeans.core.ui.resources.Bundle", "Menu/Help", "org.netbeans.modules.javadoc.search.Bundle", "CTL_INDICES_MenuItem");
    }

    private void testSubMenu(String mainMenu, String subMenu) {
        mainMenuPath = mainMenu;
        subMenuPath = subMenu;
        doMeasurement();
    }

    private void testSubMenu(String bundle, String mainMenu, String bundle_2, String subMenu) {
        testSubMenu(getFromBundle(bundle, mainMenu), getFromBundle(bundle_2, subMenu));
    }

    private String getFromBundle(String bundle, String key) {
        return org.netbeans.jellytools.Bundle.getStringTrimmed(bundle, key);
    }

    @Override
    public void prepare() {
    }

    @Override
    public ComponentOperator open() {
        menuBar.pushMenu(mainMenuPath + "|" + subMenuPath);
        return null;
    }

    @Override
    public void close() {
        menuBar.closeSubmenus();
    }

    @Override
    public void shutdown() {
        if (editor != null) {
            editor.close();
            editor = null;
        }
        MainWindowOperator.getDefault().pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
    }

    @Override
    protected void initialize() {
        menuBar = MainWindowOperator.getDefault().menuBar();
    }
}
