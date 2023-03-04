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
package org.netbeans.test.permanentUI;

import java.io.IOException;
import junit.framework.Test;
import org.netbeans.test.permanentUI.utils.MenuChecker;
import org.netbeans.test.permanentUI.utils.ProjectContext;

/**
 *
 * @author Lukas Hasik, Jan Peska, Marian.Mirilovic@oracle.com
 */
public class MainMenuTest extends MainMenuTestCase {

    /**
     * Need to be defined because of JUnit
     *
     * @param name
     */
    public MainMenuTest(String name) {
        super(name);
    }

    public static Test suite() {
        return MainMenuTest.emptyConfiguration().
                // here you test main-menu bar
                addTest(MainMenuTest.class, "testFileMenu").
                addTest(MainMenuTest.class, "testEditMenu").
                addTest(MainMenuTest.class, "testViewMenu").
                addTest(MainMenuTest.class, "testNavigateMenu").
                addTest(MainMenuTest.class, "testSourceMenu").
                addTest(MainMenuTest.class, "testRefactorMenu").
                addTest(MainMenuTest.class, "testDebugMenu").
                addTest(MainMenuTest.class, "testRunMenu").
                addTest(MainMenuTest.class, "testHelpMenu").
                addTest(MainMenuTest.class, "testToolsMenu").
                addTest(MainMenuTest.class, "testTeamMenu").
                addTest(MainMenuTest.class, "testWindowMenu").
                addTest(MainMenuTest.class, "testProfileMenu").
                // here you test sub-menus in each menu.
                // no more submenu since 8.0 addTest(MainMenuTest.class, "testFile_ProjectGroupSubMenu").
                addTest(MainMenuTest.class, "testFile_ImportProjectSubMenu").
                addTest(MainMenuTest.class, "testFile_ExportProjectSubMenu").
                addTest(MainMenuTest.class, "testNavigate_InspectSubMenu").
                // Submenu disabled - do nothing addTest(MainMenuTest.class, "testView_CodeFoldsSubMenu").
                addTest(MainMenuTest.class, "testView_ToolbarsSubMenu").
                addTest(MainMenuTest.class, "testProfile_AdvancedCommandsSubMenu").
                addTest(MainMenuTest.class, "testDebug_StackSubMenu").
                //addTest(MainMenuTest.class, "testSource_PreprocessorBlocksSubMenu").
                addTest(MainMenuTest.class, "testTools_InternationalizationSubMenu").
                addTest(MainMenuTest.class, "testTools_PaletteSubMenu").
                addTest(MainMenuTest.class, "testTeam_GitSubMenu").
                addTest(MainMenuTest.class, "testTeam_MercurialSubMenu").
                addTest(MainMenuTest.class, "testTeam_SubversionSubMenu").
                addTest(MainMenuTest.class, "testTeam_HistorySubMenu").
                addTest(MainMenuTest.class, "testWindow_DebuggingSubMenu").
                //addTest(MainMenuTest.class, "testWindow_NavigatingSubMenu").
                //addTest(MainMenuTest.class, "testWindow_OtherSubMenu").
                //addTest(MainMenuTest.class, "testWindow_OutputSubMenu").
                addTest(MainMenuTest.class, "testWindow_IDE_ToolsSubMenu").
                addTest(MainMenuTest.class, "testWindow_Configure_WindowSubMenu").
                addTest(MainMenuTest.class, "testWindow_ProfilingSubMenu").
                //addTest(MainMenuTest.class, "testWindow_VersioningSubMenu").
                //addTest(MainMenuTest.class, "testMnemonicsCollision").
                clusters(".*").enableModules(".*").
                suite();
    }

    @Override
    public void initialize() throws IOException {
        // do nothing
    }

    @Override
    public ProjectContext getContext() {
        return ProjectContext.NONE;
    }
    
    
    public void testFileMenu() {
        oneMenuTest("File");
    }

    public void testEditMenu() {
        oneMenuTest("Edit");
    }

    public void testViewMenu() {
        oneMenuTest("View");
    }

    public void testNavigateMenu() {
        oneMenuTest("Navigate");
    }

    public void testSourceMenu() {
        oneMenuTest("Source");
    }

    public void testRefactorMenu() {
        oneMenuTest("Refactor");
    }

    public void testRunMenu() {
        oneMenuTest("Run");
    }

    public void testDebugMenu() {
        oneMenuTest("Debug");
    }

    public void testHelpMenu() {
        oneMenuTest("Help");
    }

    public void testToolsMenu() {
        oneMenuTest("Tools");
    }

    public void testTeamMenu() {
        oneMenuTest("Team");
    }

    public void testWindowMenu() {
        oneMenuTest("Window");
    }

    public void testProfileMenu() {
        oneMenuTest("Profile");
    }

/** No more submenu since 8.
 * public void testFile_ProjectGroupSubMenu() {
        oneSubMenuTest("File|Project Group", false);
    }
    */

    public void testFile_ImportProjectSubMenu() {
        oneSubMenuTest("File|Import Project", true);
    }

    public void testFile_ExportProjectSubMenu() {
        oneSubMenuTest("File|Export Project", false);//here
    }

    public void testNavigate_InspectSubMenu() {
        oneSubMenuTest("Navigate|Inspect", false);
    }

/**    public void testView_CodeFoldsSubMenu() {
        //Submenu disabled - do nothing
    }
*/
    public void testView_ToolbarsSubMenu() {
        oneSubMenuTest("View|Toolbars", false);
    }

    public void testProfile_AdvancedCommandsSubMenu() {
        oneSubMenuTest("Profile|Advanced Commands", true);
    }

    public void testDebug_StackSubMenu() {
        oneSubMenuTest("Debug|Stack", false);
    }

/**    public void testSource_PreprocessorBlocksSubMenu() {
        //Submenu disabled - do nothing
    }
*/
    public void testTools_InternationalizationSubMenu() {
        oneSubMenuTest("Tools|Internationalization", false);//here
    }

    public void testTools_PaletteSubMenu() {
        oneSubMenuTest("Tools|Palette", false);
    }

    public void testTeam_GitSubMenu() {
        oneSubMenuTest("Team|Git", true);//here
    }

    public void testTeam_MercurialSubMenu() {
        oneSubMenuTest("Team|Mercurial", true);
    }

    public void testTeam_SubversionSubMenu() {
        oneSubMenuTest("Team|Subversion", true);
    }

    public void testTeam_HistorySubMenu() {
        oneSubMenuTest("Team|History", true);
    }

    public void testWindow_DebuggingSubMenu() {
        oneSubMenuTest("Window|Debugging", false);
    }

    public void testWindow_IDE_ToolsSubMenu() {
        oneSubMenuTest("Window|IDE Tools", false);
    }

    public void testWindow_Configure_WindowSubMenu() {
        oneSubMenuTest("Window|Configure Window", false);
    }

    public void testWindow_ProfilingSubMenu() {
        oneSubMenuTest("Window|Profiling", false);
    }

/**    public void testMnemonicsCollision() {
        String collisions = MenuChecker.checkMnemonicCollision();
        assertFalse(collisions, collisions.length() > 0);
    }
*/    
    @Override
    protected void tearDown() throws Exception {}

}
