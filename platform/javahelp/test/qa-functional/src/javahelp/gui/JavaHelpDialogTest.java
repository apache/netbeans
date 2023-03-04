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
package javahelp.gui;

import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.HelpOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jellytools.actions.HelpAction;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.NbModuleSuite;

//import org.netbeans.jellytools.Bundle;
//import org.netbeans.jellytools.JellyTestCase;
//import org.netbeans.jellytools.HelpOperator;
//import org.netbeans.jellytools.MainWindowOperator;
//import org.netbeans.jellytools.NbDialogOperator;
//import org.netbeans.jellytools.OptionsOperator;
//import org.netbeans.jellytools.actions.HelpAction;
//
//import org.netbeans.jemmy.operators.JButtonOperator;
//import org.netbeans.jemmy.operators.JMenuBarOperator;
//import org.netbeans.jemmy.operators.JTreeOperator;
//import org.netbeans.junit.NbModuleSuite;
//import junit.framework.Test;

/**
 * JellyTestCase test case with implemented Java Help Test support stuff
 *
 * @author  mmirilovic@netbeans.org
 */
public class JavaHelpDialogTest extends JellyTestCase {

    private HelpOperator helpWindow;

    /** Creates a new instance of JavaHelpDialogTest */
    public JavaHelpDialogTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(JavaHelpDialogTest.class)
                .addTest("testHelpF1")
                .addTest("testHelpFromMenu")
                .addTest("testHelpByButtonNonModal")
                .addTest("testHelpByButtonModal")
                .addTest("testContextualSearch")
                .addTest("testHelpByButtonNestedModal")
                .gui(true)
                .enableModules(".*")
                .clusters(".*") );
    }

    @Override
    public void setUp() {
    }

    @Override
    public void tearDown() {
        closeAllModal();

        if (helpWindow != null && helpWindow.isVisible()) {
            helpWindow.close();
        }

        helpWindow = null;
    }

    public void testHelpF1() {
        MainWindowOperator.getDefault().pressKey(java.awt.event.KeyEvent.VK_F1);
        new org.netbeans.jemmy.EventTool().waitNoEvent(7000);
        helpWindow = new HelpOperator();
    }

    public void testHelpFromMenu() {
        new HelpAction().performMenu();
        helpWindow = new HelpOperator();
    }

    public void testHelpCoreFromMenu() {
        String helpMenu = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Help"); // Help
        String helpSetsMenu = Bundle.getStringTrimmed("org.netbeans.modules.javahelp.resources.Bundle", "Menu/Help/HelpShortcuts");  // Help Sets
        String coreIDEHelpMenu = Bundle.getString("org.netbeans.modules.usersguide.Bundle", "Actions/Help/org-netbeans-modules-usersguide-mainpage.xml"); // Core IDE Help

        MainWindowOperator.getDefault().menuBar().pushMenu(helpMenu + "|" + helpSetsMenu + "|" + coreIDEHelpMenu, "|");
        helpWindow = new HelpOperator();
    }

    public void testHelpByButtonNonModal() {
        OptionsOperator.invoke();
        OptionsOperator options = new OptionsOperator();
        options.help();
        helpWindow = new HelpOperator();
        options.close();
    }

    public void testHelpByButtonModal() {
        String toolsMenu = Bundle.getStringTrimmed("org.netbeans.core.ui.resources.Bundle", "Menu/Tools"); // Tools
        
        String javaPlatformMenu = Bundle.getStringTrimmed("org.netbeans.modules.java.platform.ui.Bundle", "CTL_PlatformManager"); // Java Platforms

        new JMenuBarOperator(MainWindowOperator.getDefault().getJMenuBar()).pushMenuNoBlock(toolsMenu + "|" + javaPlatformMenu, "|");
        new NbDialogOperator(Bundle.getStringTrimmed("org.netbeans.api.java.platform.Bundle", "TXT_PlatformsManager")).help();    // Java Platform Manager
        helpWindow = new HelpOperator();
    }

    public void testHelpByButtonNestedModal() {
        String toolsMenu = Bundle.getStringTrimmed("org.netbeans.core.ui.resources.Bundle", "Menu/Tools"); // Tools
        String javaPlatformMenu = Bundle.getStringTrimmed("org.netbeans.modules.java.platform.ui.Bundle", "CTL_PlatformManager"); // Java Platforms

        new JMenuBarOperator(MainWindowOperator.getDefault().getJMenuBar()).pushMenuNoBlock(toolsMenu + "|" + javaPlatformMenu, "|");
        //new NbDialogOperator(Bundle.getStringTrimmed("org.netbeans.core.ui.Bundle", "CTL_SetupWizardTitle")).help();    // Setup Wizard
        NbDialogOperator javaPlatformManager = new NbDialogOperator(Bundle.getStringTrimmed("org.netbeans.api.java.platform.Bundle", "TXT_PlatformsManager"));// Java Platform Manager

        new JButtonOperator(javaPlatformManager, Bundle.getStringTrimmed("org.netbeans.modules.java.platform.ui.Bundle", "CTL_AddPlatform")).pushNoBlock(); // Add Platform...
        NbDialogOperator addJavaPlatform = new NbDialogOperator(Bundle.getStringTrimmed("org.netbeans.modules.java.platform.ui.Bundle", "CTL_AddPlatformTitle"));// Add Java Platform
        addJavaPlatform.help();
        helpWindow = new HelpOperator();

        // close
        addJavaPlatform.cancel();
        javaPlatformManager.closeByButton();
    }

    public void testContextualSearch() {
        new HelpAction().perform();
        helpWindow = new HelpOperator();
        helpWindow.selectPageSearch();
        helpWindow.searchFind("compile");

        try {
            Thread.sleep(5000);
        } catch (Exception exc) {
            exc.printStackTrace(getLog());
        }


        JTreeOperator tree = helpWindow.treeSearch();
        log("Selection path=" + tree.getSelectionPath());
        log("Selection count=" + tree.getSelectionCount());

        if (tree.getSelectionCount() < 1) {
            fail("None founded text in the help, it isn't obvious");
        }
    }

    /** Test could be executed internaly in Forte without XTest
     * @param args arguments from command line
     */
//    public static void main(String[] args) {
//        junit.textui.TestRunner.run(suite());
//    }
}
